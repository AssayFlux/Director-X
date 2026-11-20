[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$SourceDirectory,

    [ValidateSet("n8.1.2", "n8.1.3")]
    [string]$FfmpegTag = "n8.1.2",

    [switch]$PackageOnly
)

$expectedKitCommit = "3e223118e6e8fb6208693ecf3952e77cd096f587"
$sourcePath = (Resolve-Path -LiteralPath $SourceDirectory -ErrorAction Stop).Path
$kitScript = Join-Path $sourcePath "nix-android.sh"
if (-not (Test-Path -LiteralPath $kitScript -PathType Leaf)) {
    throw "FFmpegKitNext source directory is missing nix-android.sh: $sourcePath"
}

$actualKitCommit = (& git -C $sourcePath rev-parse HEAD 2>$null).Trim()
if ($LASTEXITCODE -ne 0 -or $actualKitCommit -ne $expectedKitCommit) {
    throw "FFmpegKitNext commit must be $expectedKitCommit; found '$actualKitCommit'."
}

$patchPath = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\third_party\ffmpeg-kit-next\directorx-security.patch"
    ) -ErrorAction Stop).Path
$candidatePinPatchPath = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\third_party\ffmpeg-kit-next\ffmpeg-source-tag-n8.1.3.patch"
    ) -ErrorAction Stop).Path

function ConvertTo-WslPath([string]$value) {
    $shellValue = $value.Replace("'", "'\''")
    $translated = (& wsl.exe bash -lc "wslpath -a '$shellValue'").Trim()
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($translated)) {
        throw "Could not translate the Windows path into WSL: $value"
    }
    return $translated
}

function ConvertTo-WslShellLiteral([string]$value) {
    return "'" + $value.Replace("'", "'\''") + "'"
}

$sourceWsl = ConvertTo-WslPath $sourcePath
$patchWsl = ConvertTo-WslPath $patchPath
$sourceLiteral = ConvertTo-WslShellLiteral $sourceWsl
$patchLiteral = ConvertTo-WslShellLiteral $patchWsl

$candidatePinPatchWsl = ConvertTo-WslPath $candidatePinPatchPath
$candidatePinPatchLiteral = ConvertTo-WslShellLiteral $candidatePinPatchWsl
if ($FfmpegTag -eq "n8.1.3") {
    $sourceTagPatchBlock = @"
if git apply --reverse --check $candidatePinPatchLiteral >/dev/null 2>&1; then
  :
elif git apply --check $candidatePinPatchLiteral; then
  git apply $candidatePinPatchLiteral
else
  echo "Director X FFmpeg 8.1.3 source-pin patch does not apply cleanly." >&2
  exit 3
fi
"@
} else {
    $sourceTagPatchBlock = @"
if git apply --reverse --check $candidatePinPatchLiteral >/dev/null 2>&1; then
  git apply --reverse $candidatePinPatchLiteral
fi
"@
}

$skipFfmpeg = if ($PackageOnly) { " --skip-ffmpeg" } else { "" }
$command = @"
set -e
cd $sourceLiteral
git config core.autocrlf false
git ls-files -z | while IFS= read -r -d '' path; do
  case "`$path" in
    *.sh|*.nix|*.mk|*.cmake|*.m4|*.ac|*.in|*.gradle|*.properties|*.txt)
      sed -i 's/\r`$//' "`$path"
      ;;
  esac
done
if git apply --reverse --check $patchLiteral >/dev/null 2>&1; then
  :
elif git apply --check $patchLiteral; then
  git apply $patchLiteral
else
  echo "Director X FFmpeg security patch does not apply cleanly." >&2
  exit 2
fi
$sourceTagPatchBlock
bash ./nix-android.sh -p android-r27d$skipFfmpeg --enable-libass --enable-libjxl --enable-android-media-codec --enable-android-zlib --enable-openh264 --jobs=8
"@

& wsl.exe bash -lc $command
if ($LASTEXITCODE -ne 0) {
    throw "FFmpegKitNext build failed with exit code $LASTEXITCODE."
}
