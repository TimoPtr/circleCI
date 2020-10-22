#!/bin/bash
if [ -z "$1" ]; then
  echo "Please put AVD name as an argument, for ex. ./create_new_espresso_emulator.sh CI_AVD"
  exit 1
fi

rm -rf ~/$HOME/.android/avd/$1.ini
rm -rf ~/$HOME/.android/avd/$1.avd/

#TODO handle case for Nexus S with Android 5
#$ANDROID_HOME/tools/bin/avdmanager create avd --name $1 \
#--abi google_apis/x86 --package "system-images;android-21;google_apis;x86" --device "Nexus S"

$ANDROID_HOME/tools/bin/avdmanager create avd --name $1 \
--abi google_apis/x86 --package "system-images;android-27;google_apis;x86" --device "Nexus 5" --sdcard 2048M
echo "hw.ramSize=1024" >> $HOME/.android/avd/$1.avd/config.ini
echo "disk.dataPartition.size=4096M" >> $HOME/.android/avd/$1.avd/config.ini
echo "vm.heapSize=256" >> $HOME/.android/avd/$1.avd/config.ini
echo "sdcard.size=4096M" >> $HOME/.android/avd/$1.avd/config.ini
echo "hw.lcd.density=480" >> $HOME/.android/avd/$1.avd/config.ini
echo "hw.lcd.height=1920" >> $HOME/.android/avd/$1.avd/config.ini
echo "hw.lcd.width=1080" >> $HOME/.android/avd/$1.avd/config.ini

echo ""
echo ""
echo "New AVD $1 was created! Waiting for boot up to complete..."
echo ""
echo ""
$ANDROID_HOME/emulator/emulator -avd $1 -netfast -no-snapshot-load -no-snapshot-save -wipe-data > /dev/null 2>&1 &
adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'
echo "Done!"
adb devices | grep emulator | cut -f1 | while read line; do adb -s $line emu kill; done

echo "Config:"
cat $HOME/.android/avd/$1.avd/config.ini
