# What is it?
Contains PirateActivity and the unity binaries to run the Pirate game

## Integration in a project
Use the provided Dagger _PirateModule_ class
See root README.md for module usage

## Integrate a Unity export

If you want to integrate a new version of the Pirate game, you need to integrate the Unity export into the app project.

To integrate an export:

* Remove content of libs, src/main/jniLibs and src/main/assets
* Copy the contents of the .zip into the appropriate folder. (libs, src/main/jniLibs, src/main/assets)

## Troubleshooting

- Pirate is not supported in Android 5, it shows a black screen. We don't know the reason.
- PirateActivity has a weird lifecycle. Read its documentation.
- Mathieu Guillaume is the Unity developer. He provides Pirate binaries.
