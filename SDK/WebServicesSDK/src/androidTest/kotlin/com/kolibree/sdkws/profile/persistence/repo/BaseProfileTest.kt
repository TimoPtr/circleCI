package com.kolibree.sdkws.profile.persistence.repo

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.accountinternal.getAgeFromBirthDate
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.room.DateConvertersString
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.room.ApiRoomDatabase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.Arrays
import org.junit.Rule
import timber.log.Timber

abstract class BaseProfileTest : BaseInstrumentationTest() {

    private val debugTree = object : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            println(message)
            // else do nothing
        }
    }

    init {
        if (Timber.treeCount() == 0) {
            Timber.plant(debugTree)
        }
    }

    @get:Rule
    val schedulersRule =
        UnitTestImmediateRxSchedulersOverrideRule()

    internal lateinit var apiRoomDatabase: ApiRoomDatabase

    protected val profileDatastore: ProfileDatastore = object : ProfileDatastore {

        override fun markAsUpdated(profile: ProfileInternal): Completable {
            TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
        }

        override fun profilesFlowable(): Flowable<List<ProfileInternal>> {
            TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
        }

        private val profilesList = mutableListOf<ProfileInternal>()

        override fun getProfiles(): Single<List<ProfileInternal>> = Single.just(profilesList)

        override fun getProfile(profileId: Long): Single<ProfileInternal> {
            return Single.just(profilesList.find { it.id == profileId })
        }

        override fun addProfiles(profiles: List<ProfileInternal>): List<Long> {
            val profilesToAddWithId = mutableListOf<ProfileInternal>()

            profiles.forEach {
                profilesToAddWithId.add(returnProfileWithId(it))
            }

            profilesList.addAll(profilesToAddWithId)

            return profiles.map { it.id }
        }

        override fun addProfile(profile: ProfileInternal): Long {
            val profileWithId = returnProfileWithId(profile)

            profilesList.add(profileWithId)

            return profileWithId.id
        }

        override fun deleteProfile(profileId: Long): Completable {
            return Completable.fromAction {
                profilesList.removeIf { it.id == profileId }
            }
        }

        override fun updateProfile(profile: ProfileInternal): Completable {
            return Completable.fromAction {
                val oldProfileIndex = profilesList.indexOfFirst { it.id == profile.id }

                if (oldProfileIndex > 0) {
                    profilesList.removeAt(oldProfileIndex)

                    profilesList.add(profile)
                }
            }
        }

        override fun deleteAll(): Completable {
            return Completable.fromAction { profilesList.clear() }
        }

        var initialId = 1L

        private fun returnProfileWithId(it: ProfileInternal): ProfileInternal {
            return if (it.id == 0L)
                it.copy(id = initialId++)
            else
                it
        }
    }

    internal val profiles = arrayListOf<ProfileInternal>()

    protected fun initRoom() {
        apiRoomDatabase = providesAppDatabase(
            InstrumentationRegistry.getInstrumentation().getTargetContext()
        )
        createDummyData()
    }

    protected fun clearAll() {
        clearDB()
        profiles.clear()
    }

    /**
     * Clean the content of the DB
     */
    protected fun clearDB() {
        apiRoomDatabase.clearAllTables()
    }

    /**
     * Add some data in Room before the tests
     */
    private fun createDummyData() {
        val profile1 = createProfileInternal(1, firstNameProfile1, genderMale, pointsProfile1)
        val profile2 = createProfileInternal(2, firstNameProfile2, genderMale, pointsProfile2)
        val profile3 = createProfileInternal(3, firstNameProfile3, genderFemale, pointsProfile3)

        profiles.addAll(Arrays.asList(profile1, profile2, profile3))
    }

    private fun providesAppDatabase(context: Context): ApiRoomDatabase {
        return Room.inMemoryDatabaseBuilder(context, ApiRoomDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    protected fun createProfileInternal(
        id: Long = DEFAULT_PROFILE_ID,
        firstName: String,
        gender: String,
        points: Int,
        accountId: Long = defaultAccountId,
        brushingTime: Int = 120
    ) = ProfileInternal(
        id = id,
        firstName = firstName,
        gender = gender,
        points = points,
        accountId = accountId.toInt(),
        birthday = birthday,
        age = getAgeFromBirthDate(birthday),
        brushingTime = brushingTime,
        creationDate = creationDate
    )

    protected fun ProfileInternal.compare(p: ProfileInternal): Boolean {
        return this.brushingTime == p.brushingTime &&
            this.accountId == p.accountId &&
            this.id == p.id &&
            this.age == p.age &&
            this.gender == p.gender &&
            this.firstName == p.firstName
    }

    protected fun createProfileData(): CreateProfileData {
        val objToAdd = CreateProfileData()

        objToAdd.firstName = firstNameProfile1
        objToAdd.setGender(Gender.MALE)
        objToAdd.age = ageProfile1.toInt()
        objToAdd.birthday = birthday
        objToAdd.country = "FR"
        return objToAdd
    }

    companion object {

        internal const val firstNameProfile1 = "user1"
        internal const val firstNameProfile2 = "user2"
        internal const val firstNameProfile3 = "user3"
        internal const val DEFAULT_PROFILE_ID = 99L
        internal const val ageProfile1 = 10L
        internal const val genderMale = "M"
        internal const val genderFemale = "F"
        internal const val pointsProfile1 = 10
        internal const val pointsProfile2 = 20
        internal const val pointsProfile3 = 30
        internal const val defaultAccountId = 101L
        internal val birthday = DateConvertersString().getLocalDateFromString("1990-02-04")!!
        private val creationDate = "1990-02-04T00:00:00+0000"
    }
}
