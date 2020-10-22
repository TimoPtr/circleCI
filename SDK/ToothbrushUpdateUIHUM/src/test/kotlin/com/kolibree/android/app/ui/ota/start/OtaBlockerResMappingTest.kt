/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.start

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.ota.R
import com.kolibree.android.toothbrushupdate.OtaUpdateBlocker
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class OtaBlockerResMappingTest : BaseUnitTest() {

    @Test
    fun `when no active connection it returns valid error`() {
        assertEquals(R.string.ota_blocker_not_active_connection, mapBlockerToError(OtaUpdateBlocker.CONNECTION_NOT_ACTIVE).messageId)
    }

    @Test
    fun `when E2 not on charger it returns valid error`() {
        assertEquals(R.string.ota_blocker_not_charging, mapBlockerToError(OtaUpdateBlocker.NOT_CHARGING).messageId)
    }

    @Test
    fun `when not enough battery it returns valid error`() {
        assertEquals(R.string.ota_blocker_not_enough_battery, mapBlockerToError(OtaUpdateBlocker.NOT_ENOUGH_BATTERY).messageId)
    }

    @Test
    fun `when no gruware data it returns valid error`() {
        assertEquals(R.string.ota_blocker_no_internet, mapBlockerToError(OtaUpdateBlocker.NO_GRUWARE_DATA).messageId)
    }
}
