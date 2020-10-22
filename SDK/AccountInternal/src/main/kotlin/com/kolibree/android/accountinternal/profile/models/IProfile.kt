/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.profile.models

import androidx.annotation.Keep
import androidx.annotation.RestrictTo
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.commons.profile.SourceApplication
import org.threeten.bp.LocalDate

/**
 * Profile interface
 */
@Keep
interface IProfile {
    val id: Long
    val firstName: String // first name
    val gender: Gender // gender of the user
    val handedness: Handedness // hand used by the user
    val brushingGoalTime: Int // brushing goal time
    val createdDate: String // date of the profile creation
    val birthday: LocalDate? // date of birth of the user
    val pictureUrl: String? // profile picture url , can start with http or any local url
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        get
    val pictureLastModifier: String? // profile picture version info, contains the last updated time
    val country: String? // country of the user
    val sourceApplication: SourceApplication? // in which app the profile was created
    /**
     * Check if the current profile is a Male
     *
     * @return true if it's a Male, false otherwise [Gender]
     */
    fun isMale() = gender == Gender.MALE

    /**
     * Check if the current profile is a Right handed
     *
     * @return true if the user is Right Handed, false otherwise [Handedness]
     */
    fun isRightHanded() = handedness == Handedness.RIGHT_HANDED
}
