/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.utils

import android.telephony.PhoneNumberUtils
import androidx.annotation.Keep
import javax.inject.Inject

@Keep
interface PhoneNumberChecker {

    fun isValid(phoneNumber: String): Boolean
}

internal class PhoneNumberCheckerImpl @Inject constructor() :
    PhoneNumberChecker {

    override fun isValid(phoneNumber: String): Boolean {
        val normalize = PhoneNumberUtils.normalizeNumber(phoneNumber)
        return PhoneNumberUtils.isGlobalPhoneNumber(normalize)
    }
}
