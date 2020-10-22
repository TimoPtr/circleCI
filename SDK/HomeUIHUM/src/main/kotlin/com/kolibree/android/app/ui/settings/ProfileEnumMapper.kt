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
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.homeui.hum.R
import javax.inject.Inject

@VisibleForApp
class ProfileEnumMapper @Inject constructor(private val context: Context) {

    private fun buildGenderResStringMap(context: Context) =
        Gender.values().map { it to context.getString(it.getResourceId()) }.toMap()

    private fun buildHandednessResStringMap(context: Context) =
        Handedness.values().map { it to context.getString(it.getResourceId()) }.toMap()

    fun fromGenderResString(resString: String): Gender? {
        val genderResStringMap = buildGenderResStringMap(context)
        for ((gender, description) in genderResStringMap) {
            if (description == resString) return gender
        }
        return null
    }

    fun fromHandednessResString(resString: String): Handedness? {
        val handednessResStringMap = buildHandednessResStringMap(context)
        for ((handedness, description) in handednessResStringMap) {
            if (description == resString) return handedness
        }
        return null
    }

    fun getResString(gender: Gender) = context.getString(gender.getResourceId())

    fun getResString(handedness: Handedness) = context.getString(handedness.getResourceId())
}

@StringRes
internal fun Gender.getResourceId(): Int {
    return when (this) {
        Gender.MALE -> R.string.settings_profile_gender_male
        Gender.FEMALE -> R.string.settings_profile_gender_female
        Gender.PREFER_NOT_TO_ANSWER -> R.string.gender_prefer_not_to_answer
        Gender.UNKNOWN -> R.string.gender_unknown
    }
}

@StringRes
internal fun Handedness.getResourceId(): Int {
    return when (this) {
        Handedness.RIGHT_HANDED -> R.string.settings_profile_handedness_right
        Handedness.LEFT_HANDED -> R.string.settings_profile_handedness_left
        Handedness.UNKNOWN -> R.string.handedness_unknown
    }
}
