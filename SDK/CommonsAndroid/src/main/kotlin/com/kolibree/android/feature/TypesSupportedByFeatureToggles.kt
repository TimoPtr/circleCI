/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature

internal val typesSupportedByFeatureToggles = listOf(
    Boolean::class,
    Long::class,
    String::class
    // TODO add support for single-choice enums https://kolibree.atlassian.net/browse/KLTB002-9495
    // TODO add support for multi-choice enums https://kolibree.atlassian.net/browse/KLTB002-9496
)

internal fun <T : Any> FeatureToggle<T>.checkIfFeatureTypeIsSupported() {
    check(typesSupportedByFeatureToggles.contains(feature.type())) {
        "${feature.type().java.canonicalName} is not supported by feature toggle mechanism.\n" +
            "Currently supported types: ${printSupportedTypes()}"
    }
}

private fun printSupportedTypes(): String {
    return "[${typesSupportedByFeatureToggles.joinToString(", ") { it.java.canonicalName!! }}]"
}
