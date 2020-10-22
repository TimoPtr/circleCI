/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.core.capabilities

import androidx.annotation.Keep

@Keep
const val ACCEPT_CAPABILITIES = "accept-capabilities"

private const val COMMA_SEPARATOR = ","

@Keep
abstract class AcceptCapabilitiesHeaderProvider {

    val capabilities: Pair<String, String>
        get() = ACCEPT_CAPABILITIES to capabilityValues.joinToString(COMMA_SEPARATOR)

    protected abstract val capabilityValues: List<String>
}
