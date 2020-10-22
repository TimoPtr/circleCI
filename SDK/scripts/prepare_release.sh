#! /usr/bin/env bash

# This script should only be run from the root directory of the git repository which
# currently contains the gradle project.

tmp_dir='maven_local'
release_dir='release'
demo_app='../SdkDemoApp'
doc_pdf='SDK_ANDROID.pdf'

cleanup () {
  rm -rf "$tmp_dir"
  rm -rf "$release_dir"
}

# Add Proguard mapping files to the release bundle
for project in */ ; do
  projectMapping="$project"/build/outputs/mapping/release/
  flavorProjectMapping="$project"/build/outputs/mapping/colgate/release/
  mkdir -p "$release_dir"/mappings/"$project"

  if [[ -e "$projectMapping" ]]; then
    cp "$projectMapping/"* "$release_dir"/mappings/"$project"
  fi

  if [[ -e "$flavorProjectMapping" ]]; then
    cp "$flavorProjectMapping/"* "$release_dir"/mappings/"$project"
  fi
done

version=$(./gradlew properties -q | grep "versions" | grep -o 'name=[^,]*' | cut -d"=" -f2)
./gradlew publishToMavenLocal -Dmaven.repo.local="$tmp_dir"/
mkdir -p "$release_dir" && tar -cvf "$release_dir"/android_sdk.tar -C "$tmp_dir"/com/kolibree/ android/
echo "$version" > "$release_dir"/version.txt
rm -rf "$release_dir"/android/
cp CHANGELOG.md "$release_dir" || { cleanup ; exit 1; }
cp "$doc_pdf" "$release_dir" || { cleanup ; exit 1; }

(cd "$demo_app" && ./scripts/prepare_release.sh) || { cleanup; exit 1; }
rm -rf "$demo_app"/scripts/
rm -rf "$demo_app"/build/
rm -rf "$demo_app"/app/build/
(sed -i.bak "/module.version=/ s/=.*/=$version/" "$demo_app"/gradle.properties && rm "$demo_app"/gradle.properties.bak) || { cleanup ; exit 1; }
cp "$demo_app"/build.gradle /tmp/
cp "$demo_app"/gradle.properties /tmp/
git checkout -- "$demo_app"/build.gradle
git checkout -- "$demo_app"/gradle.properties
tar -cvf "$release_dir"/demoapp.tar "$demo_app"
mv /tmp/build.gradle "$demo_app"/build.gradle
mv /tmp/gradle.properties "$demo_app"/gradle.properties

tar -cvf release_"$version".tar "$release_dir"
cleanup
