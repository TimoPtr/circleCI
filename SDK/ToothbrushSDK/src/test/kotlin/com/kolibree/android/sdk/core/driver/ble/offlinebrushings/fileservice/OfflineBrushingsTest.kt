/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.MIN_BRUSHING_DURATION_SECONDS
import com.kolibree.android.test.mocks.createOfflineBrushing
import org.junit.Assert
import org.junit.Test

internal class OfflineBrushingsTest : BaseUnitTest() {

    @Test
    fun valueOf_MIN_RECORD_DURATION_SECONDS() {
        Assert.assertEquals(10, MIN_BRUSHING_DURATION_SECONDS)
    }

    @Test
    fun isValid_moreThanMinDurationReturnsTrue() {
        Assert.assertTrue(
            createOfflineBrushing(MIN_BRUSHING_DURATION_SECONDS + 1).isValid()
        )
    }

    @Test
    fun isValid_minDurationReturnsTrue() {
        Assert.assertTrue(
            createOfflineBrushing(MIN_BRUSHING_DURATION_SECONDS).isValid()
        )
    }

    @Test
    fun isValid_lessThanMinDurationReturnsFalse() {
        Assert.assertFalse(
            createOfflineBrushing(MIN_BRUSHING_DURATION_SECONDS - 1).isValid()
        )
    }
}
