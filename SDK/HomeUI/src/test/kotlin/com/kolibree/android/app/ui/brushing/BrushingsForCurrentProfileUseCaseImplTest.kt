/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushing

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushing.BrushingsForCurrentProfileUseCaseImpl.Companion.getGameBrushingConstraint
import com.kolibree.android.app.ui.game.ActivityGame as Game
import com.kolibree.android.commons.GameApiConstants as Constant
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class BrushingsForCurrentProfileUseCaseImplTest : BaseUnitTest() {

    private val brushingRepository: BrushingsRepository = mock()
    private val profileProvider: CurrentProfileProvider = mock()

    @Test
    fun `getBrushingCount should query the profile from the Provider and dispatch the Repository brushing count result`() {
        val profileId = 10L
        val brushingCount: Long = 123
        val mockProfile = mock<Profile>()

        whenever(mockProfile.id).thenReturn(profileId)
        whenever(profileProvider.currentProfileFlowable()).thenReturn(Flowable.just(mockProfile))
        whenever(brushingRepository.countBrushings(profileId)).thenReturn(Single.just(brushingCount))

        val brushingUseCaseImpl =
            BrushingsForCurrentProfileUseCaseImpl(brushingRepository, profileProvider)
        val testObserver = brushingUseCaseImpl.getBrushingCount().test()

        testObserver.assertValue(brushingCount)
    }

    @Test
    fun `getBrushingOfflineCount should query the profile from the Provider and dispatch the Repository offline brushing count result`() {
        val profileId = 10L
        val brushingCount: Long = 123
        val mockProfile = mock<Profile>()

        whenever(mockProfile.id).thenReturn(profileId)
        whenever(profileProvider.currentProfileFlowable()).thenReturn(Flowable.just(mockProfile))
        whenever(brushingRepository.countBrushings(Constant.GAME_OFFLINE, profileId))
            .thenReturn(Single.just(brushingCount))

        val brushingUseCaseImpl =
            BrushingsForCurrentProfileUseCaseImpl(brushingRepository, profileProvider)
        val testObserver = brushingUseCaseImpl.getBrushingOfflineCount().test()

        testObserver.assertValue(brushingCount)
    }

    @Test
    fun `getBrushingCount should query the profile from the Provider and dispatch the Repository brushing count in given game`() {
        val profileId = 10L
        val game = Game.TestBrushing
        val brushingCount: Long = 123
        val mockProfile = mock<Profile>()

        whenever(mockProfile.id).thenReturn(profileId)
        whenever(profileProvider.currentProfileFlowable()).thenReturn(Flowable.just(mockProfile))
        whenever(
            brushingRepository.countBrushings(
                getGameBrushingConstraint(game, false)!!,
                profileId
            )
        )
            .thenReturn(Single.just(brushingCount))

        val brushingUseCaseImpl =
            BrushingsForCurrentProfileUseCaseImpl(brushingRepository, profileProvider)
        val testObserver = brushingUseCaseImpl.getBrushingCount(game, false).test()

        testObserver.assertValue(brushingCount)
    }

    @Test
    fun `getGameBrushingConstraint should correctly map values to backend IDs`() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)
        assertEquals(Constant.GAME_SBA, getGameBrushingConstraint(Game.TestBrushing, false))
        assertEquals(Constant.GAME_SBA, getGameBrushingConstraint(Game.TestBrushing, true))
        assertEquals(Constant.GAME_GO_PIRATE, getGameBrushingConstraint(Game.Pirate, false))
        assertEquals(Constant.GAME_GO_PIRATE, getGameBrushingConstraint(Game.Pirate, true))
        assertEquals(Constant.GAME_RABBIDS, getGameBrushingConstraint(Game.Rabbids, false))
        assertEquals(Constant.GAME_RABBIDS, getGameBrushingConstraint(Game.Rabbids, true))
        assertEquals(Constant.GAME_COACH_PLUS, getGameBrushingConstraint(Game.CoachPlus, false))
        assertEquals(Constant.GAME_COACH_MANUAL, getGameBrushingConstraint(Game.CoachPlus, true))
        assertEquals(Constant.GAME_COACH, getGameBrushingConstraint(Game.Coach, false))
        assertEquals(Constant.GAME_COACH_MANUAL, getGameBrushingConstraint(Game.Coach, true))
        assertNull(getGameBrushingConstraint(Game.Archaelogy, false))
        assertNull(getGameBrushingConstraint(Game.Archaelogy, true))
        assertNull(getGameBrushingConstraint(Game.TestAngles, false))
        assertNull(getGameBrushingConstraint(Game.TestAngles, true))
        assertNull(getGameBrushingConstraint(Game.SpeedControl, false))
        assertNull(getGameBrushingConstraint(Game.SpeedControl, true))
    }
}
