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

new = "./$(gen_unicode_version) unidata/ReadMe.txt unidata/BidiMirroring.txt > $@"
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
