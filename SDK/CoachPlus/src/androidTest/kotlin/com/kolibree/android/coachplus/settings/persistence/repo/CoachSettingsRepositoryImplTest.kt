package com.kolibree.android.coachplus.settings.persistence.repo

import com.kolibree.android.coachplus.settings.persistence.model.CoachSettingsEntity
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by Guillaume Agis on 03/10/2018.
 */
class CoachSettingsRepositoryImplTest : CoachSettingsHelperTest() {

    @get:Rule
    val schedulersRule =
        UnitTestImmediateRxSchedulersOverrideRule()

    @Before
    fun setup() {
        initRoom()
    }

    @Test
    fun verifyCanUpdateDB() {
        clearDB()
        val expected = CoachSettingsEntity(profileId = profile_id)
        repository.getSettingsByProfileId(profile_id).test()
            .awaitCount(1)
            .assertNoErrors()
            .assertValue(expected)

        val settingsToAdd = CoachSettingsEntity(
            profileId = profile_id,
            enableTransitionSounds = true,
            enableHelpText = true,
            enableMusic = false,
            enableShuffle = false
        )

        repository.save(settingsToAdd).concatWith {
            repository.getSettingsByProfileId(profile_id).test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(settingsToAdd)
        }

        repository.truncate().concatWith {
            repository.getSettingsByProfileId(profile_id).test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(expected)
        }
    }

    @After
    override fun tearDown() {
        clearDB()
    }

    companion object {
        private const val profile_id = 42L
    }
}
