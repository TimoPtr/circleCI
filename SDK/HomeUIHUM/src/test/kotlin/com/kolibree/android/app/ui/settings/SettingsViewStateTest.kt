/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.settings.binding.BirthDateItemBindingModel
import com.kolibree.android.app.ui.settings.binding.BrusherDetailsSettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.FirstNameSettingsDetailItemBindingModel
import com.kolibree.android.app.ui.settings.binding.GenderItemBindingModel
import com.kolibree.android.app.ui.settings.binding.HandednessItemBindingModel
import com.kolibree.android.app.ui.settings.binding.LinkAccountBindingModel
import com.kolibree.android.app.ui.settings.binding.ShareYourDataBindingModel
import com.kolibree.android.app.ui.settings.binding.VibrationLevelsItemBindingModel
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.test.mocks.ProfileBuilder
import io.kotlintest.matchers.collections.shouldBeEmpty
import java.util.Locale
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class SettingsViewStateTest : BaseUnitTest() {

    private val profile: Profile = ProfileBuilder.create()
        .withId(1L)
        .withName("NAME")
        .withGender(Gender.MALE)
        .withAge(32)
        .withTargetBrushingTime(100)
        .withHandednessLeft()
        .withCountry(Locale.FRANCE.isO3Country)
        .build()

    @Test
    fun `secret settings items are visible if enabled`() {
        val viewState = SettingsViewState.initial(
            isSecretSettingsEnabled = true,
            profile = profile,
            isAmazonDashAvailable = false,
            allowDisablingDataSharing = true
        )
        assertTrue(viewState.items().containsAll(SECRET_SETTINGS_ITEMS))
        assertTrue(viewState.items().containsAll(ADMIN_SETTINGS_ITEMS))
        assertTrue(viewState.items().containsAll(BOTTOM_ITEMS))
    }

    @Test
    fun `secret settings items are not visible if disabled`() {
        val viewState =
            SettingsViewState.initial(
                isSecretSettingsEnabled = false,
                profile = profile,
                isAmazonDashAvailable = false,
                allowDisablingDataSharing = true
            )
        assertFalse(viewState.items().containsAll(SECRET_SETTINGS_ITEMS))
        assertTrue(viewState.items().containsAll(ADMIN_SETTINGS_ITEMS))
        assertTrue(viewState.items().containsAll(BOTTOM_ITEMS))
    }

    @Test
    fun `view state items has correct brusher profile related items`() {
        val viewState =
            SettingsViewState.initial(
                isSecretSettingsEnabled = false,
                profile = profile,
                isAmazonDashAvailable = false,
                allowDisablingDataSharing = true
            )

        val brusherDetailItem = viewState.items()[0]
        assertTrue(
            brusherDetailItem is BrusherDetailsSettingsItemBindingModel &&
                brusherDetailItem.name == profile.firstName
        )

        val firstNameItem = viewState.items()[1]
        assertTrue(
            firstNameItem is FirstNameSettingsDetailItemBindingModel &&
                firstNameItem.name == profile.firstName
        )

        val birthDateItem = viewState.items()[2]
        assertTrue(
            birthDateItem is BirthDateItemBindingModel &&
                birthDateItem.birthDate == profile.birthday
        )

        val genderItem = viewState.items()[3]
        assertTrue(
            genderItem is GenderItemBindingModel &&
                genderItem.gender == profile.gender
        )

        val handednessItem = viewState.items()[4]
        assertTrue(
            handednessItem is HandednessItemBindingModel &&
                handednessItem.handedness == profile.handedness
        )
    }

    /*
    VibrationLevels
     */
    @Test
    fun `vibrationLevels is excluded from visibleItems if showSetting=false`() {
        val viewState =
            SettingsViewState.initial(
                isSecretSettingsEnabled = false,
                profile = profile,
                isAmazonDashAvailable = false,
                allowDisablingDataSharing = true
            )
                .copy(adminSettingsItems = listOf(VibrationLevelsItemBindingModel(showSetting = false)))

        viewState.items().filterIsInstance(VibrationLevelsItemBindingModel::class.java).single()

        assertTrue(
            viewState.visibleItems()
                .filterIsInstance(VibrationLevelsItemBindingModel::class.java)
                .isEmpty()
        )
    }

    @Test
    fun `vibrationLevels is included in visibleItems if showSetting=true`() {
        val viewState =
            SettingsViewState.initial(
                isSecretSettingsEnabled = false,
                profile = profile,
                isAmazonDashAvailable = false,
                allowDisablingDataSharing = true
            )
                .copy(adminSettingsItems = listOf(VibrationLevelsItemBindingModel(showSetting = true)))

        viewState.items().filterIsInstance(VibrationLevelsItemBindingModel::class.java).single()

        assertFalse(
            viewState.visibleItems()
                .filterIsInstance(VibrationLevelsItemBindingModel::class.java)
                .isEmpty()
        )
    }

    /*
    LinkAccount
     */
    @Test
    fun `linkAccount section is visible when available`() {
        val viewState =
            SettingsViewState.initial(
                isSecretSettingsEnabled = false,
                profile = profile,
                isAmazonDashAvailable = true,
                allowDisablingDataSharing = true
            )

        viewState.items().filterIsInstance(LinkAccountBindingModel::class.java).single()

        assertFalse(
            viewState.visibleItems()
                .filterIsInstance(LinkAccountBindingModel::class.java)
                .isEmpty()
        )
    }

    @Test
    fun `linkAccount section is not present when not available`() {
        val viewState =
            SettingsViewState.initial(
                isSecretSettingsEnabled = false,
                profile = profile,
                isAmazonDashAvailable = false,
                allowDisablingDataSharing = true
            )

        viewState.items().filterIsInstance(LinkAccountBindingModel::class.java).shouldBeEmpty()

        assertTrue(
            viewState.visibleItems()
                .filterIsInstance(LinkAccountBindingModel::class.java)
                .isEmpty()
        )
    }

    /*
     shareData
     */

    @Test
    fun `shareData section is not present when not allowed to switch`() {
        val viewState =
            SettingsViewState.initial(
                isSecretSettingsEnabled = false,
                profile = profile,
                isAmazonDashAvailable = false,
                allowDisablingDataSharing = false
            )

        viewState.items().filterIsInstance(ShareYourDataBindingModel::class.java).shouldBeEmpty()

        assertTrue(
            viewState.visibleItems()
                .filterIsInstance(ShareYourDataBindingModel::class.java)
                .isEmpty()
        )
    }

    @Test
    fun `shareData section is present when allowed to switch`() {
        val viewState =
            SettingsViewState.initial(
                isSecretSettingsEnabled = false,
                profile = profile,
                isAmazonDashAvailable = false,
                allowDisablingDataSharing = true
            )

        viewState.items().filterIsInstance(ShareYourDataBindingModel::class.java).single()

        assertTrue(
            viewState.visibleItems()
                .filterIsInstance(ShareYourDataBindingModel::class.java)
                .isNotEmpty()
        )
    }
}
