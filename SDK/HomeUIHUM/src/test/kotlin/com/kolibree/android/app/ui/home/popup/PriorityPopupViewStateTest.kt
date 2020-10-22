/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup

import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.popup.DisplayedItem.Bluetooth
import com.kolibree.android.app.ui.home.popup.DisplayedItem.Location
import com.kolibree.android.app.ui.home.popup.DisplayedItem.LocationPermissionError
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.homeui.hum.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PriorityPopupViewStateTest : BaseUnitTest() {

    @Test
    fun `viewState set-up correctly the SnackbarConfig if item is Location`() {
        val viewState = SnackbarsPriorityDisplayViewState().withDisplayedItem(Location)

        val expectedConfig = SnackbarConfiguration(
            isShown = true,
            error = Error.from(
                messageId = R.string.home_location_unavailable_snackbar,
                buttonTextId = R.string.home_snackbar_enable_button
            )
        )

        assertEquals(expectedConfig, viewState.snackbarConfiguration)
    }

    @Test
    fun `viewState set-up correctly the SnackbarConfig if item is Bluetooth`() {
        val viewState = SnackbarsPriorityDisplayViewState().withDisplayedItem(Bluetooth)

        val expectedConfig = SnackbarConfiguration(
            isShown = true,
            error = Error.from(
                messageId = R.string.home_bluetooth_unavailable_snackbar,
                buttonTextId = R.string.home_snackbar_enable_button
            )
        )

        assertEquals(expectedConfig, viewState.snackbarConfiguration)
    }

    @Test
    fun `viewState set-up correctly the SnackbarConfig if item is LocationPermissionError`() {
        val viewState = SnackbarsPriorityDisplayViewState().withDisplayedItem(LocationPermissionError)

        val expectedConfig = SnackbarConfiguration(
            isShown = true,
            error = Error.from(
                messageId = R.string.pairing_grant_location_permission_error,
                buttonTextId = R.string.ok
            )
        )

        assertEquals(expectedConfig, viewState.snackbarConfiguration)
    }

    @Test
    fun `viewState set-up correctly the snackbar visibility`() {
        val viewState = SnackbarsPriorityDisplayViewState().withDisplayedItem(Location)

        assertTrue(viewState.snackbarConfiguration.isShown)

        val viewStateCopy = viewState.withSnackbarShown(false)

        assertFalse(viewStateCopy.snackbarConfiguration.isShown)
    }
}
