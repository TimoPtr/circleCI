package com.kolibree.android.accountinternal.profile.persistence.models

import com.google.gson.JsonObject
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test
import org.threeten.bp.LocalDate

class ProfileInternalTest : BaseUnitTest() {
    private val profileInternal = ProfileInternal(
        pictureUrl = pictureUrl,
        pictureLastModifier = pictureLastModifier,
        birthday = birthday,
        points = points,
        coachMusic = coachMusic,
        transitionSounds = transitionSounds,
        gender = gender,
        addressCountry = addressCountry,
        accountId = accountId,
        brushingTime = brushingTime,
        creationDate = creationDate,
        exactBirthday = exactBirthDay,
        firstName = firstName,
        handedness = handedness,
        id = id,
        isOwnerProfile = isOwnerProfile,
        age = age
    )

    @Test
    fun canExportProfile() {
        val exportedProfile = profileInternal.exportProfile()

        assertEquals(pictureUrl, exportedProfile.pictureUrl)
        assertEquals(firstName, exportedProfile.firstName)
        assertEquals(addressCountry, exportedProfile.country)
        assertEquals(gender, exportedProfile.gender.serializedName)
        assertEquals(handedness, exportedProfile.handedness.serializedName)
        assertEquals(brushingTime, exportedProfile.brushingGoalTime)
        assertEquals(id, exportedProfile.id)
        assertEquals(coachMusic, exportedProfile.coachMusic)
        assertEquals(transitionSounds, exportedProfile.coachTransitionSounds)
        assertEquals(!isOwnerProfile, exportedProfile.deletable)
        assertEquals(creationDate, exportedProfile.createdDate)
        assertEquals(points, exportedProfile.points)
        assertEquals(exactBirthDay, exportedProfile.exactBirthdate)
        assertEquals(birthday, exportedProfile.birthday)
        assertEquals(profileInternal.age, exportedProfile.age)
    }

    @Test
    fun canIncreasePoint() {
        val increaseValue = 100
        val newProfile = profileInternal.increasePoints(increaseValue)
        Assert.assertEquals(increaseValue + points, newProfile.points)
    }

    @Test
    fun canUpdateDataFromProfile() {
        val profile = Profile(
            profilePicture,
            profilePictureLastModifier,
            profileFirstName,
            addressCountry,
            Gender.FEMALE,
            Handedness.RIGHT_HANDED,
            profileBrushingTime,
            profileId,
            profileBirthday,
            profileCreationDate,
            null,
            profileCoachMusic,
            profileTransitionSounds,
            profileIsOwnerProfile,
            profilePoints,
            profileExactBirthday,
            profileAge
        )

        val newProfileinternal = profileInternal.copyProfile(profile)

        assertEquals(profilePicture, newProfileinternal.pictureUrl)
        assertEquals(profilePictureLastModifier, newProfileinternal.pictureLastModifier)
        assertEquals(profileFirstName, newProfileinternal.firstName)
        assertEquals(addressCountry, newProfileinternal.addressCountry)
        assertEquals(profileGender, newProfileinternal.gender)
        assertEquals(profileHandedness, newProfileinternal.handedness)
        assertEquals(profileBrushingTime, newProfileinternal.brushingTime)
        assertEquals(profileId, newProfileinternal.id)
        assertEquals(profileCreationDate, newProfileinternal.creationDate)
        assertEquals(profilePoints, newProfileinternal.points)
        assertEquals(profileExactBirthday, newProfileinternal.exactBirthday)
        assertEquals(profileBrushingTime, newProfileinternal.brushingTime)
        assertEquals(profileBirthday, newProfileinternal.birthday)
        assertEquals(profileAge, newProfileinternal.age)

        assertEquals(profileInternal.coachMusic, newProfileinternal.coachMusic)
        assertEquals(profileInternal.transitionSounds, newProfileinternal.transitionSounds)
        assertEquals(profileInternal.isOwnerProfile, newProfileinternal.isOwnerProfile)
    }

    @Test
    fun canCopyFromProfileInternal() {

        val profileInternalToCopyFrom = ProfileInternal(
            id = profileId,
            pictureUrl = profilePicture,
            pictureLastModifier = profilePictureLastModifier,
            firstName = profileFirstName,
            addressCountry = addressCountry,
            gender = profileGender,
            handedness = profileHandedness,
            brushingTime = profileBrushingTime,
            coachMusic = profileCoachMusic,
            accountId = profileAccountId,
            transitionSounds = profileTransitionSounds,
            isOwnerProfile = profileIsOwnerProfile,
            creationDate = profileCreationDate,
            points = profilePoints,
            exactBirthday = profileExactBirthday,
            birthday = profileBirthday,
            age = profileAge
        )

        val newProfileinternal = profileInternal.copyProfileInternal(profileInternalToCopyFrom)

        assertEquals(profilePicture, newProfileinternal.pictureUrl)
        assertEquals(profilePictureLastModifier, newProfileinternal.pictureLastModifier)
        assertEquals(profileFirstName, newProfileinternal.firstName)
        assertEquals(profileGender, newProfileinternal.gender)
        assertEquals(profileHandedness, newProfileinternal.handedness)
        assertEquals(profileBrushingTime, newProfileinternal.brushingTime)
        assertEquals(profilePoints, newProfileinternal.points)
        assertEquals(profileBirthday, newProfileinternal.birthday)
        assertEquals(profileAge, newProfileinternal.age)

        assertEquals(profileInternal.addressCountry, newProfileinternal.addressCountry)
        assertEquals(profileInternal.id, newProfileinternal.id)
        assertEquals(profileInternal.coachMusic, newProfileinternal.coachMusic)
        assertEquals(profileInternal.transitionSounds, newProfileinternal.transitionSounds)
        assertEquals(profileInternal.isOwnerProfile, newProfileinternal.isOwnerProfile)
        assertEquals(profileInternal.creationDate, newProfileinternal.creationDate)
    }

