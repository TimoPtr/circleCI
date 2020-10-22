/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.profile.models

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.Keep
import com.kolibree.android.accountinternal.getAgeFromBirthDate
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.commons.profile.SourceApplication
import com.kolibree.android.defensive.Preconditions
import com.kolibree.android.defensive.PreconditionsKt
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MAXIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MINIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime

@Keep
@Parcelize
data class Profile(
    override val pictureUrl: String? = null,
    override val pictureLastModifier: String? = null,
    override val firstName: String,
    override val country: String? = null,
    override val gender: Gender,
    override val handedness: Handedness,
    override val brushingGoalTime: Int,
    override val id: Long,
    override val birthday: LocalDate?,
    override val createdDate: String,
    override val sourceApplication: SourceApplication? = null,
    val coachMusic: String? = null,
    val coachTransitionSounds: Boolean = false,
    val deletable: Boolean = true,
    @Deprecated(
        message = "Points are deprecated and will be removed in the future",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith("nothing")
    )
    val points: Int = 0,
    val exactBirthdate: Boolean = false,
    val age: Int = birthday?.let { getAgeFromBirthDate(it) } ?: DEFAULT_AGE,
    val brushingNumber: Int = 0
) : IProfile, Parcelable {

    init {
        Preconditions.checkArgumentNonNegative(id)
        Preconditions.checkArgument(firstName.isNotEmpty())
        Preconditions.checkArgumentInRange(
            brushingGoalTime,
            MINIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS,
            MAXIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS,
            "goal duration"
        )
        PreconditionsKt.checkArgumentContainsZonedDateTime(createdDate)
        Preconditions.checkArgumentNonNegative(age)
        Preconditions.checkArgumentNonNegative(brushingNumber)
    }

    fun hasPicture() = pictureUrl != null && pictureUrl.isNotEmpty()
    fun getCoachMusicFileUri() = if (coachMusic != null) Uri.parse(coachMusic) else null
    fun copyBirthday(b: LocalDate) = this.copy(birthday = b)
    fun getCreationDate(): OffsetDateTime = OffsetDateTime.parse(createdDate, DATETIME_FORMATTER)

    val brushingGoalDuration: Duration
        get() = Duration.ofSeconds(brushingGoalTime.toLong())

    companion object {

        const val DEFAULT_AGE = 25

        internal const val DEFAULT_FIRST_NAME = "Me"

        fun of(profile: IProfile): Profile = Profile(
            pictureUrl = profile.pictureUrl,
            pictureLastModifier = profile.pictureLastModifier,
            firstName = profile.firstName,
            country = profile.country,
            gender = profile.gender,
            handedness = profile.handedness,
            brushingGoalTime = profile.brushingGoalTime,
            createdDate = profile.createdDate,
            birthday = profile.birthday,
            id = profile.id,
            sourceApplication = profile.sourceApplication
        )
    }
}
