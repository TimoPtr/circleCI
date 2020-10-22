## Generate Weight files key and crypt binaries files

The idea is that we generate a random AES key and IV we obfuscate this AES key with KolibreeGuard which
will give you the encrypted KEY and the IV used (keep this in string in the SDK).

Next we encrypt the binaries with the random AES key and IV. To do this follow this instructions :

1. Put the binaries into the `weight_files` folder with names without `.` in it.

2. Run inside this gradle module:
```
./gradlew generateEncryptedWeightFiles
```
3. The error in the console or the report contains the IV value and the Key generated which should be keep 
in the SDK. The binaries encrypted and IV are store in `weight_files_enc` folder.

## How to use the encrypted binaries

To reveal the file you will have to reveal the AES key with `KolibreeGuard.reveal(encrypted_value, iv)` once 
you have the AES key you can reveal the bytes of the binary with ``KolibreeGuard.reveal(encrypted_bin, iv_bin, AES_Key_revealed)``

## DO NOT COMMIT THE KEY

KEY Files are in the repo but empty just in order to the test to not fail. 
