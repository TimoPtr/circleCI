/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.profile.persistence.models

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.kolibree.android.accountinternal.getAgeFromBirthDate
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.commons.profile.SourceApplication
import com.kolibree.android.defensive.Preconditions
import com.kolibree.android.defensive.PreconditionsKt
import com.kolibree.android.room.DateConvertersString
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MAXIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MINIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS
import org.threeten.bp.LocalDate

@Keep
@Entity(tableName = ProfileInternal.TABLE_NAME)
@TypeConverters(DateConvertersString::class)
data class ProfileInternal(
    @ColumnInfo(name = FIELD_ID) @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = FIELD_PICTURE)
    @SerializedName(FIELD_PICTURE) var pictureUrl: String? = null,
    @ColumnInfo(name = FIELD_PICTURE_LAST_MODIFIER)
    @SerializedName(FIELD_PICTURE_LAST_MODIFIER) var pictureLastModifier: String? = null,
    @ColumnInfo(name = FIELD_FIRST_NAME)
    @SerializedName(FIELD_FIRST_NAME) var firstName: String,
    @Deprecated(
        message = "Points are deprecated and will be removed in the future",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith("nothing")
    )
    @ColumnInfo(name = FIELD_POINTS) var points: Int,
    @ColumnInfo(name = FIELD_IS_OWNER_PROFILE)
    @SerializedName(FIELD_IS_OWNER_PROFILE) val isOwnerProfile: Boolean = false,
    @ColumnInfo(name = FIELD_ADDRESS_COUNTRY)
    @SerializedName(FIELD_ADDRESS_COUNTRY) var addressCountry: String? = null,
    @ColumnInfo(name = FIELD_GENDER)
    @SerializedName(FIELD_GENDER) var gender: String? = null,
    @ColumnInfo(name = FIELD_SURVEY_HANDEDNESS)
    @SerializedName(FIELD_SURVEY_HANDEDNESS) var handedness: String? = null,
    @ColumnInfo(name = FIELD_ACCOUNT)
    @SerializedName(FIELD_ACCOUNT) val accountId: Int,
    @ColumnInfo(name = FIELD_BUSHING_NB)
    @SerializedName(FIELD_BUSHING_NB) var brushingNumber: Int = 0,
    @ColumnInfo(name = FIELD_BRUSHING_GOAL_TIME)
    @SerializedName(FIELD_BRUSHING_GOAL_TIME) var brushingTime: Int,
    @ColumnInfo(name = FIELD_COACH_MUSIC)
    @SerializedName(FIELD_COACH_MUSIC) var coachMusic: String? = null,
    @ColumnInfo(name = FIELD_COACH_TRANSITION_SOUNDS)
    @SerializedName(FIELD_COACH_TRANSITION_SOUNDS) var transitionSounds: Boolean = false,
    @ColumnInfo(name = FIELD_CREATION_DATE)
    @SerializedName(FIELD_CREATION_DATE) val creationDate: String,
    @ColumnInfo(name = FIELD_BIRTHDAY)
    @SerializedName(FIELD_BIRTHDAY) var birthday: LocalDate?,
    @ColumnInfo(name = FIELD_EXACT_BIRTHDAY)
    @SerializedName(FIELD_EXACT_BIRTHDAY) val exactBirthday: Boolean = false,
    @ColumnInfo(name = FIELD_AGE) var age: Int = birthday?.let {
        getAgeFromBirthDate(it)
    } ?: Profile.DEFAULT_AGE,
    @ColumnInfo(name = FIELD_SOURCE_APPLICATION)
    @SerializedName(FIELD_SOURCE_APPLICATION) val sourceApplication: String? = null,
    @ColumnInfo(name = FIELD_NEEDS_UPDATE)
    @Transient val needsUpdate: Boolean = false
) {

    init {
        Preconditions.checkArgumentNonNegative(id)
        Preconditions.checkArgument(firstName.isNotEmpty())
        Preconditions.checkArgumentInRange(
            brushingTime,
            MINIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS,
            MAXIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS,
            "goal duration"
        )
        PreconditionsKt.checkArgumentContainsZonedDateTime(creationDate)
        Preconditions.checkArgumentNonNegative(age)
        Preconditions.checkArgumentNonNegative(brushingNumber)
    }

    @Ignore
    var pictureGetUrl: String? = null
    @Ignore
    var pictureUploadUrl: String? = null

    fun exportProfile(): Profile {
        return Profile(
            pictureUrl = pictureUrl,
            pictureLastModifier = pictureLastModifier,
            firstName = firstName,
            country = addressCountry,
            gender = getGenderEnum(),
            handedness = getHandednessEnum(),
            brushingGoalTime = brushingTime,
            id = id,
            coachMusic = coachMusic,
            coachTransitionSounds = transitionSounds,
            deletable = !isOwnerProfile,
            createdDate = creationDate,
            points = points,
            exactBirthdate = exactBirthday,
            birthday = birthday,
            brushingNumber = brushingNumber,
            age = age,
            sourceApplication = SourceApplication.findBySerializedName(sourceApplication)
        )
    }

    fun copyProfile(profile: Profile): ProfileInternal = this.copy(
        pictureUrl = profile.pictureUrl,
        pictureLastModifier = profile.pictureLastModifier,
        firstName = profile.firstName,
        addressCountry = profile.country,
        gender = profile.gender.serializedName,
        handedness = profile.handedness.serializedName,
        birthday = profile.birthday,
        exactBirthday = profile.exactBirthdate,
        brushingTime = profile.brushingGoalTime,
        id = profile.id,
        points = profile.points,
        age = profile.age,
        brushingNumber = profile.brushingNumber,
        creationDate = profile.createdDate,
        sourceApplication = profile.sourceApplication?.serializedName
    )

    fun copyProfileInternal(profile: ProfileInternal): ProfileInternal = this.copy(
        firstName = profile.firstName,
        addressCountry = profile.addressCountry,
        pictureUrl = profile.pictureUrl,
        pictureLastModifier = profile.pictureLastModifier,
        handedness = profile.handedness,
        birthday = profile.birthday,
        gender = profile.gender,
        brushingTime = profile.brushingTime,
        points = profile.points,
        age = profile.age,
        brushingNumber = profile.brushingNumber,
        needsUpdate = profile.needsUpdate,
        sourceApplication = profile.sourceApplication
    )

    @Deprecated(
        message = "Points are deprecated and will be removed in the future",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith("nothing")
    )
    fun increasePoints(amount: Int) = this.copy(points = points + amount)

    private fun getHandednessEnum() = Handedness.findBySerializedName(handedness)

    private fun getGenderEnum() = Gender.findBySerializedName(gender)

    @Keep
    companion object {

        const val TABLE_NAME = "profiles"

        // use only to get data of Json, used only to get the points
        const val FIELD_PICTURE_GET_URL = "picture_get_url"

        const val FIELD_PICTURE = "picture"
        const val FIELD_PICTURE_LAST_MODIFIER = "picture_last_modifier"
        const val FIELD_PICTURE_UPLOAD_URL = "picture_upload_url"
        const val FIELD_FIRST_NAME = "first_name"
        const val FIELD_POINTS = "points"
        const val FIELD_IS_OWNER_PROFILE = "is_owner_profile"
        const val FIELD_ADDRESS_COUNTRY = "address_country"
        const val FIELD_GENDER = "gender"
        const val FIELD_SURVEY_HANDEDNESS = "survey_handedness"
        const val FIELD_BIRTHDAY = "birthday"
        const val FIELD_EXACT_BIRTHDAY = "exact_birthday"
        const val FIELD_ACCOUNT = "account"
        const val FIELD_BUSHING_NB = "brushing_number"
        const val FIELD_BRUSHING_GOAL_TIME = "brushing_goal_time"
        const val FIELD_ID = "id"
        const val FIELD_AGE = "age"
        const val FIELD_CREATION_DATE = "created_at"
        const val FIELD_COACH_MUSIC = "coach_music"
        const val FIELD_COACH_TRANSITION_SOUNDS = "transition_sounds"
        const val FIELD_STATS = "stats"
        const val FIELD_NEEDS_UPDATE = "needs_update"
        const val FIELD_SOURCE_APPLICATION = "application"
    }
}
