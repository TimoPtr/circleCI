/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.account

import androidx.annotation.Keep
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal

@Keep
class AccountConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromParentalConsent(value: Int?): ParentalConsent {
        return when (value) {
            0 -> ParentalConsent.PENDING
            1 -> ParentalConsent.GRANTED
            else -> ParentalConsent.UNKNOWN
        }
    }

    @TypeConverter
    fun toParentalConsent(parentalConsent: ParentalConsent?): Int? {
        if (parentalConsent == null || parentalConsent == ParentalConsent.UNKNOWN) {
            return null
        }

        return when (parentalConsent == ParentalConsent.GRANTED) {
            true -> 1
            false -> 0
        }
    }

    @TypeConverter
    fun fromProfilesList(json: String?): ArrayList<ProfileInternal> {
        return json?.let {
            val turnsType = object : TypeToken<ArrayList<ProfileInternal>>() {}.type
            gson.fromJson<ArrayList<ProfileInternal>>(it, turnsType)
        } ?: ArrayList()
    }

    @TypeConverter
    fun toProfilesList(profiles: ArrayList<ProfileInternal>?): String? {
        return profiles?.let {
            gson.toJson(profiles)
        }
    }
}
