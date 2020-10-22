package com.kolibree.sdkws.sms.data

import android.os.Parcelable
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

/**
 * Class provides information about user
 */

@Parcelize
data class AccountData(
    val birthday: LocalDate,
    val gender: Gender,
    val handedness: Handedness,
    val country: String,
    val firstName: String
) : Parcelable
