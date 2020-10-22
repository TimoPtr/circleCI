/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.models

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.synchronizator.Bundle

typealias BundleCreatorSet = Set<@JvmSuppressWildcards BundleCreator>

/**
 * Represents instances that encapsulate the logic to create a [Bundle]
 */
@VisibleForApp
interface BundleCreator {
    fun create(): Bundle
}
