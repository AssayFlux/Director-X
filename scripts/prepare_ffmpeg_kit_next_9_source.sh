#!/usr/bin/env bash
set -euo pipefail
# Fail closed: this helper only edits the exact FFmpeg 9.0.2 source selector.

source_file="${1:-scripts/source.sh}"
test -f "$source_file"

python3 - "$source_file" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")
old = '''  ffmpeg)
    SOURCE_REPO_URL="https://github.com/arthenica/FFmpeg"
    SOURCE_ID="n9.0.2"
    SOURCE_TYPE="TAG"
    ;;'''
new = '''  ffmpeg)
    SOURCE_REPO_URL="https://github.com/arthenica/FFmpeg"
    SOURCE_ID="n9.0.2"
    SOURCE_TYPE="TAG"
    ;;'''
count = text.count(old)
if count == 0:
    if new in text:
        print("FFmpeg source mode already prepared.")
        raise SystemExit(0)
    raise SystemExit("Expected pinned FFmpeg source block not found; refusing an ambiguous edit.")
if count != 1:
    raise SystemExit(f"Expected exactly one FFmpeg source block, found {count}.")
path.write_text(text.replace(old, new), encoding="utf-8")
print("Prepared pinned FFmpeg source as a commit checkout.")
PY


# Apply the pinned FriBidi v1.0.16 generator correction after source checkout.
# Keep this out of the FFmpeg patch so each upstream file hunk stays independently
# verifiable and the helper fails closed if the Automake recipe changes.
kit_root="$(dirname "$(dirname "$source_file")")"
python3 - "$kit_root/scripts/android/fribidi.sh" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")
marker = "# Director X build correction for FriBidi v1.0.16:"
if marker in text:
    print("FriBidi generator correction already prepared.")
    raise SystemExit(0)
anchor = "${SED_INLINE}"
if text.count(anchor) != 1:
    raise SystemExit("Expected exactly one post-configure FriBidi anchor; refusing an ambiguous edit.")
