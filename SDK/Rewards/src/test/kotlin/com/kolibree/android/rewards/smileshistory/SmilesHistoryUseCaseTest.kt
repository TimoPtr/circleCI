/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.rewards.ProfileWithSmiles
import com.kolibree.android.rewards.ProfileWithSmilesUseCase
import com.kolibree.android.rewards.models.FakeEvent
import com.kolibree.android.rewards.persistence.RewardsRepository
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.core.AvatarCache
import com.kolibree.sdkws.profile.ProfileManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

internal class SmilesHistoryUseCaseTest : BaseUnitTest() {

    @Mock
    private lateinit var currentProfileProvider: CurrentProfileProvider

    @Mock
    private lateinit var profileUseCase: ProfileWithSmilesUseCase

    @Mock
    private lateinit var rewardsRepository: RewardsRepository

    @Mock
    private lateinit var brushingsRepository: BrushingsRepository

    @Mock
    private lateinit var profileManager: ProfileManager

    private val avatarCache: AvatarCache = mock()

    private lateinit var useCase: SmilesHistoryUseCase

    override fun setup() {
        super.setup()
        useCase = SmilesHistoryUseCase(
            profileManager,
            currentProfileProvider,
            rewardsRepository,
            brushingsRepository,
            avatarCache,
            profileUseCase
        )
        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    @Test
    fun `when event are unknown it does not create item`() {
        val fakeEvent = FakeEvent(
            ProfileBuilder.DEFAULT_ID,
            "fake !! should not displayed",
            101,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 4, ZoneOffset.UTC)
        )
        val profileId = 10L
        val profile = ProfileBuilder.create().withId(profileId).build()

        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(Flowable.just(profile))
        whenever(profileUseCase.retrieveOtherProfilesSmilesStream(any())).thenReturn(Flowable.never())
        whenever(rewardsRepository.smilesHistoryEvents(profile.id)).thenReturn(
            Flowable.just(
                listOf(
                    fakeEvent
                )
            )
        )

        val testObserver = useCase.smilesHistoryStream().test()
        testObserver.assertValue {
            it.first.isEmpty() && it.second.isEmpty()
        }
    }

    @Test
    fun `retrieveSmilesHistory emit emptyList first`() {
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(
            Flowable.error(
                Exception()
            )
        )
        val profile = ProfileBuilder.create().build()

        whenever(rewardsRepository.smilesHistoryEvents(any())).thenReturn(Flowable.never())
        val testObserver = useCase.retrieveSmilesHistory(profile).test()

        testObserver.assertValue(emptyList())
    }

    @Test
    fun `retrieveOtherProfilesSmiles emit emptyList first`() {
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(
            Flowable.error(
                Exception()
            )
        )
        val profile = ProfileBuilder.create().build()

        whenever(profileUseCase.retrieveOtherProfilesSmilesStream(any())).thenReturn(Flowable.never())
        val testObserver = useCase.retrieveOtherProfilesSmiles(profile).test()

        testObserver.assertValue(emptyList())
    }

    @Test
    fun `retrieveOtherProfilesSmiles emit profiles with transformed avatarUrl`() {
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(Flowable.never())
        val profile = ProfileBuilder.create().build()

        val expectedAvatarUrl = "expected avatar url"
        whenever(avatarCache.getAvatarUrl(any())).thenReturn(expectedAvatarUrl)

        whenever(profileUseCase.retrieveOtherProfilesSmilesStream(any())).thenReturn(
            Flowable.just(
                listOf(ProfileWithSmiles(profile, 10))
            )
        )
        val testObserver = useCase.retrieveOtherProfilesSmiles(profile).test()

        testObserver.assertValueAt(1) { itemViewModelList ->
            itemViewModelList[0].drawableUrl == expectedAvatarUrl
        }
    }
}
