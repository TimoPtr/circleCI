package com.kolibree.sdkws.data.model

import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.android.commons.profile.Gender
import com.kolibree.sdkws.data.model.EditProfileData.UNSET
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test
import org.threeten.bp.LocalDate

class EditProfileDataTest {

    @Test
    fun integersAreInitialedToUNSET() {
        val data = EditProfileData()

        assertEquals(UNSET, data.age)
        assertEquals(UNSET, data.brushingNumber)
        assertEquals(UNSET, data.brushingTime)
    }

    @Test
    fun checkifBirthdayIsTheCorrectformat() {
        val date = LocalDate.of(1990, 12, 12)
        val data = EditProfileData()
        data.setBirthday(date)
        Assert.assertEquals(date, LocalDate.parse(data.birthday))
        Assert.assertEquals(data.birthday, DATE_FORMATTER.format(date))
    }

    /*
    setGender
     */

    @Test
    fun `setGender uses Gender's serializedName`() {
        val profileData = EditProfileData()
        val expectedGender = Gender.MALE

        profileData.setGender(expectedGender)

        assertEquals(expectedGender.serializedName, profileData.gender)
    }
}