insert = '''# Director X build correction for FriBidi v1.0.16:
# Automake's $^ includes the generator executable as an input file. The
# upstream generator expects only the two Unicode data files; pass them
# explicitly to prevent the extra executable path from corrupting argv.
printf 'FriBidi generator working directory: %s\\n' "$PWD"
python3 - <<'FRIBIDI_PY' || return 1
from pathlib import Path
import re

old = "./$(gen_unicode_version) $^ > $@"
new = "python3 ./gen-unicode-version.py unidata/ReadMe.txt > $@"
for path in (Path("gen.tab/Makefile.in"), Path("gen.tab/Makefile")):
    if not path.is_file():
        raise SystemExit(f"Expected configured FriBidi generator file: {path}")
    text = path.read_text(encoding="utf-8")
    if text.count(old) == 1:
        text = text.replace(old, new)
    elif text.count(new) != 1:
        raise SystemExit(f"Expected one original FriBidi Unicode-version recipe in {path}; old={text.count(old)} new={text.count(new)}")

    # The six table generators consume a compression level followed by
    # exactly the Unicode data inputs declared for each generated table.
    # Automake $^ also includes the generated executable and version header,
    # neither of which is a data file accepted by these generators.
    recipes = {
        "./$(gen_bidi_type_tab) $(COMPRESSION) $^ > $@": "./$(gen_bidi_type_tab) $(COMPRESSION) unidata/UnicodeData.txt > $@",
        "./$(gen_joining_type_tab) $(COMPRESSION) $^ > $@": "./$(gen_joining_type_tab) $(COMPRESSION) unidata/UnicodeData.txt unidata/ArabicShaping.txt > $@",
        "./$(gen_arabic_shaping_tab) $(COMPRESSION) $^ > $@": "./$(gen_arabic_shaping_tab) $(COMPRESSION) unidata/UnicodeData.txt > $@",
        "./$(gen_mirroring_tab) $(COMPRESSION) $^ > $@": "./$(gen_mirroring_tab) $(COMPRESSION) unidata/BidiMirroring.txt > $@",
        "./$(gen_brackets_tab) $(COMPRESSION) $^ > $@": "./$(gen_brackets_tab) $(COMPRESSION) unidata/BidiBrackets.txt unidata/UnicodeData.txt > $@",
        "./$(gen_brackets_type_tab) $(COMPRESSION) $^ > $@": "./$(gen_brackets_type_tab) $(COMPRESSION) unidata/BidiBrackets.txt > $@",
    }
    for source, target in recipes.items():
        count = text.count(source)
        if count == 1:
            text = text.replace(source, target)
        elif count != 0 or text.count(target) != 1:
            raise SystemExit(f"Expected one original or corrected recipe in {path}: {source}; found {count}")

    # AX_PROG_CC_FOR_BUILD selected the runner's generic gcc while the Nix
    # cross environment exposes its verified x86_64 host compiler as HOST_CC.
    # Force only these host-side table generators to use HOST_CC; keep the
    # hardening flags active and explicit.
    old_cc = "CC = $(CC_FOR_BUILD)"
    new_cc = "CC = $(HOST_CC)"
    if text.count(old_cc) == 1:
        text = text.replace(old_cc, new_cc)
    elif text.count(new_cc) != 1:
        raise SystemExit(f"Expected one FriBidi host compiler selector in {path}; old={text.count(old_cc)} new={text.count(new_cc)}")
    old_flags = "CFLAGS = $(CFLAGS_FOR_BUILD)"
    new_flags = "CFLAGS = $(CFLAGS_FOR_BUILD) -fstack-protector-strong"
    if text.count(old_flags) == 1:
        text = text.replace(old_flags, new_flags)
    elif text.count(new_flags) != 1:
        raise SystemExit(f"Expected one FriBidi host CFLAGS selector in {path}; old={text.count(old_flags)} new={text.count(new_flags)}")
    path.write_text(text, encoding="utf-8")
    print(f"Patched FriBidi generator recipe in {path}")

generator = """#!/usr/bin/env python3
import pathlib
import sys

source = pathlib.Path(sys.argv[1]).read_text(encoding="utf-8")
version = None
for line in source.splitlines():
    for marker in ("Version ", "Unicode "):
        if marker in line:
            candidate = line.split(marker, 1)[1].split()[0].rstrip(".,;")
            parts = candidate.split(".")
            if len(parts) == 3 and all(part.isdigit() for part in parts):
                version = tuple(map(int, parts))
                break
    if version is not None:
        break
if version is None:
    raise SystemExit("Could not determine Unicode version from pinned FriBidi ReadMe.txt")
major, minor, micro = version
print(f'#define FRIBIDI_UNICODE_VERSION "{major}.{minor}.{micro}"')
print(f"#define FRIBIDI_UNICODE_MAJOR_VERSION {major}")
print(f"#define FRIBIDI_UNICODE_MINOR_VERSION {minor}")
print(f"#define FRIBIDI_UNICODE_MICRO_VERSION {micro}")
"""
Path("gen.tab/gen-unicode-version.py").write_text(generator, encoding="utf-8")
print("Installed bounded Python FriBidi Unicode-version generator.")
FRIBIDI_PY

'''
path.write_text(text.replace(anchor, insert + anchor), encoding="utf-8")
print("Prepared the pinned FriBidi generator correction.")
PY

# Build FreeType's API-name scanner with the Nix host compiler, not the
# generic runner gcc selected by FreeType's configure script. The scanner is a
# build-host executable; its output is consumed by the target static library.
# Keep host stack protection enabled.
python3 - "$kit_root/scripts/android/freetype.sh" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")
old = 'make -j$(get_cpu_count) || return 1'
new = 'make -j$(get_cpu_count) CCexe="${HOST_CC}" CCexe_CFLAGS="-fstack-protector-strong" CCexe_LDFLAGS="-fstack-protector-strong" || return 1'
if text.count(old) == 1:
    text = text.replace(old, new)
elif text.count(new) != 1:
    raise SystemExit(f"Expected one FreeType make invocation; old={text.count(old)} new={text.count(new)}")
path.write_text(text, encoding="utf-8")
print("Configured FreeType apinames to use the hardened Nix host compiler.")
PY

# Backport independently verified upstream FFmpeg security fixes onto the pinned
# n9.0.2-derived fork. Keep the base revision pinned and verify every patch.
python3 - "$kit_root/scripts/android/ffmpeg.sh" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")
marker = "# Director X security backports for FFmpeg 9.0.2:"
if marker in text:
    print("FFmpeg security backport hook already prepared.")
    raise SystemExit(0)
anchor = "# SET BUILD OPTIONS"
if text.count(anchor) != 1:
    raise SystemExit("Expected exactly one FFmpeg build-options anchor; refusing an ambiguous edit.")
