package com.kolibree.android.accountinternal.internal

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.kolibree.android.accountinternal.account.ParentalConsent
import com.kolibree.android.accountinternal.profile.persistence.ProfileInternalAdapter
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.android.commons.gson.LocalDateTypeAdapter
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.retrofit.ParentalConsentTypeAdapter
import java.io.IOException
import junit.framework.TestCase.assertEquals
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate

@RunWith(AndroidJUnit4::class)
class ProfileInternalParseTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private var gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(ParentalConsent::class.java, ParentalConsentTypeAdapter())
        .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
        .create()

    @Before
    override fun setUp() {
        super.setUp()
        gson = gson.newBuilder()
            .registerTypeAdapter(ProfileInternal::class.java, ProfileInternalAdapter(gson))
            .create()
    }

    @Throws(IOException::class)
    @Test
    fun jsonConstructor_verify_all_data_are_correct() {
        val json = loadJson("profile")
        val expected = JSONObject(json)

        val profile = gson.fromJson(json, ProfileInternal::class.java)

        assertEquals(expected.getString(ProfileInternal.FIELD_FIRST_NAME), profile.firstName)
        assertEquals(expected.getString(ProfileInternal.FIELD_PICTURE), profile.pictureUrl)
        assertEquals(expected.getString(ProfileInternal.FIELD_PICTURE_UPLOAD_URL), profile.pictureUploadUrl)
        assertEquals(expected.getBoolean(ProfileInternal.FIELD_IS_OWNER_PROFILE), profile.isOwnerProfile)
        assertEquals(expected.getString(ProfileInternal.FIELD_ADDRESS_COUNTRY), profile.addressCountry)
        assertEquals(expected.getString(ProfileInternal.FIELD_GENDER), profile.gender)
        assertEquals(expected.getString(ProfileInternal.FIELD_SURVEY_HANDEDNESS), profile.handedness)
        assertEquals(expected.getString(ProfileInternal.FIELD_BIRTHDAY), DATE_FORMATTER.format(profile.birthday))
        assertEquals(expected.getBoolean(ProfileInternal.FIELD_EXACT_BIRTHDAY), profile.exactBirthday)
        assertEquals(expected.getLong(ProfileInternal.FIELD_ACCOUNT), profile.accountId.toLong())
        assertEquals(expected.getLong(ProfileInternal.FIELD_BRUSHING_GOAL_TIME), profile.brushingTime.toLong())
        assertEquals(expected.getLong(ProfileInternal.FIELD_ID), profile.id)
        assertEquals(expected.getString(ProfileInternal.FIELD_CREATION_DATE), profile.creationDate)

        assertEquals(expected.getString(ProfileInternal.FIELD_PICTURE_UPLOAD_URL), profile.pictureUploadUrl)
        assertEquals(expected.getString(ProfileInternal.FIELD_PICTURE_GET_URL), profile.pictureGetUrl)

        assertEquals(
            expected.getJSONObject(ProfileInternal.FIELD_STATS).getLong(ProfileInternal.FIELD_POINTS),
            profile.points.toLong()
        )
    }

    @Throws(IOException::class)
    private fun loadJson(fileName: String): String {
        return SharedTestUtils.getJson("json/profile/$fileName.json")
    }
}
