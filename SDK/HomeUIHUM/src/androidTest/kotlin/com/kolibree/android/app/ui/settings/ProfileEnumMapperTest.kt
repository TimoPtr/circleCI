/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ProfileEnumMapperTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val profileEnumMapper = ProfileEnumMapper(context())

    @Test
    fun fromGenderResString_returns_correct_Gender_for_a_given_Gender_string() {
        assertEquals(Gender.MALE,
            profileEnumMapper.fromGenderResString(getString(R.string.settings_profile_gender_male)))
        assertEquals(Gender.FEMALE,
            profileEnumMapper.fromGenderResString(getString(R.string.settings_profile_gender_female)))
        assertEquals(Gender.PREFER_NOT_TO_ANSWER,
            profileEnumMapper.fromGenderResString(getString(R.string.gender_prefer_not_to_answer)))
        assertEquals(Gender.UNKNOWN,
            profileEnumMapper.fromGenderResString(getString(R.string.gender_unknown)))
    }

    @Test
    fun fromHandednessResString_returns_correct_Handedness_for_a_given_Handedness_string() {
        assertEquals(Handedness.RIGHT_HANDED,
            profileEnumMapper.fromHandednessResString(getString(R.string.settings_profile_handedness_right)))
        assertEquals(Handedness.LEFT_HANDED,
            profileEnumMapper.fromHandednessResString(getString(R.string.settings_profile_handedness_left)))
        assertEquals(Handedness.UNKNOWN,
            profileEnumMapper.fromHandednessResString(getString(R.string.handedness_unknown)))
    }

    @Test
    fun getResString_for_Gender_returns_correct_string_for_a_given_Gender() {
        assertEquals(getString(R.string.settings_profile_gender_male),
            profileEnumMapper.getResString(Gender.MALE))
        assertEquals(getString(R.string.settings_profile_gender_female),
            profileEnumMapper.getResString(Gender.FEMALE))
        assertEquals(getString(R.string.gender_prefer_not_to_answer),
            profileEnumMapper.getResString(Gender.PREFER_NOT_TO_ANSWER))
        assertEquals(getString(R.string.gender_unknown),
            profileEnumMapper.getResString(Gender.UNKNOWN))
    }

    @Test
    fun getResString_for_Handedness_returns_correct_string_for_a_given_Handedness() {
        assertEquals(getString(R.string.settings_profile_handedness_right),
            profileEnumMapper.getResString(Handedness.RIGHT_HANDED))
        assertEquals(getString(R.string.settings_profile_handedness_left),
            profileEnumMapper.getResString(Handedness.LEFT_HANDED))
        assertEquals(getString(R.string.handedness_unknown),
            profileEnumMapper.getResString(Handedness.UNKNOWN))
    }

    private fun getString(@StringRes resId: Int) = context().getString(resId)
}
