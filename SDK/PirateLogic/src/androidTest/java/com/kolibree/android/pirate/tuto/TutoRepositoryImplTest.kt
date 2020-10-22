package com.kolibree.android.pirate.tuto

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TutoRepositoryImplTest : TutorialHelperTest() {

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
        assertFalse(tutoRepository.hasSeenBreeFirstMessage(profile_id))
        assertFalse(tutoRepository.hasSeenPirateCompleteTrailer(profile_id))
        assertFalse(tutoRepository.hasSeenPirateTrailer(profile_id))
        assertFalse(tutoRepository.hasSeenPirateTuto(profile_id))

        tutoRepository.setGotABadgeWithLastBrushing(profile_id, true)
        tutoRepository.setHasSeenBreeFirstMessage(profile_id)
        tutoRepository.setHasSeenPirateCompleteTrailer(profile_id)
        tutoRepository.setHasSeenPirateTrailer(profile_id)
        tutoRepository.setHasSeenPirateTuto(profile_id)

        assertTrue(tutoRepository.hasSeenBreeFirstMessage(profile_id))
        assertTrue(tutoRepository.hasSeenPirateCompleteTrailer(profile_id))
        assertTrue(tutoRepository.hasSeenPirateTrailer(profile_id))
        assertTrue(tutoRepository.hasSeenPirateTuto(profile_id))
    }

    @After
    fun tearDown() {
        clearDB()
    }

    companion object {
        private const val profile_id = 42L
    }
}
