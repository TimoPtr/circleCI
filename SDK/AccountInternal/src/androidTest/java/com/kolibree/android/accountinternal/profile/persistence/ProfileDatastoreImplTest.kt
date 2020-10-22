package com.kolibree.android.accountinternal.profile.persistence

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.accountinternal.AccountRoomDatabase
import com.kolibree.android.accountinternal.getAgeFromBirthDate
import com.kolibree.android.accountinternal.profile.persistence.dao.ProfileDao
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.room.DateConvertersString
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import io.reactivex.schedulers.Schedulers
import java.util.Arrays
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(AndroidJUnit4::class)
class ProfileDatastoreImplTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val schedulersRule =
        UnitTestImmediateRxSchedulersOverrideRule()

    @Mock
    private lateinit var profileDao: ProfileDao

    private lateinit var apiRoomDatabase: AccountRoomDatabase

    private lateinit var profileDatastore: ProfileDatastore

    internal val profiles = arrayListOf<ProfileInternal>()

    private fun initRoom() {
        apiRoomDatabase = providesAppDatabase(
            InstrumentationRegistry.getInstrumentation().getTargetContext()
        )
        profileDao = apiRoomDatabase.profileDao()
        profileDatastore = ProfileDatastoreImpl(profileDao, Schedulers.io())
        createDummyData()
    }

    @Before
    override fun setUp() {
        super.setUp()
        initRoom()
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearAll()
    }

    private fun clearAll() {
        clearDB()
        profiles.clear()
    }

    /**
     * Clean the content of the DB
     */
    private fun clearDB() {
        apiRoomDatabase.clearAllTables()
    }

    /**
     * Add some data in Room before the tests
     */
    private fun createDummyData() {

        val profile1 = createProfileInternal(firstNameProfile1, genderMale, pointsProfile1)
        val profile2 = createProfileInternal(firstNameProfile2, genderMale, pointsProfile2)
        val profile3 = createProfileInternal(firstNameProfile3, genderFemale, pointsProfile3)

        profile1.id = profileDao.addProfile(profile1)
        profile2.id = profileDao.addProfile(profile2)
        profile3.id = profileDao.addProfile(profile3)

        profiles.addAll(Arrays.asList(profile1, profile2, profile3))
    }

    private fun providesAppDatabase(context: Context): AccountRoomDatabase {
        return Room.inMemoryDatabaseBuilder(context, AccountRoomDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    private fun createProfileInternal(
        firstName: String,
        gender: String,
        points: Int,
        accountId: Long = defaultAccountId,
        brushingTime: Int = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS
    ) = ProfileInternal(
        firstName = firstName,
        gender = gender,
        points = points,
        accountId = accountId.toInt(),
        birthday = birthday,
        age = getAgeFromBirthDate(birthday),
        brushingTime = brushingTime,
        creationDate = creationDate,
        brushingNumber = 0
    )

    companion object {

        internal const val firstNameProfile1 = "user1"
        internal const val firstNameProfile2 = "user2"
        internal const val firstNameProfile3 = "user3"
        internal const val genderMale = "M"
        internal const val genderFemale = "F"
        internal const val pointsProfile1 = 10
        internal const val pointsProfile2 = 20
        internal const val pointsProfile3 = 30
        internal const val defaultAccountId = 101L
        internal val birthday = DateConvertersString().getLocalDateFromString("1990-02-04")!!
        private const val creationDate = "1990-02-04T00:00:00+0000"
    }

    @Test
    fun verifyEmptyProfile() {
        clearDB()
        profileDatastore.getProfiles()
            .test()
            .assertNoErrors()
            .assertValue(emptyList())

        profileDatastore.getProfile(profiles.first().id)
            .test()
            .assertNotComplete()
    }

    @Test
    fun verifyCanDeleteAllProfile() {
        profileDatastore.getProfiles()
            .test()
            .assertNoErrors()
            .assertValue(profiles)

        profileDatastore.deleteAll().concatWith {
            profileDatastore.getProfiles()
                .test()
                .assertNoErrors()
                .assertValue(emptyList())
        }
    }

    @Test
    fun verifyCanDeleteAProfile() {
        profileDatastore.getProfiles()
            .test()
            .assertNoErrors()
            .assertValue(profiles)
        profileDatastore.deleteProfile(profiles.first().id).concatWith {
            val newList = profiles.subList(1, profiles.size - 1)
            profileDatastore.getProfiles()
                .test()
                .assertNoErrors()
                .assertValue(newList)
        }
    }

    @Test
    fun verifyCanUpdateAProfile() {

        val newName = "userWithNewName"
        val firstProfile = profiles.first()
        val newProfile = firstProfile.copy(firstName = newName)

        profileDatastore.updateProfile(newProfile).concatWith {
            profileDatastore.getProfile(firstProfile.id)
                .test()
                .assertNoErrors()
                .assertValue(newProfile)
        }
    }

    @Test
    fun verifyCanAddAProfile() {

        val newProfile = createProfileInternal("NewName", genderFemale, 0, 99)

        val res = profileDatastore.getProfiles().blockingGet()
        Assert.assertEquals(profiles, res)

        val newId = profileDatastore.addProfile(newProfile)
        val expected = newProfile.copy(id = newId)

        Assert.assertTrue(newId > 0)

        profileDatastore.getProfile(newId)
            .test()
            .assertNoErrors()
            .assertValue(expected)
    }

    @Test
    fun verifyCanAddSeveralProfiles() {

        val newProfile = createProfileInternal("NewName", genderFemale, 0, 99)
        val newProfile2 = createProfileInternal("NewName2", genderMale, 10, 199)
        val newProfiles = listOf(newProfile, newProfile2)

        profileDatastore.getProfiles()
            .test()
            .assertNoErrors()
            .assertValue(profiles)

        val newIds = profileDatastore.addProfiles(newProfiles)

        Assert.assertTrue(newIds[0] > 0)
        Assert.assertTrue(newIds[1] > 0)

        val expected1 = newProfile.copy(id = newIds[0])
        val expected2 = newProfile2.copy(id = newIds[1])

        profileDatastore.getProfile(newIds[0])
            .test()
            .assertNoErrors()
            .assertValue(expected1)

        profileDatastore.getProfile(newIds[1])
            .test()
            .assertNoErrors()
            .assertValue(expected2)
    }

    @Test
    fun verifyCanDeleteProfile() {

        val id = profiles.first().id

        profileDatastore.deleteProfile(id).concatWith {
            profileDatastore.getProfile(id)
                .test()
                .assertNoErrors()
                .assertNoValues()

            profileDatastore.getProfiles()
                .test()
                .assertNoErrors()
                .assertValue { res -> res.size == profiles.size - 1 }
        }
    }
}