insert = r'''export LDFLAGS="${LDFLAGS} -Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384"
# Director X security backports for FFmpeg 9.0.2:
# CVE-2026-107678: iterative free plus append-in-place PSSH side data.
# CVE-2026-107676: initialize all HDR10+ serialized payload bits.
# CVE-2026-107677: reject empty search strings in av_strireplace.
# CVE-2026-125269: clear recycled H.264/H.265 SEI message slots.
# CVE-2026-125250/251/260/261/266/207/216: GIF, VVC, generic encoder,
# SGI, SANM, Interplay ACM and DPX boundary/initialization fixes.
ffmpeg_source_dir="$PWD"
FFMPEG_SECURITY_COMMITS=(
  969c7eb091989a664cb7bf66a1f02051cc46cb52
  2c2f6e96e31795ae95a8f8323a493ffbc493111f
  ed27bfcbbbc0872c0195eaf4dd2c0c93b9f1778e
  b8f159858e2fd29dae4fa34d9e713bbf9bbf190e
  0bfa350291b2118f6d590263dc1ee9cea766c0f1
  bc896c613ec0d4ad0108d62fbdda0196f69e7121
  f5d4fd186c68913240c4f6e9cda98443e1518b34
  439b452f9fe916a9f7d8788ab6afee8fac846b0b
  ab28d57ba2ec07b41b4a60ed8806b13a39ec86fe
  733bec3e19759520ccdbeb6c3c0cae280bbe6dc5
  a13e27336a61a3452e1ab8b4bb132c4174c67498
  053660fdb0d2403b838690689c47ad7685c3b10b
  8732d7585cd8ab9d1dd9baeb4db5e93466c0efa7
  6280907da48c4dd78f95ef5c713a29c7702153a3
  a9c070e52fecb7fb247db829130899db07953823
  ee457fdcdb3a069ef35910a2e1cacff10906b131
  60bc31fcf6543505e6d2fc1a9399cca95f54ba4d
  27f612b306eb04f81138173f705df9a9b055b01d
  e621da8ecdbef8edb8336cc899b962159dc2465c
  7f80b2ad83f37a4998d9617495c92b28a7339a90
  bf0a2e4680df92d8de6514090d350152eb7845b8
  16d19ccbce802634662a9bf18a5679cf27da2848
)
for fix in "${FFMPEG_SECURITY_COMMITS[@]}"; do
  if ! git -C "${ffmpeg_source_dir}" cat-file -e "${fix}^{commit}" 2>/dev/null; then
    git -C "${ffmpeg_source_dir}" fetch --no-tags origin "${fix}"
  fi
  if git -C "${ffmpeg_source_dir}" merge-base --is-ancestor "${fix}" HEAD; then
    continue
  fi
  patch_file="$(mktemp)"
  git -C "${ffmpeg_source_dir}" show --format= "${fix}" > "$patch_file"
  if git -C "${ffmpeg_source_dir}" apply --check "$patch_file"; then
    git -C "${ffmpeg_source_dir}" apply "$patch_file" || {
      rm -f "$patch_file"
      exit 1
    }
  elif git -C "${ffmpeg_source_dir}" apply --reverse --check "$patch_file"; then
    echo "FFmpeg security backport already present: $fix"
  else
    echo "FFmpeg security backport is neither cleanly applicable nor already applied: $fix" >&2
    rm -f "$patch_file"
    exit 1
  fi
  rm -f "$patch_file"
done
# Port fork security fixes onto the exact n9.0.2 source; do not cherry-pick
# incompatible fork-only context or change upstream library version macros.
python3 - "${ffmpeg_source_dir}" <<'FFMPEG_SECURITY_PY'
from pathlib import Path
import re
import sys
root = Path(sys.argv[1])
def edit(path, old, new, label):
    p = root / path
    s = p.read_text(encoding='utf-8')
    if new in s:
        if s.count(new) != 1:
            raise SystemExit(f'{label}: corrected block is not unique in {path}')
        return
    if s.count(old) != 1:
        raise SystemExit(f'{label}: expected one original or corrected anchor in {path}; old={s.count(old)} new={s.count(new)}')
    p.write_text(s.replace(old, new), encoding='utf-8')

# PSSH records are appended in place with tracked capacity: O(n) total work.
mov = root / 'libavformat/mov.c'
s = mov.read_text(encoding='utf-8')
anchor = 'static int mov_read_pssh(MOVContext *c, AVIOContext *pb, MOVAtom atom)\n'
helper = """static int mov_append_pssh_side_data(MOVContext *c, AVStream *st,
                                     const AVEncryptionInitInfo *info)
{
    AVPacketSideData *old_side_data = NULL;
    const AVEncryptionInitInfo *cur;
    uint8_t *buffer, *cur_buffer;
    uint64_t init_info_count, new_count, new_size;
    size_t old_size;
    unsigned int i;

    for (i = 0; i < st->codecpar->nb_coded_side_data; i++) {
        AVPacketSideData *sd = &st->codecpar->coded_side_data[i];
        if (sd->type == AV_PKT_DATA_ENCRYPTION_INIT_INFO) {
            old_side_data = sd;
            break;
        }
    }
    if (old_side_data) {
        if (old_side_data->size < 4 || old_side_data->size > UINT32_MAX)
            return AVERROR_INVALIDDATA;
        if (old_side_data->data != c->pssh_side_data || st != c->pssh_stream)
            c->pssh_side_data_allocated = old_side_data->size;
        old_size = old_side_data->size;
        init_info_count = AV_RB32(old_side_data->data);
    } else {
        c->pssh_side_data_allocated = 0;
        old_size = 4;
        init_info_count = 0;
    }
    new_count = init_info_count;
    new_size = old_size;
    for (cur = info; cur; cur = cur->next) {
        uint64_t record_size = 16;
        if (new_count == UINT32_MAX ||
            cur->system_id_size > UINT32_MAX - record_size)
            return AVERROR(EINVAL);
        record_size += cur->system_id_size;
        if (cur->data_size > UINT32_MAX - record_size)
            return AVERROR(EINVAL);
        record_size += cur->data_size;
        if (cur->num_key_ids &&
            cur->key_id_size > (UINT32_MAX - record_size) / cur->num_key_ids)
            return AVERROR(EINVAL);
        record_size += (uint64_t)cur->num_key_ids * cur->key_id_size;
        if (record_size > UINT32_MAX - new_size)
            return AVERROR(EINVAL);
        new_count++;
        new_size += record_size;
    }
    buffer = av_fast_realloc(old_side_data ? old_side_data->data : NULL,
                             &c->pssh_side_data_allocated, (size_t)new_size);
    if (!buffer)
        return AVERROR(ENOMEM);
    AV_WB32(buffer, (uint32_t)new_count);
    cur_buffer = buffer + old_size;
    for (cur = info; cur; cur = cur->next) {
        AV_WB32(cur_buffer, cur->system_id_size);
        AV_WB32(cur_buffer + 4, cur->num_key_ids);
        AV_WB32(cur_buffer + 8, cur->key_id_size);
        AV_WB32(cur_buffer + 12, cur->data_size);
        cur_buffer += 16;
        memcpy(cur_buffer, cur->system_id, cur->system_id_size);
        cur_buffer += cur->system_id_size;
        for (i = 0; i < cur->num_key_ids; i++) {
            memcpy(cur_buffer, cur->key_ids[i], cur->key_id_size);
            cur_buffer += cur->key_id_size;
        }
        if (cur->data_size) {
            memcpy(cur_buffer, cur->data, cur->data_size);
            cur_buffer += cur->data_size;
        }
    }
    if (old_side_data) {
        old_side_data->data = buffer;
        old_side_data->size = (size_t)new_size;
    } else if (!av_packet_side_data_add(&st->codecpar->coded_side_data,
                                        &st->codecpar->nb_coded_side_data,
                                        AV_PKT_DATA_ENCRYPTION_INIT_INFO,
                                        buffer, (size_t)new_size, 0)) {
        av_free(buffer);
        c->pssh_side_data_allocated = 0;
        return AVERROR(ENOMEM);
    }
    c->pssh_side_data = buffer;
    c->pssh_stream = st;
    return 0;
}

"""
if 'static int mov_append_pssh_side_data(' in s:
    if s.count('static int mov_append_pssh_side_data(') != 1:
        raise SystemExit('PSSH helper is duplicated')
else:
    if s.count(anchor) != 1:
        raise SystemExit('PSSH helper anchor mismatch')
    s = s.replace(anchor, helper + anchor)
s = s.replace("AVEncryptionInitInfo *info, *old_init_info;", "AVEncryptionInitInfo *info;")
s = s.replace("    const AVPacketSideData *old_side_data;", "")
s = s.replace("    uint8_t *side_data, *extra_data;", "    uint8_t *extra_data;")
s = s.replace("    size_t side_data_size;", "")
pattern = r"    // If there is existing initialization data, append to the list\..*?        av_free\(side_data\);"
replacement = """    ret = mov_append_pssh_side_data(c, st, info);
    if (ret < 0)
        goto finish;"""
s, replaced = re.subn(pattern, replacement, s, flags=re.S)
if replaced == 0 and s.count(replacement) == 1:
    print("PSSH append transformation already present.")
elif replaced != 1:
    raise SystemExit(f"PSSH append block replacement count: {replaced}; replacement={s.count(replacement)}")
mov.write_text(s, encoding='utf-8')

edit('libavformat/isom.h', '    MOVFragmentIndex frag_index;\n    int atom_depth;',
     '    MOVFragmentIndex frag_index;\n    uint8_t *pssh_side_data;\n    AVStream *pssh_stream;\n    unsigned int pssh_side_data_allocated;\n    int atom_depth;', 'PSSH context')
edit('libavformat/mov.c', '    mov->found_mdat = 0;\n',
     '    mov->found_mdat = 0;\n    mov->pssh_side_data = NULL;\n    mov->pssh_stream = NULL;\n    mov->pssh_side_data_allocated = 0;\n', 'PSSH reset')
edit('libavcodec/vvc/refs.c',
     '        frame->rpl = av_refstruct_allocz(s->current_frame.nb_units * sizeof(RefPicListTab));\n',
     '        size_t rpl_size;\n        if (av_size_mult(s->current_frame.nb_units, sizeof(RefPicListTab), &rpl_size) < 0)\n            goto fail;\n        frame->rpl = av_refstruct_allocz(rpl_size);\n', 'VVC allocation')
edit('libavcodec/encode.c', '    int ret;\n\n    if (avctx->codec->type == AVMEDIA_TYPE_AUDIO) {',
     '    int ret;\n\n    if (avctx->codec->type == AVMEDIA_TYPE_VIDEO) {\n        const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(src->format);\n        if (!(desc && desc->flags & AV_PIX_FMT_FLAG_HWACCEL) &&\n            (src->format != avctx->pix_fmt || src->width != avctx->width || src->height != avctx->height)) {\n            av_log(avctx, AV_LOG_ERROR, "Frame size or pixel format does not match the encoder\\n");\n            return AVERROR(EINVAL);\n        }\n    }\n\n    if (avctx->codec->type == AVMEDIA_TYPE_AUDIO) {', 'encoder dimensions')
edit('libavcodec/cbs_sei.c', '    ++list->nb_messages;\n    return 0;\n',
     '    memset(&list->messages[list->nb_messages], 0, sizeof(*list->messages));\n    ++list->nb_messages;\n    return 0;\n', 'SEI slot')
edit('libavcodec/sanm.c', '        bytestream2_get_bufferu(gb, ctx->c23lut, 256);\n',
     '        bytestream2_get_bufferu(gb, ctx->c23lut, 256);\n        memcpy(lut, ctx->c23lut, 256);\n', 'SANM LUT')
edit('libavcodec/gif.c', '    const uint8_t *ptr;\n', '    const uint8_t *ptr;\n    int ptr_linesize = linesize;\n', 'GIF stride declaration')
edit('libavcodec/gif.c', '            s->shrunk_buf = av_malloc(avctx->height * linesize);\n', '            s->shrunk_buf = av_malloc(avctx->height * avctx->width);\n', 'GIF buffer allocation')
edit('libavcodec/gif.c', '        remap_frame_to_palette(buf, linesize, s->shrunk_buf, linesize, avctx->width, avctx->height, map);\n        ptr = s->shrunk_buf + y_start*linesize + x_start;\n', '        remap_frame_to_palette(buf, linesize, s->shrunk_buf, avctx->width, avctx->width, avctx->height, map);\n        ptr = s->shrunk_buf + y_start*avctx->width + x_start;\n        ptr_linesize = avctx->width;\n', 'GIF remap stride')
gif_path = root / 'libavcodec/gif.c'
gif_text = gif_path.read_text(encoding='utf-8')
gif_old = '            ptr += linesize;\n'
gif_new = '            ptr += ptr_linesize;\n'
if gif_text.count(gif_old) == 2:
    gif_text = gif_text.replace(gif_old, gif_new)
elif gif_text.count(gif_old) != 0 or gif_text.count(gif_new) != 2:
    raise SystemExit(f'GIF row stride anchors: old={gif_text.count(gif_old)} new={gif_text.count(gif_new)}')
gif_path.write_text(gif_text, encoding='utf-8')
edit('libavcodec/sgienc.c', '    unsigned int width, height, depth, dimension;\n    unsigned int bytes_per_channel, pixmax, put_be;\n', '    int width, height, depth, dimension;\n    int bytes_per_channel, pixmax, put_be;\n', 'SGI signed dimensions')
edit('libavcodec/sgienc.c', '    if ((ret = ff_alloc_packet(avctx, pkt, bytes_per_channel * length)) < 0)\n', '    if ((ret = ff_alloc_packet(avctx, pkt, (int64_t)bytes_per_channel * length)) < 0)\n', 'SGI packet size')
sgi_path = root / 'libavcodec/sgienc.c'
sgi_text = sgi_path.read_text(encoding='utf-8')
sgi_old = 'p->data[0] + p->linesize[0] * (height - 1)'
sgi_new = 'p->data[0] + (ptrdiff_t)p->linesize[0] * (height - 1)'
if sgi_text.count(sgi_old) == 2:
    sgi_text = sgi_text.replace(sgi_old, sgi_new)
elif sgi_text.count(sgi_old) != 0 or sgi_text.count(sgi_new) != 2:
    raise SystemExit(f'SGI signed-stride anchors: old={sgi_text.count(sgi_old)} new={sgi_text.count(sgi_new)}')
sgi_path.write_text(sgi_text, encoding='utf-8')
edit('libavcodec/dpxenc.c', '        if (avctx->bits_per_raw_sample)\n            s->bits_per_component = avctx->bits_per_raw_sample;\n', '        if (avctx->bits_per_raw_sample == 10) {\n            s->bits_per_component = 10;\n        } else if (avctx->bits_per_raw_sample && avctx->bits_per_raw_sample != 16) {\n            av_log(avctx, AV_LOG_ERROR, "Unsupported bit depth: %d\\n", avctx->bits_per_raw_sample);\n            return AVERROR(EINVAL);\n        }\n', 'DPX RGB48 depth')
edit('libavcodec/interplayacm.c', '    s->block_len = s->rows * s->cols;\n', '    s->block_len = s->rows * s->cols;\n    if (s->block_len > 1024 * 1024) {\n        av_log(avctx, AV_LOG_ERROR, "Block of %d samples is too large\\n", s->block_len);\n        return AVERROR_INVALIDDATA;\n    }\n', 'Interplay ACM allocation cap')
FFMPEG_SECURITY_PY
if [[ $? -ne 0 ]]; then exit 1; fi
git -C "${ffmpeg_source_dir}" diff --check || exit 1
grep -q 'while (info)' "${ffmpeg_source_dir}/libavutil/encryption_info.c" || exit 1
grep -q 'mov_append_pssh_side_data' "${ffmpeg_source_dir}/libavformat/mov.c" || exit 1
grep -q 'pssh_side_data' "${ffmpeg_source_dir}/libavformat/mov.c" || exit 1
grep -q 'if (!fromlen)' "${ffmpeg_source_dir}/libavutil/avstring.c" || exit 1
grep -Fq 'memset(&list->messages[list->nb_messages]' "${ffmpeg_source_dir}/libavcodec/cbs_sei.c" || exit 1
grep -q 'ptr_linesize = linesize' "${ffmpeg_source_dir}/libavcodec/gif.c" || exit 1
grep -q 'ptrdiff_t' "${ffmpeg_source_dir}/libavcodec/sgienc.c" || exit 1
grep -q 'av_size_mult(s->current_frame.nb_units' "${ffmpeg_source_dir}/libavcodec/vvc/refs.c" || exit 1
grep -q 'memcpy(lut, ctx->c23lut, 256)' "${ffmpeg_source_dir}/libavcodec/sanm.c" || exit 1
grep -q 'Unsupported bit depth' "${ffmpeg_source_dir}/libavcodec/dpxenc.c" || exit 1
grep -q 'src->width != avctx->width' "${ffmpeg_source_dir}/libavcodec/encode.c" || exit 1
grep -q 's->block_len > 1024 \* 1024' "${ffmpeg_source_dir}/libavcodec/interplayacm.c" || exit 1
'''
path.write_text(text.replace(anchor, insert + anchor).rstrip() + "\n", encoding="utf-8")
print("Prepared pinned FFmpeg security backports.")
PY

bash -n "$source_file"
