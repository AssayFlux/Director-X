#!/usr/bin/env bash
set -euo pipefail
# Fail closed: this helper only edits the exact FFmpeg 9.0.2 source selector.
# The native workflow builds each Android architecture independently via nix-android.sh --arch.

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
# CVE-2026-125269/125250/125251/125260/125261/125266/125207/125216:
# H.264/H.265 SEI, GIF, VVC, encoder, SGI, SANM, Interplay ACM and DPX.
# Additional reachable-codec fixes cover reviewed 125xxx/120xxx advisories:
# AV1, AAC, Bink, CDXL, CFHD, DNG, DVBSub, DOVI, EXR, FLIC, FLAC, HAP,
# HEVC/MPEG-PS/VC-2 RTP, Huffyuv, JPEG/JPEG-LS, Mimic, MLZ, MPC7,
# MPEG audio, Nellymoser, PCX, ProRes, PSD, RoQ, SBG, Sun Raster, TAK,
# Targa, VMNC, Vorbis, VP9, VVC, WavPack and related muxers/filters.
ffmpeg_source_dir="$PWD"
FFMPEG_SECURITY_COMMITS=(
  969c7eb091989a664cb7bf66a1f02051cc46cb52
  2c2f6e96e31795ae95a8f8323a493ffbc493111f
  ed27bfcbbbc0872c0195eaf4dd2c0c93b9f1778e
  f5d4fd186c68913240c4f6e9cda98443e1518b34
  b8f159858e2fd29dae4fa34d9e713bbf9bbf190e
  0bfa350291b2118f6d590263dc1ee9cea766c0f1
  bc896c613ec0d4ad0108d62fbdda0196f69e7121
  ab28d57ba2ec07b41b4a60ed8806b13a39ec86fe
  733bec3e19759520ccdbeb6c3c0cae280bbe6dc5
  a13e27336a61a3452e1ab8b4bb132c4174c67498
  053660fdb0d2403b838690689c47ad7685c3b10b
  8732d7585cd8ab9d1dd9baeb4db5e93466c0efa7
  6280907da48c4dd78f95ef5c713a29c7702153a3
  a9c070e52fecb7fb247db829130899db07953823
  ee457fdcdb3a069ef35910a2e1cacff10906b131
  b89ce54d93bf644433dfa9fbc34f19c22237691a
  60bc31fcf6543505e6d2fc1a9399cca95f54ba4d
  27f612b306eb04f81138173f705df9a9b055b01d
  e621da8ecdbef8edb8336cc899b962159dc2465c
  7f80b2ad83f37a4998d9617495c92b28a7339a90
  bf0a2e4680df92d8de6514090d350152eb7845b8
  16d19ccbce802634662a9bf18a5679cf27da2848
  11052ca4b01b021a135ec89ed7e86aa96dd3bf33
  45f98d0819efe2b7ce93e9034b7e6a4ed6700621
  1f4b2e22cfba19f94489ec4a17130f4f0d5d8a0c
  3b1cbb5e0f66b85d15f1d3527436520b3f4711ec
  90f5e61a1da2dcc0050169af8ca593530f26071b
  bcce3232016695f5e482867519c45727a0a2a2c9
  62c308e0416bb701447817fe6f0bff074d206215
  972fe1ccd326538fe9a6b5d5094b7880b5443429
  db77b0b8318ddb38b5a3227724da088a325a4308
  f71410e0288f0c40ae2fb5e5a4d7428763d08a71
  971ea9d6a2a9ae4c770936f0569cf40720d4c304
  11c4b420183cb2d6cd4afaa53de57d400a45e09e
  aa4b80b54ee8645b5a77fe835ab2bbae1c9c465c
  9edd06f8612c242b83bf9e3d14aa85180015a211
  c90bbc2036bac67e95777638bdc86157a775858a
  50d206a75b5e60261d71b25657ae04891eb60290
  cfb40c912927fdd2c1186b7b6aca468422da23db
  f8c4c6400e835bf65e8b9cc12aba9e5c4d1c818c
  5d59b7c45d7142bddd13ddc8891e72c5c7dc991b
  f8f84348dfe1a80d38e7dbbcddcdd5d92a760dfd
  968e9e948ed8c9ba90dbc875e60b843c755cf4ba
  db98784a72600a94f7ebd5f016a37e01a505c27b
  01e1bb126179879201429952cc2dcaea0d50d603
  50865f20b1f474e6b11735c88f668751bc9aa528
  c05d46ea2919da83233bf4216d3f58428bb54c59
  9b26bb4c9a4457d480899a1bf64ec080eceb1acd
  839c93f261cd3e1719edfac4244a2d0055ce8841
  5d7b743bdfb724df21fd43a3d7f3258382eb578c
  26b4c37526fe0ecf1095b45daac08050f25f7659
  5195d6f9729ad00b93e4d0f0bea309416fb4713e
  7c73707355708c846f718cca90096bc1a33c8989
  47313ad3f9f33b892384b976fb7c09b3c85fd74e
  356a391bf57df2fb80fd61f2821c8c83d15ce180
  ecd5dfc308a164757e7f25b1afefd8db21b43cf9
  edad2d0dafcf44fa315eab716fd0183ce808053b
  8702f12c9daa199cc4d7c17164b13c3cdcdc2436
  acf5d7cdc1f9ae8752c23e1ea8d7f355ed780781
  9d786e4b5e9b8482651928574de33772aeee7be1
  1cdeb3c4e7f1f8566d846b9b451e01c376398818
  65b0dab903e5975e036b30ecc58f5935d4f151e0
)
for fix in "${FFMPEG_SECURITY_COMMITS[@]}"; do
  if ! git -C "${ffmpeg_source_dir}" cat-file -e "${fix}^{commit}" 2>/dev/null; then
    git -C "${ffmpeg_source_dir}" fetch --no-tags origin "${fix}"
  fi
  # Check the patch against the actual working-tree contents, not only commit
  # ancestry. A later fork commit can revert or replace an upstream fix while
  # leaving the original commit in HEAD's history.
  # This VVC fix is ported explicitly below because Director X also
  # applies an allocation-overflow guard in the same function. Applying the
  # upstream hunk first would collide with that independent hardening edit.
  if [[ "$fix" == "a13e27336a61a3452e1ab8b4bb132c4174c67498" ]]; then
    echo "Deferring VVC SPS/PPS lifetime fix to the verified source-level port: $fix"
    continue
  fi
  if [[ "$fix" == "053660fdb0d2403b838690689c47ad7685c3b10b" ]]; then
    echo "Deferring VVC allocation-overflow fix to the verified source-level port: $fix"
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

