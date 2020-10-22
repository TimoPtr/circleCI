/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.domain.model

import com.kolibree.android.annotation.VisibleForApp

/**
 * Gathers identifiers of each supported partnerships.
 *
 * If you want to add support for new partnership, you need to add new value to this enum.
 *
 * @param partnerName unique signature of each partner; must match with the key in partnership API response
 */
@VisibleForApp
enum class Partner(val partnerName: String) {

    HEADSPACE("headspace"),
    TEST_ONLY("test_only");

    @VisibleForApp
    companion object {

        fun from(name: String): Partner = when (name) {
            HEADSPACE.partnerName -> HEADSPACE
            TEST_ONLY.partnerName -> TEST_ONLY
            else -> throw IllegalArgumentException("Unknown partner `$name`")
        }
    }
}
