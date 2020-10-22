/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.location

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.homeui.hum.R
import org.junit.Assert.assertEquals
import org.junit.Test

internal class LocationScreenTypeTest : BaseUnitTest() {

    @Test
    fun `GrantLocationPermission title resource is grant_location_permission_title`() {
        assertEquals(
            R.string.pairing_grant_location_permission_title,
            LocationScreenType.GrantLocationPermission.title
        )
    }

    @Test
    fun `GrantLocationPermission description resource is grant_location_permission_description`() {
        assertEquals(
            R.string.pairing_grant_location_permission_description,
            LocationScreenType.GrantLocationPermission.description
        )
    }

    @Test
    fun `GrantLocationPermission action resource is grant_location_permission_action`() {
        assertEquals(
            R.string.pairing_grant_location_permission_action,
            LocationScreenType.GrantLocationPermission.action
        )
    }

    @Test
    fun `EnableLocation title resource is enable_location_title`() {
        assertEquals(
            R.string.pairing_enable_location_title,
            LocationScreenType.EnableLocation.title
        )
    }

    @Test
    fun `EnableLocation description resource is enable_location_description`() {
        assertEquals(
            R.string.pairing_enable_location_description,
            LocationScreenType.EnableLocation.description
        )
    }

    @Test
    fun `EnableLocation action resource is enable_location_action`() {
        assertEquals(
            R.string.pairing_enable_location_action,
            LocationScreenType.EnableLocation.action
        )
    }
}
