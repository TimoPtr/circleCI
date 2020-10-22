/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto

import com.kolibree.crypto.signing.ApkFingerprint
import com.kolibree.crypto.signing.ApkFingerprintChecker
import javax.inject.Inject

internal class SecurityKeeperImpl @Inject constructor(fingerprintChecker: ApkFingerprintChecker) : SecurityKeeper {

    private val fingerprint: ApkFingerprint = fingerprintChecker.fingerprint

    override val isLoggingAllowed: Boolean by lazy {
        return@lazy fingerprint.loggingAvailable
    }

    override val isAuditAllowed: Boolean by lazy {
        return@lazy fingerprint.auditAvailable
    }

    override val testFeaturesAllowed: Boolean by lazy {
        return@lazy fingerprint.testFeaturesAllowed
    }
}
