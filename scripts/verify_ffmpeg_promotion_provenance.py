#!/usr/bin/env python3
"""Create/verify immutable FFmpeg native build provenance for promotion."""
from __future__ import annotations
import argparse, hashlib, json, pathlib, subprocess
ROOT=pathlib.Path(__file__).resolve().parents[1]
FILES=["third_party/ffmpeg-kit-next/directorx-security.patch","third_party/ffmpeg-kit-next/native-lock.json","scripts/prepare_ffmpeg_kit_next_9_source.sh",".github/workflows/ffmpeg-9-native-build.yml"]
def digest(b): return hashlib.sha256(b.replace(b"\r\n",b"\n")).hexdigest()
def show(rev,path): return subprocess.check_output(["git","show",f"{rev}:{path}"],cwd=ROOT)
def main():
 p=argparse.ArgumentParser(); p.add_argument("--create",action="store_true"); p.add_argument("--verify",action="store_true"); p.add_argument("--revision",required=True); p.add_argument("--run-id",required=True); p.add_argument("--built-at",default=""); p.add_argument("--manifest",default="build-output/native-build-provenance.json"); a=p.parse_args()
 if a.create==a.verify: raise SystemExit("choose exactly one of --create or --verify")
 if len(a.revision)!=40 or any(c not in "0123456789abcdef" for c in a.revision): raise SystemExit("invalid source revision")
 if not a.run_id.isdigit(): raise SystemExit("invalid native workflow run ID")
 entries={}
 for rel in FILES:
  committed=show(a.revision,rel); working=(ROOT/rel).read_bytes()
  if digest(committed)!=digest(working): raise SystemExit(f"source checkout mismatch for {rel}")
  entries[rel]=digest(committed)
 path=pathlib.Path(a.manifest)
 if a.create:
  data={"schemaVersion":1,"runId":a.run_id,"headSha":a.revision,"builtAt":a.built_at,"files":entries,"wrapperCommit":"9f04bf1e3c97def70b98e198cb80a57b43ef5b45","ffmpegCommit":"946fcce07b6dcd0331c8cc609192aeff5e1924f8"}
  path.parent.mkdir(parents=True,exist_ok=True); path.write_text(json.dumps(data,indent=2,sort_keys=True)+"\n",encoding="utf-8"); print("Created source-bound native provenance manifest"); return
 data=json.loads(path.read_text(encoding="utf-8"))
 expected={"schemaVersion":1,"runId":a.run_id,"headSha":a.revision,"files":entries,"wrapperCommit":"9f04bf1e3c97def70b98e198cb80a57b43ef5b45","ffmpegCommit":"946fcce07b6dcd0331c8cc609192aeff5e1924f8"}
 for k,v in expected.items():
  if data.get(k)!=v: raise SystemExit(f"native provenance mismatch: {k}")
 if not data.get("builtAt"): raise SystemExit("native provenance missing builtAt")
 print("Verified native source revision, wrapper/FFmpeg pins, lock, security patch and build recipe")
if __name__=="__main__": main()
