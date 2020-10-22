#!/bin/bash
if [ -z "$1" ]; then
  echo "Please put AVD name as an argument, for ex. ./execute_espresso.sh CI_AVD"
  exit 1
fi
cd ..

#2nd parameter specifies the test class to run
if [ -z "$2" ]; then
  specificTest=""
else
  specificTest="-PmarathonClassName="$2
fi

adb kill-server | true
killall adb | true
adb start-server
$ANDROID_HOME/emulator/emulator-headless -avd $1 -netfast -no-audio -no-snapshot-load -no-snapshot-save -wipe-data > /dev/null 2>&1 &
echo "Wait for emulator-headless to boot up..."
adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'
echo "Done, let's go!"
./gradlew :app:marathonHumDebugAndroidTest ${specificTest}
adb devices | grep emulator | cut -f1 | while read line; do adb -s $line emu kill; done
