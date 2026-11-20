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
insert = r'''# Director X security backports for FFmpeg 9.0.2:
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
  26c2980980295e5516f308c5a2792d2a72e43eae
  c6e5b6105aa13f6b0df2a646da608078cfe52d79
  aa5fd7d287db4f469fdcbe444f7f9f38c8c289c7
  6ecaddb9c1d59cf7f5de16017122fecb2fa0c67c
)
for fix in "${FFMPEG_SECURITY_COMMITS[@]}"; do
  if ! git -C "${ffmpeg_source_dir}" cat-file -e "${fix}^{commit}" 2>/dev/null; then
    git -C "${ffmpeg_source_dir}" fetch --no-tags origin "${fix}"
  fi
  if git -C "${ffmpeg_source_dir}" merge-base --is-ancestor "${fix}" HEAD; then
    continue
  fi
  git -C "${ffmpeg_source_dir}" show --format= "${fix}" | git -C "${ffmpeg_source_dir}" apply --check -
  git -C "${ffmpeg_source_dir}" show --format= "${fix}" | git -C "${ffmpeg_source_dir}" apply -
done
# Port fork security fixes onto the exact n9.0.2 source; do not cherry-pick
# incompatible fork-only context or change upstream library version macros.
python3 - "${ffmpeg_source_dir}" <<'PY'
from pathlib import Path
import sys
root = Path(sys.argv[1])
def edit(path, old, new, label):
    p = root / path
    s = p.read_text(encoding='utf-8')
    if s.count(old) != 1:
        raise SystemExit(f'{label}: expected one exact anchor in {path}, got {s.count(old)}')
    p.write_text(s.replace(old, new), encoding='utf-8')

# PSSH records are appended in place with tracked capacity: O(n) total work.
mov = root / 'libavformat/mov.c'
s = mov.read_text(encoding='utf-8')
anchor = 'static int mov_read_pssh(MOVContext *c, AVIOContext *pb, MOVAtom atom)\n'
helper = '''static int mov_append_pssh_side_data(MOVContext *c, AVStream *st,
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
        if (old_side_data->size < 4)
            return AVERROR_INVALIDDATA;
        if (old_side_data->data != c->pssh_side_data)
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
        uint64_t record_size = 16ULL + cur->system_id_size + cur->data_size +
                               (uint64_t)cur->num_key_ids * cur->key_id_size;
        if (new_count == UINT32_MAX || record_size > UINT32_MAX - new_size)
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
    return 0;
}

'''
if s.count(anchor) != 1: raise SystemExit('PSSH helper anchor mismatch')
s = s.replace(anchor, helper + anchor)
old_decl = '''    AVEncryptionInitInfo *info, *old_init_info;
    uint8_t **key_ids;
    AVStream *st;
    const AVPacketSideData *old_side_data;
    uint8_t *side_data, *extra_data;
    size_t side_data_size;'''
new_decl = '''    AVEncryptionInitInfo *info;
    uint8_t **key_ids;
    AVStream *st;
    uint8_t *extra_data;'''
if s.count(old_decl) != 1: raise SystemExit('PSSH declaration mismatch')
s = s.replace(old_decl, new_decl)
import re
pattern = r"    // If there is existing initialization data, append to the list\\..*?        av_free\\(side_data\\);"
replacement = """    ret = mov_append_pssh_side_data(c, st, info);
    if (ret < 0)
        goto finish;"""
s, replaced = re.subn(pattern, replacement, s, flags=re.S)
if replaced != 1: raise SystemExit(f"PSSH append block replacement count: {replaced}")
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
PY
git -C "${ffmpeg_source_dir}" diff --check
grep -q 'while (info)' "${ffmpeg_source_dir}/libavutil/encryption_info.c"
grep -q 'mov_append_pssh_side_data' "${ffmpeg_source_dir}/libavformat/mov.c"
grep -q 'pssh_side_data' "${ffmpeg_source_dir}/libavformat/mov.c"
grep -q 'if (!fromlen)' "${ffmpeg_source_dir}/libavutil/avstring.c"
grep -Fq 'memset(&list->messages[list->nb_messages]' "${ffmpeg_source_dir}/libavcodec/cbs_sei.c"
grep -q 'ptr_linesize = linesize' "${ffmpeg_source_dir}/libavcodec/gif.c"
grep -q 'ptrdiff_t' "${ffmpeg_source_dir}/libavcodec/sgienc.c"
grep -q 'av_size_mult(s->current_frame.nb_units' "${ffmpeg_source_dir}/libavcodec/vvc/refs.c"
grep -q 'memcpy(lut, ctx->c23lut, 256)' "${ffmpeg_source_dir}/libavcodec/sanm.c"
grep -q 'Unsupported bit depth' "${ffmpeg_source_dir}/libavcodec/dpxenc.c"
grep -q 'src->width != avctx->width' "${ffmpeg_source_dir}/libavcodec/encode.c"
grep -q 's->block_len > 1024 \* 1024' "${ffmpeg_source_dir}/libavcodec/interplayacm.c"
'''
path.write_text(text.replace(anchor, insert + anchor).rstrip() + "\n", encoding="utf-8")
print("Prepared pinned FFmpeg security backports.")
PY

bash -n "$source_file"
