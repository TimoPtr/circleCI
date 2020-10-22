/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto.signing

internal enum class ApkFingerprint(
    val loggingAvailable: Boolean,
    val testFeaturesAllowed: Boolean,
    val auditAvailable: Boolean
) {
    DEBUG(loggingAvailable = true, testFeaturesAllowed = true, auditAvailable = false),
    BETA(loggingAvailable = true, testFeaturesAllowed = true, auditAvailable = true),
    PRODUCTION(loggingAvailable = false, testFeaturesAllowed = false, auditAvailable = true);
}
