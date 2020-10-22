/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.logic

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingEvent
import com.kolibree.android.test.mocks.BrushingBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.data.model.Brushing
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

internal class BrushingEventsProviderTest : BaseUnitTest() {

    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val brushingsRepository: BrushingsRepository = mock()
    private val checkupCalculator: CheckupCalculator = mock()
    private val mapper: BrushingStatsToPersonalChallengeInputMapper = mock()

    private lateinit var provider: BrushingEventsProviderImpl

    override fun setup() {
        super.setup()

        provider = BrushingEventsProviderImpl(
            currentProfileProvider,
            brushingsRepository,
            checkupCalculator,
            mapper
        )
    }

    @Test
    fun `event stream for a given profile emits possible event given mapping function`() {
        val profileId = 10L
        val profile = ProfileBuilder.create().withId(profileId).build()
        val subject = PublishProcessor.create<List<Brushing>>()
        val checkupData = mock<CheckupData>()
        val expectedEvents = mock<List<BrushingEvent>>()

        whenever(currentProfileProvider.currentProfile()).thenReturn(profile)

        whenever(brushingsRepository.brushingsFlowable(profileId)).thenReturn(subject)

        whenever(mapper.map(any())).thenReturn(expectedEvents)

        whenever(checkupCalculator.calculateCheckup(anyString(), any(), any()))
            .thenReturn(checkupData)

        whenever(checkupData.surfacePercentage).thenReturn(10)

        val testObserver = provider.brushingEventsStream(profileId).test()

        testObserver.assertNoValues()

        subject.onNext(listOf(BrushingBuilder.create().build()))

        testObserver.assertValue(expectedEvents)
    }

    @Test
    fun `emits new events when profile changes`() {
        val profile1 = ProfileBuilder.create().withId(1).build()
        val profile2 = ProfileBuilder.create().withId(2).build()
        val profile3 = ProfileBuilder.create().withId(3).build()

        whenever(currentProfileProvider.currentProfileFlowable())
            .thenReturn(Flowable.just(profile1, profile2, profile3))

        whenever(brushingsRepository.getFirstBrushingSession(any()))
            .thenReturn(null)

        whenever(checkupCalculator.calculateCheckup(anyString(), any(), any()))
            .thenReturn(mock())

        whenever(brushingsRepository.brushingsFlowable(any()))
            .thenReturn(Flowable.just(listOf(BrushingBuilder.create().build())))

        val testObserver = provider.brushingEventsStreamCurrentProfile().test()
        testObserver.assertValueCount(3)
    }
}