    private fun createProfileInternalJson(): JsonObject {
        val jo = JsonObject()
        jo.addProperty(
            ProfileInternal.FIELD_PICTURE,
            profilePicture
        )
        jo.addProperty(
            ProfileInternal.FIELD_PICTURE_LAST_MODIFIER,
            profilePictureLastModifier
        )
        jo.addProperty(
            ProfileInternal.FIELD_FIRST_NAME,
            profileFirstName
        )
        jo.addProperty(
            ProfileInternal.FIELD_IS_OWNER_PROFILE,
            profileIsOwnerProfile
        )
        jo.addProperty(
            ProfileInternal.FIELD_ADDRESS_COUNTRY,
            addressCountry
        )
        jo.addProperty(
            ProfileInternal.FIELD_GENDER,
            profileGender
        )
        jo.addProperty(
            ProfileInternal.FIELD_SURVEY_HANDEDNESS,
            profileHandedness
        )
        jo.addProperty(
            ProfileInternal.FIELD_BIRTHDAY,
            bday
        )
        jo.addProperty(ProfileInternal.FIELD_EXACT_BIRTHDAY, true)
        jo.addProperty(
            ProfileInternal.FIELD_ACCOUNT,
            accountId
        )
        jo.addProperty(
            ProfileInternal.FIELD_BRUSHING_GOAL_TIME,
            profileBrushingTime
        )
        jo.addProperty(
            ProfileInternal.FIELD_ID,
            profileId
        )
        jo.addProperty(
            ProfileInternal.FIELD_CREATION_DATE,
            profileCreationDate
        )

        val stat = JsonObject()
        stat.addProperty(
            ProfileInternal.FIELD_POINTS,
            profilePoints
        )
        jo.add(ProfileInternal.FIELD_STATS, stat)
        return jo
    }

    /*
    gender
     */

    @Test
    fun `gender F exports FEMALE`() {
        assertEquals(
            Gender.FEMALE,
            profileInternal.copy(gender = "F").exportProfile().gender
        )
    }

    @Test
    fun `gender M exports RIGHT_HANDED`() {
        assertEquals(
            Gender.MALE,
            profileInternal.copy(gender = "M").exportProfile().gender
        )
    }

    @Test
    fun `gender unknown exports UNKNOWN`() {
        assertEquals(
            Gender.UNKNOWN,
            profileInternal.copy(gender = null).exportProfile().gender
        )
        assertEquals(
            Gender.UNKNOWN,
            profileInternal.copy(gender = "").exportProfile().gender
        )
        assertEquals(
            Gender.UNKNOWN,
            profileInternal.copy(gender = "random").exportProfile().gender
        )
    }

    /*
    handedness
     */

    @Test
    fun `handedness L exports LEFT_HANDED`() {
        assertEquals(
            Handedness.LEFT_HANDED,
            profileInternal.copy(handedness = "L").exportProfile().handedness
        )
    }

    @Test
    fun `handedness R exports RIGHT_HANDED`() {
        assertEquals(
            Handedness.RIGHT_HANDED,
            profileInternal.copy(handedness = "R").exportProfile().handedness
        )
    }

    @Test
    fun `handedness unknown exports UNKNOWN`() {
        assertEquals(
            Handedness.UNKNOWN,
            profileInternal.copy(handedness = null).exportProfile().handedness
        )
        assertEquals(
            Handedness.UNKNOWN,
            profileInternal.copy(handedness = "").exportProfile().handedness
        )
        assertEquals(
            Handedness.UNKNOWN,
            profileInternal.copy(handedness = "random").exportProfile().handedness
        )
    }

    companion object {
        private const val id = 101L
        private const val pictureUrl = ""
        private const val pictureLastModifier = ""
        private const val firstName = "SomeName"
        private const val points = 42
        private const val isOwnerProfile = true
        private const val addressCountry = "FR"
        private const val gender = "M"
        private const val handedness = "L"
        private const val accountId = 1001
        private const val age = 28
        private const val brushingTime = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS
        private const val transitionSounds = true
        private const val creationDate = "1990-04-04T12:00:00+0000"
        private const val coachMusic = "rocky"
        private const val bday = "1990-02-04"
        private val birthday = LocalDate.parse(bday, DATE_FORMATTER)
        private const val exactBirthDay = true
        private const val profilePicture = "my_profile_pic"
        private const val profilePictureLastModifier = "my_profile_pic_last_modifier"
        private const val profileFirstName = "new_user"
        private const val profileGender = "F"
        private const val profileHandedness = "R"
        private const val profileBrushingTime = 120
        private const val profileId = 324234L
        private const val profileCoachMusic = "fast n furious"
        private const val profileTransitionSounds = false
        private const val profileIsOwnerProfile = false
        private const val profileCreationDate = "1918-04-24T12:00:34+0000"
        private const val profilePoints = 80
        private const val profileAccountId = 10000
        private const val profileExactBirthday = false
        private const val profileAge = 90
        private val profileBirthday = LocalDate.parse("1980-04-24", DATE_FORMATTER)
    }
}
