#!/usr/bin/env bash
# Packs a built APK into a root module ZIP that installs it as a system app
# (Magisk 20.4+, KernelSU, APatch). Run from the repo root.
#   module/build.sh <apk> <app|priv-app> <out-dir>
set -euo pipefail
apk=$1 kind=$2 out=$3
[ -f "$apk" ] || { echo "APK not found: $apk" >&2; exit 1; }
case $kind in app|priv-app) ;; *) echo "kind must be app or priv-app" >&2; exit 1 ;; esac

here=$(cd "$(dirname "$0")" && pwd)
gradle=app/build.gradle.kts
version=$(sed -n 's/.*versionName *= *"\([^"]*\)".*/\1/p' "$gradle" | head -1)
code=$(sed -n 's/.*versionCode *= *\([0-9]*\).*/\1/p' "$gradle" | head -1)
min_api=$(sed -n 's/.*minSdk *= *\([0-9]*\).*/\1/p' "$gradle" | head -1)
. "$here/module.conf"   # NAME, ID, DESCRIPTION
[ -n "$version" ] && [ -n "$code" ] && [ -n "$min_api" ] || { echo "could not read version/minSdk from $gradle" >&2; exit 1; }

stage=$(mktemp -d); trap 'rm -rf "$stage"' EXIT
cp -r "$here/META-INF" "$stage/"
sed -e "s/@MIN_API@/$min_api/" -e "s/@NAME@/$NAME/" "$here/customize.sh" > "$stage/customize.sh"
cat > "$stage/module.prop" <<PROP
id=$ID
name=$NAME
version=v$version
versionCode=$code
author=Real3rr0r303YT
description=$DESCRIPTION (system app; Magisk, KernelSU, APatch)
PROP
mkdir -p "$stage/system/$kind/$NAME"
cp "$apk" "$stage/system/$kind/$NAME/$NAME.apk"
# Privileged apps need their privileged permissions allowlisted, or Android refuses to boot.
if [ "$kind" = priv-app ]; then
  [ -d "$here/system" ] || { echo "priv-app needs module/system/etc/permissions/privapp-permissions-*.xml" >&2; exit 1; }
  cp -r "$here/system/." "$stage/system/"
fi

mkdir -p "$out"
zip_path=$(cd "$out" && pwd)/$NAME-v$version-module.zip
rm -f "$zip_path"
(cd "$stage" && zip -qr9 "$zip_path" .)
# Self-check: the files every root manager needs are in the ZIP.
for f in module.prop customize.sh META-INF/com/google/android/update-binary "system/$kind/$NAME/$NAME.apk"; do
  unzip -l "$zip_path" "$f" >/dev/null || { echo "missing $f in $zip_path" >&2; exit 1; }
done
echo "$zip_path"
