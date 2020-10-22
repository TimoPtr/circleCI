/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.toothbrushupdate.R
import org.junit.Assert.assertEquals
import org.junit.Test

/** [OtaUpdateViewState] unit tests */
class OtaUpdateViewStateTest : BaseUnitTest() {

    val otaUpdateViewStateRechageable = OtaUpdateViewState.init(false, true)
    val otaUpdateViewStateNonRechargeable = OtaUpdateViewState.init(false, false)

    @Test
    fun `init returns initial state`() {
        val initialState = OtaUpdateViewState.init(false, true)
        assertEquals(initialState, OtaUpdateViewState(false, true, OTA_ACTION_NONE, PERCENTAGE_UNDEFINED, null, 0, false, false, false, false))
    }

    @Test
    fun `use rechargeable welcome message when toothbrush is rechargeable`() {
        assertEquals(
            otaUpdateViewStateRechageable.empty().message,
            R.string.firmware_upgrade_welcome)
    }

    @Test
    fun `use non-rechargeable welcome message when toothbrush is non-rechargeable`() {
        assertEquals(
            otaUpdateViewStateNonRechargeable.empty().message,
            R.string.firmware_upgrade_welcome_nonrechargeable)
    }

    @Test
    fun `with checkingPrerequisite, isProgressVisible is true`() {
        assertEquals(
            otaUpdateViewStateRechageable.checkingPrerequisite().isProgressVisible,
            true
        )
    }

    @Test
    fun `with checkPrerequisiteComplete, isProgressVisible is false`() {
        assertEquals(
            otaUpdateViewStateRechageable.checkPrerequisiteComplete().isProgressVisible,
            false
        )
    }

    @Test
    fun `with blockedNotCharging, isProgressVisible is false and needChargingDialog is true`() {
        val blockedNotChargingState = otaUpdateViewStateRechageable.blockedNotCharging()
        assertEquals(blockedNotChargingState.isProgressVisible, false)
        assertEquals(blockedNotChargingState.showNeedChargingDialog, true)
    }
}
