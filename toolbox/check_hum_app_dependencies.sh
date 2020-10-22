#!/bin/bash
cd ../MainApp/
([[ -z $(./gradlew :app:dependencies | awk '/humReleaseRuntimeClasspath/,/^\$/' | grep static-resources-v1) ]] || (echo "V1 dependency found in HUM!" && exit 1)) &&
([[ -z $(./gradlew :app:dependencies | awk '/humReleaseCompileClasspath/,/^\$/' | grep static-resources-v1) ]] || (echo "V1 dependency found in HUM!" && exit 1))
