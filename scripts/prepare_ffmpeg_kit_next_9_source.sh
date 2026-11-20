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
  if ! git -C "${ffmpeg_source_dir}" cat-file -e "${fix}^{commit}" 2>/dev/null; then
    git -C "${ffmpeg_source_dir}" fetch --no-tags origin "${fix}"
  fi
  if git -C "${ffmpeg_source_dir}" merge-base --is-ancestor "${fix}" HEAD; then
    continue
  fi
  git -C "${ffmpeg_source_dir}" show --format= "${fix}" | git -C "${ffmpeg_source_dir}" apply --check -
  git -C "${ffmpeg_source_dir}" show --format= "${fix}" | git -C "${ffmpeg_source_dir}" apply -
done
git -C "${ffmpeg_source_dir}" diff --check
grep -q 'while (info)' "${ffmpeg_source_dir}/libavutil/encryption_info.c"
grep -q 'av_encryption_init_info_append_side_data' "${ffmpeg_source_dir}/libavutil/encryption_info.c"
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
