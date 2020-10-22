/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto.signing

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
internal object ApkFingerprintConstants {

    const val DIGEST_ALGORITHM = "SHA-1"

    /*
     * To whitelist new keystore, run `keytool -list -keystore [path/to/keystore]` and provide keystore password.
     *
     * In the output you'll see:
     * Certificate fingerprint (SHA1): [THIS IS THE SHA-1 FINGERPRINT OF KEYSTORE THAT YOU WANT TO WHITELIST]
     */
    const val DEVELOPMENT_DEBUG = "7A:80:48:BA:81:F2:1C:A4:20:17:C7:86:BA:3A:8E:97:93:01:AE:2C"
    const val KOLIBREE_BETA = "4A:DA:F4:14:78:E1:5A:CF:92:E0:98:52:3B:65:87:D7:06:B4:34:AE"
}
