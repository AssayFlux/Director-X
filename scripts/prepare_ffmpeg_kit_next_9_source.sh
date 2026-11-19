#!/usr/bin/env bash
set -euo pipefail
# Fail closed: this helper only edits the exact pinned FFmpeg 9.x source selector.

source_file="${1:-scripts/source.sh}"
test -f "$source_file"

python3 - "$source_file" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")
old = '''  ffmpeg)
    SOURCE_REPO_URL="https://github.com/FFmpeg/FFmpeg"
    SOURCE_ID="2473872b7ceadbdf3235c0a482f58753e3f6dacb"
    SOURCE_TYPE="COMMIT"
    ;;'''
new = '''  ffmpeg)
    SOURCE_REPO_URL="https://github.com/arthenica/FFmpeg"
    SOURCE_ID="n9.0.2"
    SOURCE_TYPE="COMMIT"
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


# Patch the generated FriBidi Makefile after configure has created it.
# The upstream rule expands $^ to include the generator executable itself,
# which causes stack corruption in this pinned FriBidi generator.
kit_root="$(dirname "$(dirname "$source_file")")"
python3 - "$kit_root/scripts/android/fribidi.sh" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")
marker = "# Director X FriBidi generated-Makefile correction:"
if marker in text:
    print("FriBidi generated-Makefile correction already prepared.")
    raise SystemExit(0)
anchor = "# WORKAROUND TO DISABLE BUILDING OF doc FOLDER"
if text.count(anchor) != 1:
    raise SystemExit("Expected exactly one post-configure FriBidi anchor; refusing an ambiguous edit.")
insert = r'''# Director X FriBidi generated-Makefile correction:
FRIBIDI_SOURCE_DIR="${BASEDIR}/src/${LIB_NAME}"
FRIBIDI_GENERATOR="${FRIBIDI_SOURCE_DIR}/gen.tab/gen-unicode-version.py"
cat > "${FRIBIDI_GENERATOR}" <<'FRIBIDI_GEN'
#!/usr/bin/env python3
import pathlib
import re
import sys

source = pathlib.Path(sys.argv[1]).read_text(encoding="utf-8")
match = re.search(r"\bVersion\s+(\d+)\.(\d+)\.(\d+)\b", source)
if not match:
    raise SystemExit("Could not determine Unicode version from pinned FriBidi ReadMe.txt")
major, minor, micro = map(int, match.groups())
print("/* fribidi-unicode-version.h: generated from pinned Unicode ReadMe.txt */")
print(f'#define FRIBIDI_UNICODE_VERSION "{major}.{minor}.{micro}"')
print(f"#define FRIBIDI_UNICODE_MAJOR_VERSION {major}")
print(f"#define FRIBIDI_UNICODE_MINOR_VERSION {minor}")
print(f"#define FRIBIDI_UNICODE_MICRO_VERSION {micro}")
FRIBIDI_GEN
FRIBIDI_MAKEFILE="${FRIBIDI_SOURCE_DIR}/gen.tab/Makefile"
python3 - "${FRIBIDI_MAKEFILE}" <<'FRIBIDI_MAKE'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")
old = "./$(gen_unicode_version) $^ > $@"
new = "./$(gen_unicode_version) unidata/ReadMe.txt unidata/BidiMirroring.txt > $@"
if text.count(old) == 1:
    text = text.replace(old, new)
elif text.count(new) != 1:
    raise SystemExit("Expected exactly one FriBidi Unicode generator recipe; refusing an ambiguous Makefile.")
path.write_text(text, encoding="utf-8")
print("Removed the generator executable from FriBidi's input-file arguments.")
FRIBIDI_MAKE
'''
path.write_text(text.replace(anchor, insert + anchor), encoding="utf-8")
print("Prepared post-configure FriBidi Makefile correction.")
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
FFMPEG_SOURCE="${BASEDIR}/src/ffmpeg"
FFMPEG_SECURITY_MASTER_PIN="2473872b7ceadbdf3235c0a482f58753e3f6dacb"
ACTUAL_FFMPEG_SOURCE="$(git -C "${FFMPEG_SOURCE}" rev-parse HEAD)"
if [[ "${ACTUAL_FFMPEG_SOURCE}" != "${FFMPEG_SECURITY_MASTER_PIN}" ]]; then
  echo "::error::Refusing an unreviewed FFmpeg source revision: expected ${FFMPEG_SECURITY_MASTER_PIN}, got ${ACTUAL_FFMPEG_SOURCE}" >&2
  exit 1
fi
echo "Verified exact official FFmpeg security source pin ${FFMPEG_SECURITY_MASTER_PIN}."
git -C "${FFMPEG_SOURCE}" diff --check
grep -q 'while (info)' "${FFMPEG_SOURCE}/libavutil/encryption_info.c"
grep -q 'av_encryption_init_info_append_side_data' "${FFMPEG_SOURCE}/libavutil/encryption_info.c"
grep -q 'pssh_side_data' "${FFMPEG_SOURCE}/libavformat/mov.c"
grep -q 'if (!fromlen)' "${FFMPEG_SOURCE}/libavutil/avstring.c"
grep -Fq 'memset(&list->messages[list->nb_messages]' "${FFMPEG_SOURCE}/libavcodec/cbs_sei.c"
grep -Fq 'if ((ret = init_get_bits8(&gb, buf, buf_size)) < 0)' "${FFMPEG_SOURCE}/libavcodec/g726.c"
grep -q 'ptr_linesize = linesize' "${FFMPEG_SOURCE}/libavcodec/gif.c"
grep -q 'ptrdiff_t' "${FFMPEG_SOURCE}/libavcodec/sgienc.c"
grep -q 'av_size_mult(s->current_frame.nb_units' "${FFMPEG_SOURCE}/libavcodec/vvc/refs.c"
grep -q 'memcpy(lut, ctx->c23lut, sizeof(lut))' "${FFMPEG_SOURCE}/libavcodec/sanm.c"
grep -q 'Unsupported bit depth' "${FFMPEG_SOURCE}/libavcodec/dpxenc.c"
grep -q 'src->width != avctx->width' "${FFMPEG_SOURCE}/libavcodec/encode.c"
grep -q 's->block_len > 1024 \* 1024' "${FFMPEG_SOURCE}/libavcodec/interplayacm.c"
grep -q 'redirects++ >= 8' "${FFMPEG_SOURCE}/libavformat/rtsp.c"
grep -q 'ff_format_check_set_url' "${FFMPEG_SOURCE}/libavformat/rtsp.c"
'''
path.write_text(text.replace(anchor, insert + anchor), encoding="utf-8")
print("Prepared pinned FFmpeg security backports.")
PY

bash -n "$kit_root/scripts/android/fribidi.sh"
bash -n "$kit_root/scripts/android/ffmpeg.sh"
bash -n "$source_file"
