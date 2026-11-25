#!/usr/bin/env python3
"""Create/verify immutable FFmpeg native build provenance for promotion."""
from __future__ import annotations
import argparse, hashlib, json, pathlib, subprocess
ROOT=pathlib.Path(__file__).resolve().parents[1]
FILES=["third_party/ffmpeg-kit-next/directorx-security.patch","scripts/prepare_ffmpeg_kit_next_9_source.sh"]
def digest(b): return hashlib.sha256(b.replace(b"\r\n",b"\n")).hexdigest()
def lock_digest(lock):
 return hashlib.sha256(json.dumps(lock,sort_keys=True,separators=(",",":"),ensure_ascii=False).encode("utf-8")).hexdigest()
def show(rev,path):
 try:
  return subprocess.check_output(["git","show",f"{rev}:{path}"],cwd=ROOT,stderr=subprocess.PIPE)
 except subprocess.CalledProcessError as exc:
  detail=exc.stderr.decode("utf-8",errors="replace").strip()
  raise SystemExit(f"required provenance input {path!r} is unavailable at revision {rev}: {detail}") from None
def main():
 p=argparse.ArgumentParser(); p.add_argument("--create",action="store_true"); p.add_argument("--verify",action="store_true"); p.add_argument("--revision",required=True); p.add_argument("--run-id",required=True); p.add_argument("--built-at",default=""); p.add_argument("--manifest",default="build-output/native-build-provenance.json"); p.add_argument("--aar"); p.add_argument("--pom"); a=p.parse_args()
 if a.create==a.verify: raise SystemExit("choose exactly one of --create or --verify")
 if len(a.revision)!=40 or any(c not in "0123456789abcdef" for c in a.revision): raise SystemExit("invalid source revision")
 if not a.run_id.isdigit(): raise SystemExit("invalid native workflow run ID")
 entries={}
 for rel in FILES:
  committed=show(a.revision,rel)
  working_path=ROOT/rel
  if not working_path.is_file(): raise SystemExit(f"required provenance input is missing from the working tree: {rel}")
  working=working_path.read_bytes()
  if digest(committed)!=digest(working): raise SystemExit(f"source checkout mismatch for {rel}")
  entries[rel]=digest(committed)
 lock=json.loads((ROOT/"third_party/ffmpeg-kit-next/native-lock.json").read_text(encoding="utf-8"))
 if lock["source"]["commit"]!="9f04bf1e3c97def70b98e198cb80a57b43ef5b45" or lock["source"]["ffmpegRevision"]!="946fcce07b6dcd0331c8cc609192aeff5e1924f8": raise SystemExit("current native lock does not pin the promoted FFmpeg 9.0.2 sources")
 if lock["source"]["securityPatchSha256"]!=entries["third_party/ffmpeg-kit-next/directorx-security.patch"]: raise SystemExit("native lock security patch digest mismatch")
 if lock["build"]["abis"]!=["armeabi-v7a","armeabi-v7a-neon","arm64-v8a","x86","x86_64"]: raise SystemExit("native lock ABI inventory mismatch")
 lock_sha=lock_digest(lock)
 path=pathlib.Path(a.manifest)
 aar=pathlib.Path(a.aar) if a.aar else path.parent/"ffmpeg-kit-next-9.0.2.aar"; pom=pathlib.Path(a.pom) if a.pom else path.parent/"ffmpeg-kit-next-9.0.2.pom"
 if not aar.is_file() or not pom.is_file(): raise SystemExit("assembled AAR/POM missing for provenance binding")
 artifact_hashes={"aarSha256":hashlib.sha256(aar.read_bytes()).hexdigest(),"pomSha256":hashlib.sha256(pom.read_bytes()).hexdigest()}
 if artifact_hashes["aarSha256"]!=lock["artifact"]["sha256"] or artifact_hashes["pomSha256"]!=lock["artifact"]["pomSha256"]:
  raise SystemExit("assembled AAR/POM does not match the fully reviewed native lock")
 if a.create:
  data={"schemaVersion":1,"runId":a.run_id,"headSha":a.revision,"builtAt":a.built_at,"files":entries,"wrapperCommit":"9f04bf1e3c97def70b98e198cb80a57b43ef5b45","ffmpegCommit":"946fcce07b6dcd0331c8cc609192aeff5e1924f8","nativeLockSha256":lock_sha,**artifact_hashes}
  path.parent.mkdir(parents=True,exist_ok=True); path.write_text(json.dumps(data,indent=2,sort_keys=True)+"\n",encoding="utf-8"); print("Created source-bound native provenance manifest"); return
 data=json.loads(path.read_text(encoding="utf-8"))
 expected={"schemaVersion":1,"runId":a.run_id,"headSha":a.revision,"files":entries,"wrapperCommit":"9f04bf1e3c97def70b98e198cb80a57b43ef5b45","ffmpegCommit":"946fcce07b6dcd0331c8cc609192aeff5e1924f8","nativeLockSha256":lock_sha,**artifact_hashes}
 for k,v in expected.items():
  if data.get(k)!=v: raise SystemExit(f"native provenance mismatch: {k}")
 if not data.get("builtAt"): raise SystemExit("native provenance missing builtAt")
 print("Verified native source revision, complete native-lock digest, advisories, wrapper/FFmpeg pins, security patch, build recipe and AAR/POM")
if __name__=="__main__": main()
