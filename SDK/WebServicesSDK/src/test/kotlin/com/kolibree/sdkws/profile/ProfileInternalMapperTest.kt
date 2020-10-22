package com.kolibree.sdkws.profile

import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.HANDEDNESS_LEFT
import com.kolibree.android.commons.profile.HANDEDNESS_RIGHT
import com.kolibree.sdkws.data.model.EditProfileData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class ProfileInternalMapperTest {

    @Test
    fun copyEditData() {
        val expectedFirstName = "new First Name"
        val expectedAge = 78
        val expectedCountry = "RU"
        val expectedBrushingTime = 200
        val expectedHandedness =
            HANDEDNESS_LEFT
        val expectedGender = Gender.FEMALE.serializedName

        val data = EditProfileData()
        data.firstName = expectedFirstName
        data.gender = expectedGender
        data.age = expectedAge
        data.handedness = expectedHandedness
        data.countryCode = expectedCountry
        data.brushingTime = expectedBrushingTime

        val oldProfileInternal = profile(
            firstName = "oldFirstName",
            gender = Gender.MALE.serializedName,
            age = 1,
            handedness = HANDEDNESS_RIGHT,
            countryCode = "ES",
            brushingTime = 180,
            accountId = 0,
            birthday = LocalDate.MAX,
            creationDate = "1983-04-01T12:00:00+0000",
            points = 0
        )

        val newProfileInternal = ProfileInternalMapper().copyEditData(oldProfileInternal, data)

        assertNotEquals(oldProfileInternal, newProfileInternal)

        assertEquals(expectedFirstName, newProfileInternal.firstName)
        assertEquals(expectedGender, newProfileInternal.gender)
        assertEquals(expectedHandedness, newProfileInternal.handedness)
        assertEquals(expectedBrushingTime, newProfileInternal.brushingTime)
        assertEquals(expectedCountry, newProfileInternal.addressCountry)
    }

    @Test
    fun copyEditData_ageUnset_usesOldAge() {
        val expectedAge = 78
        val oldProfileInternal = profile(age = expectedAge)

        val data = EditProfileData()
        data.firstName = "Name"
        data.age = EditProfileData.UNSET
        data.brushingTime = 200

        val newProfileInternal = ProfileInternalMapper().copyEditData(oldProfileInternal, data)

        assertEquals(expectedAge, newProfileInternal.age)
    }

    @Test
    fun copyEditData_brushingTimeUnset_usesOldBrushingTime() {
        val expectedBrushingTime = 120
        val oldProfileInternal = profile(brushingTime = expectedBrushingTime)

        val data = EditProfileData()
        data.firstName = "Name"
        data.age = 1
        data.brushingTime = EditProfileData.UNSET

        val newProfileInternal = ProfileInternalMapper().copyEditData(oldProfileInternal, data)

        assertEquals(expectedBrushingTime, newProfileInternal.brushingTime)
    }

    @Test
    fun copyEditData_brushingNumberUnset_usesOldBrushingNumber() {
        val expectedBrushingNumber = 78
        val oldProfileInternal = profile(brushingNumber = expectedBrushingNumber)

        val data = EditProfileData()
        data.firstName = "Name"
        data.age = 1
        data.brushingNumber = EditProfileData.UNSET

        val newProfileInternal = ProfileInternalMapper().copyEditData(oldProfileInternal, data)

        assertEquals(expectedBrushingNumber, newProfileInternal.brushingNumber)
    }

    private fun profile(
        firstName: String = "first name",
        points: Int = 0,
        countryCode: String? = null,
        gender: String? = null,
        handedness: String? = null,
        accountId: Int = 1,
        brushingTime: Int = 120,
        creationDate: String = "1900-01-01T12:00:00+0000",
        birthday: LocalDate? = TrustedClock.getNowLocalDate(),
        age: Int = 1,
        brushingNumber: Int = 1
    ): ProfileInternal {
        return ProfileInternal(
            firstName = firstName,
            age = age,
            handedness = handedness,
            addressCountry = countryCode,
            brushingTime = brushingTime,
            accountId = accountId,
            birthday = birthday,
            creationDate = creationDate,
            points = points,
            gender = gender,
            brushingNumber = brushingNumber
        )
    }
}
