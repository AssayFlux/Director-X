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
anchor = "# REGENERATE BUILD FILES IF NECESSARY OR REQUESTED"
if text.count(anchor) != 1:
    raise SystemExit("Expected exactly one FriBidi build-regeneration anchor; refusing an ambiguous edit.")
insert = '''# Director X build correction for FriBidi v1.0.16:
# Automake's $^ includes the generator executable itself as an input file.
# Pass only the two Unicode data files to avoid parsing the executable.
FRIBIDI_MAKEFILE_AM="${BASEDIR}/src/${LIB_NAME}/gen.tab/Makefile.am"
python3 - "${FRIBIDI_MAKEFILE_AM}" <<'FRIBIDI_PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")
old = "./$(gen_unicode_version) $^ > $@"
new = "python3 ./gen-unicode-version.py unidata/ReadMe.txt > $@"
dependency = "\\t\\t$(gen_unicode_version)\\n"
if text.count(dependency) != 1:
    raise SystemExit("Expected exactly one generated executable prerequisite; refusing an ambiguous edit.")
text = text.replace(dependency, "")
continued_dependency = "\\t\\tunidata/BidiMirroring.txt \\\\n"
plain_dependency = "\\t\\tunidata/BidiMirroring.txt\\n"
if text.count(continued_dependency) == 1:
    text = text.replace(continued_dependency, plain_dependency)
elif text.count(plain_dependency) != 1:
    raise SystemExit("Expected exactly one final FriBidi data dependency; refusing an ambiguous edit.")
if text.count(old) == 1:
    text = text.replace(old, new)
elif text.count(new) == 1:
    print("FriBidi Unicode version rule already uses the bounded Python generator.")
else:
    raise SystemExit("Unexpected FriBidi Unicode generator recipe; refusing an ambiguous patch.")
path.write_text(text, encoding="utf-8")

generator = """#!/usr/bin/env python3
import pathlib
import re
import sys

source = pathlib.Path(sys.argv[1]).read_text(encoding="utf-8")
match = re.search(r"\\bVersion\\s+(\\d+)\\.(\\d+)\\.(\\d+)\\b", source)
if not match:
    raise SystemExit("Could not determine Unicode version from pinned FriBidi ReadMe.txt")
major, minor, micro = map(int, match.groups())
print("/* fribidi-unicode-version.h: generated from pinned Unicode ReadMe.txt */")
print(f'#define FRIBIDI_UNICODE_VERSION "{major}.{minor}.{micro}"')
print(f"#define FRIBIDI_UNICODE_MAJOR_VERSION {major}")
print(f"#define FRIBIDI_UNICODE_MINOR_VERSION {minor}")
print(f"#define FRIBIDI_UNICODE_MICRO_VERSION {micro}")
"""
(path.parent / "gen-unicode-version.py").write_text(generator, encoding="utf-8")

FRIBIDI_PY

'''
path.write_text(text.replace(anchor, insert + anchor), encoding="utf-8")
print("Prepared the pinned FriBidi generator correction.")
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
FFMPEG_SECURITY_COMMITS=(
  969c7eb091989a664cb7bf66a1f02051cc46cb52
  d1434518fbd03789e3e5dc0657a7a9f1123ce074
  71403a21b02a92b925e26e609e9bd3e48df451cf
  2c2f6e96e31795ae95a8f8323a493ffbc493111f
  ed27bfcbbbc0872c0195eaf4dd2c0c93b9f1778e
  2c515eea07df719379e7f73deeeb7ccb8768433f
  26c2980980295e5516f308c5a2792d2a72e43eae
  c6e5b6105aa13f6b0df2a646da608078cfe52d79
  053660fdb0d2403b838690689c47ad7685c3b10b
  d42cb1925987cc27899441ce39fa7bd2647dc72e
  aa5fd7d287db4f469fdcbe444f7f9f38c8c289c7
  9b99973b2bbecee0b7e1e57035b95498b42f54a6
  6ecaddb9c1d59cf7f5de16017122fecb2fa0c67c
)
for fix in "${FFMPEG_SECURITY_COMMITS[@]}"; do
  if ! git -C "${FFMPEG_SOURCE}" cat-file -e "${fix}^{commit}" 2>/dev/null; then
    git -C "${FFMPEG_SOURCE}" fetch --no-tags origin "${fix}"
  fi
  if git -C "${FFMPEG_SOURCE}" merge-base --is-ancestor "${fix}" HEAD; then
    continue
  fi
  git -C "${FFMPEG_SOURCE}" show --format= "${fix}" | git -C "${FFMPEG_SOURCE}" apply --check -
  git -C "${FFMPEG_SOURCE}" show --format= "${fix}" | git -C "${FFMPEG_SOURCE}" apply -
done
git -C "${FFMPEG_SOURCE}" diff --check
grep -q 'while (info)' "${FFMPEG_SOURCE}/libavutil/encryption_info.c"
grep -q 'av_encryption_init_info_append_side_data' "${FFMPEG_SOURCE}/libavutil/encryption_info.c"
grep -q 'pssh_side_data' "${FFMPEG_SOURCE}/libavformat/mov.c"
grep -q 'if (!fromlen)' "${FFMPEG_SOURCE}/libavutil/avstring.c"
grep -Fq 'memset(&list->messages[list->nb_messages]' "${FFMPEG_SOURCE}/libavcodec/cbs_sei.c"
grep -q 'ptr_linesize = linesize' "${FFMPEG_SOURCE}/libavcodec/gif.c"
grep -q 'ptrdiff_t' "${FFMPEG_SOURCE}/libavcodec/sgienc.c"
grep -q 'av_size_mult(s->current_frame.nb_units' "${FFMPEG_SOURCE}/libavcodec/vvc/refs.c"
grep -q 'memcpy(lut, ctx->c23lut, 256)' "${FFMPEG_SOURCE}/libavcodec/sanm.c"
grep -q 'Unsupported bit depth' "${FFMPEG_SOURCE}/libavcodec/dpxenc.c"
grep -q 'src->width != avctx->width' "${FFMPEG_SOURCE}/libavcodec/encode.c"
grep -q 's->block_len > 1024 \* 1024' "${FFMPEG_SOURCE}/libavcodec/interplayacm.c"
'''
path.write_text(text.replace(anchor, insert + anchor).rstrip() + "\n", encoding="utf-8")
print("Prepared pinned FFmpeg security backports.")
PY

bash -n "$source_file"
