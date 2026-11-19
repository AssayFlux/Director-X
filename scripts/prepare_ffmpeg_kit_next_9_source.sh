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
old = "\\t$(AM_V_GEN) ./$(gen_unicode_version) $^ > $@ \\\\n"
new = "\\t$(AM_V_GEN) ./$(gen_unicode_version) unidata/ReadMe.txt unidata/BidiMirroring.txt > $@ \\\\n"
if text.count(old) == 1:
    path.write_text(text.replace(old, new), encoding="utf-8")
elif text.count(new) == 1:
    print("FriBidi Unicode generator recipe already corrected.")
else:
    raise SystemExit("Unexpected FriBidi Unicode generator recipe; refusing an ambiguous patch.")
FRIBIDI_PY

'''
path.write_text(text.replace(anchor, insert + anchor), encoding="utf-8")
print("Prepared the pinned FriBidi generator correction.")
PY

bash -n "$source_file"