def remove(path, block, label):
    p = root / path
    s = p.read_text(encoding='utf-8')
    count = s.count(block)
    if count == 0:
        return
    if count != 1:
        raise SystemExit(f'{label}: expected one removable block in {path}, found {count}')
    p.write_text(s.replace(block, ''), encoding='utf-8')

# Port the ProRes worst-case slice reserve and 64-bit frame-size fix to
# the exact 9.0.2 source; upstream commit 439b452f assumes 96eea382 already applied.
edit('libavcodec/proresenc_kostya.c',
     '    int max_slice_size = (ctx->frame_size_upper_bound - 200) / (ctx->pictures_per_frame * ctx->slices_per_picture + 1);\n    uint8_t frame_flags;\n',
     '    int max_slice_size = (ctx->frame_size_upper_bound - 200) / (ctx->pictures_per_frame * ctx->slices_per_picture + 1);\n    int slice_size_upper_bound = ctx->mbs_per_slice * (ctx->chroma_factor == CFACTOR_Y444 ? 768 : 512) * 8;\n    uint8_t frame_flags;\n',
     'ProRes slice upper-bound declaration')
edit('libavcodec/proresenc_kostya.c',
     '    uint8_t frame_flags;\n\n    ctx->pic = pic;\n',
     '    uint8_t frame_flags;\n\n    if (ctx->alpha_bits)\n        slice_size_upper_bound += ctx->mbs_per_slice * 256 * (1 + ctx->alpha_bits + 1) + 7 >> 3;\n\n    ctx->pic = pic;\n',
     'ProRes alpha slice upper bound')
edit('libavcodec/proresenc_kostya.c',
     '    pkt_size = ctx->frame_size_upper_bound;\n',
     '    pkt_size = ctx->frame_size_upper_bound + slice_size_upper_bound;\n',
     'ProRes initial packet reserve')
edit('libavcodec/proresenc_kostya.c',
     '                if (pkt_size <= buf - orig_buf + 2 * max_slice_size) {\n',
     '                if (pkt_size - (buf - orig_buf) < slice_size_upper_bound) {\n',
     'ProRes packet reserve condition')
edit('libavcodec/proresenc_kostya.c',
     '                    delta = FFMAX(delta, 2 * max_slice_size);\n',
     '                    delta = FFMAX(delta, slice_size_upper_bound);\n',
     'ProRes packet growth bound')
