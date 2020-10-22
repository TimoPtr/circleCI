#! /usr/bin/env bash

rm buildscripts/dependencies.gradle
cp ../SDK/dependencies.gradle buildscripts/dependencies.gradle
rm buildscripts/post_settings.gradle
mv buildscripts/post_settings_ci.gradle buildscripts/post_settings.gradle
rm settings.gradle
mv settings_ci.gradle settings.gradle

rm .gitignore