edit('libavcodec/proresenc_kostya.c',
     '    int slice_size_upper_bound = ctx->mbs_per_slice * (ctx->chroma_factor == CFACTOR_Y444 ? 768 : 512) * 8;\n',
     '    int slice_size_upper_bound = ctx->slice_size_upper_bound;\n',
     'ProRes shared slice bound')
remove('libavcodec/proresenc_kostya.c',
       '    if (ctx->alpha_bits)\n        slice_size_upper_bound += ctx->mbs_per_slice * 256 * (1 + ctx->alpha_bits + 1) + 7 >> 3;\n\n',
       'ProRes duplicate alpha bound')
edit('libavcodec/proresenc_kostya_common.h',
     '    int frame_size_upper_bound;\n',
     '    int frame_size_upper_bound;\n    int slice_size_upper_bound;\n',
     'ProRes context slice bound')
edit('libavcodec/proresenc_kostya_common.c',
     '    ctx->frame_size_upper_bound = (ctx->pictures_per_frame *\n                                   ctx->slices_per_picture + 1) *\n                                  (2 + 2 * ctx->num_planes +\n                                   (mps * ctx->bits_per_mb) / 8)\n                                  + 200;\n\n    if (ctx->alpha_bits) {\n         // The alpha plane is run-coded and might exceed the bit budget.\n         ctx->frame_size_upper_bound += (ctx->pictures_per_frame *\n                                         ctx->slices_per_picture + 1) *\n         /* num pixels per slice */     (ctx->mbs_per_slice * 256 *\n         /* bits per pixel */            (1 + ctx->alpha_bits + 1) + 7 >> 3);\n    }\n',
     '    int64_t frame_size_upper_bound = (int64_t)(ctx->pictures_per_frame *\n                                   ctx->slices_per_picture + 1) *\n                                  (2 + 2 * ctx->num_planes +\n                                   (mps * ctx->bits_per_mb) / 8)\n                                  + 200;\n    ctx->slice_size_upper_bound = mps * (ctx->chroma_factor == CFACTOR_Y444 ? 768 : 512) * 8;\n\n    if (ctx->alpha_bits) {\n        int alpha_slice_size = (mps * 256 * (1 + ctx->alpha_bits + 1) + 7) >> 3;\n        frame_size_upper_bound += (int64_t)(ctx->pictures_per_frame *\n                                            ctx->slices_per_picture + 1) * alpha_slice_size;\n        ctx->slice_size_upper_bound += alpha_slice_size;\n    }\n\n    if (frame_size_upper_bound + ctx->slice_size_upper_bound + AV_INPUT_BUFFER_PADDING_SIZE > INT_MAX) {\n        av_log(avctx, AV_LOG_ERROR, "Frame too large to encode\\n");\n        return AVERROR(EINVAL);\n    }\n    ctx->frame_size_upper_bound = frame_size_upper_bound;\n',
     'ProRes 64-bit frame-size bound')

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
# Port upstream a13e2733 explicitly: acquire SPS/PPS references only
# after frame-buffer allocation succeeds. This avoids leaking references when
# metadata attachment or buffer allocation fails. Keep the overflow guard too.
edit('libavcodec/vvc/refs.c', '        frame->sps = av_refstruct_ref_c(fc->ps.sps);\n        frame->pps = av_refstruct_ref_c(fc->ps.pps);\n\n        // Add LCEVC SEI metadata here, as it's needed in get_buffer()\n', '        // Add LCEVC SEI metadata here, as it's needed in get_buffer()\n', 'VVC delayed SPS/PPS references (remove early acquisition)')
edit('libavcodec/vvc/refs.c',
     '        ret = ff_thread_get_buffer(s->avctx, frame->frame, AV_GET_BUFFER_FLAG_REF);\n        if (ret < 0)\n            return NULL;\n\n',
     '        ret = ff_thread_get_buffer(s->avctx, frame->frame, AV_GET_BUFFER_FLAG_REF);\n        if (ret < 0)\n            return NULL;\n\n        frame->sps = av_refstruct_ref_c(fc->ps.sps);\n        frame->pps = av_refstruct_ref_c(fc->ps.pps);\n\n',
     'VVC delayed SPS/PPS references (acquire after buffer)')
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
edit('libavcodec/mpegaudiodec_template.c', '    init_get_bits(&s->gb, buf + HEADER_SIZE, (buf_size - HEADER_SIZE) * 8);\n    if (s->error_protection)', '    ret = init_get_bits8(&s->gb, buf + HEADER_SIZE, buf_size - HEADER_SIZE);\n    if (ret < 0)\n        return ret;\n    if (s->error_protection)', 'MP3 packet bit-reader bounds')
FFMPEG_SECURITY_PY
if [[ $? -ne 0 ]]; then exit 1; fi
if ! git -C "${ffmpeg_source_dir}" diff --check; then
  echo "FFmpeg security backport diff whitespace validation failed" >&2
  exit 1
fi
require_ffmpeg_security_check() {
  local label="$1"
  local pattern="$2"
  local file="$3"
  if ! grep -Fq -- "$pattern" "$file"; then
    echo "FFmpeg security verification failed: $label ($file; expected literal: $pattern)" >&2
    exit 1
  fi
  echo "FFmpeg security verification passed: $label"
}
require_ffmpeg_security_check "encryption-info iterative free" "while (info)" "${ffmpeg_source_dir}/libavutil/encryption_info.c"
require_ffmpeg_security_check "PSSH append helper" "mov_append_pssh_side_data" "${ffmpeg_source_dir}/libavformat/mov.c"
require_ffmpeg_security_check "PSSH capacity tracking" "pssh_side_data" "${ffmpeg_source_dir}/libavformat/mov.c"
require_ffmpeg_security_check "empty search-string guard" "if (!fromlen)" "${ffmpeg_source_dir}/libavutil/avstring.c"
require_ffmpeg_security_check "SEI slot initialization" "memset(&list->messages[list->nb_messages]" "${ffmpeg_source_dir}/libavcodec/cbs_sei.c"
require_ffmpeg_security_check "GIF stride initialization" "ptr_linesize = linesize" "${ffmpeg_source_dir}/libavcodec/gif.c"
require_ffmpeg_security_check "SGI signed-stride handling" "ptrdiff_t" "${ffmpeg_source_dir}/libavcodec/sgienc.c"
require_ffmpeg_security_check "VVC allocation overflow guard" "av_size_mult(s->current_frame.nb_units" "${ffmpeg_source_dir}/libavcodec/vvc/refs.c"
require_ffmpeg_security_check "SANM LUT initialization" "memcpy(lut, ctx->c23lut, 256)" "${ffmpeg_source_dir}/libavcodec/sanm.c"
require_ffmpeg_security_check "DPX bit-depth validation" "Unsupported bit depth" "${ffmpeg_source_dir}/libavcodec/dpxenc.c"
require_ffmpeg_security_check "MP3 bit-reader bounds" "ret = init_get_bits8(&s->gb, buf + HEADER_SIZE, buf_size - HEADER_SIZE)" "${ffmpeg_source_dir}/libavcodec/mpegaudiodec_template.c"
require_ffmpeg_security_check "encoder frame-dimension validation" "src->width != avctx->width" "${ffmpeg_source_dir}/libavcodec/encode.c"
require_ffmpeg_security_check "ProRes slice-size bound" "ctx->slice_size_upper_bound = mps" "${ffmpeg_source_dir}/libavcodec/proresenc_kostya_common.c"
require_ffmpeg_security_check "ProRes packet reserve" "pkt_size = ctx->frame_size_upper_bound + slice_size_upper_bound" "${ffmpeg_source_dir}/libavcodec/proresenc_kostya.c"
require_ffmpeg_security_check "ProRes oversized-frame rejection" "Frame too large to encode" "${ffmpeg_source_dir}/libavcodec/proresenc_kostya_common.c"
require_ffmpeg_security_check "Interplay ACM allocation cap" "s->block_len > 1024 * 1024" "${ffmpeg_source_dir}/libavcodec/interplayacm.c"
'''
path.write_text(text.replace(anchor, insert + anchor).rstrip() + "\n", encoding="utf-8")
print("Prepared pinned FFmpeg security backports.")
PY

# Parse the embedded security backport Python before any source edits/build.
python3 - "$0" <<'PY'
from pathlib import Path
import ast, re, sys
source = Path(sys.argv[1]).read_text(encoding="utf-8")
blocks = re.findall(r"<<'FFMPEG_SECURITY_PY'\\n(.*?)\\nFFMPEG_SECURITY_PY", source, re.S)
if len(blocks) != 1:
    raise SystemExit(f"Expected one FFmpeg security Python block, found {len(blocks)}")
ast.parse(blocks[0], filename="embedded-ffmpeg-security.py")
print("Embedded FFmpeg security Python syntax validated.")
PY

bash -n "$source_file"
