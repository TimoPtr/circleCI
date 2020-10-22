/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.studies

import com.kolibree.account.utils.ToothbrushesForProfileUseCase
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.tracker.studies.StudiesForProfileUseCaseImpl.Companion.STUDY_SEPARATOR
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.junit.Test

internal class StudiesForProfileUseCaseImplTest : BaseUnitTest() {

    private val studiesRepository: StudiesRepository = mock()

    private val toothbrushesForProfileUseCase: ToothbrushesForProfileUseCase = mock()

    private lateinit var studiesForProfileUseCaseImpl: StudiesForProfileUseCaseImpl

    override fun setup() {
        super.setup()

        studiesForProfileUseCaseImpl = StudiesForProfileUseCaseImpl(
            studiesRepository,
            toothbrushesForProfileUseCase
        )
    }

    @Test
    fun `provide emits after first list of connections`() {
        val profileToothbrushesStream = PublishProcessor.create<List<KLTBConnection>>()
        whenever(toothbrushesForProfileUseCase.profileToothbrushesOnceAndStream(any()))
            .thenReturn(profileToothbrushesStream)

        val testObserver = studiesForProfileUseCaseImpl.provide(1986L).test()
        testObserver.assertNotComplete().assertNoErrors().assertNoValues()

        profileToothbrushesStream.onNext(listOf())
        testObserver.assertComplete().assertValueCount(1)

        profileToothbrushesStream.onNext(listOf())
        testObserver.assertComplete().assertValueCount(1)
    }

    @Test
    fun `provide emits empty String when profile has no toothbrush`() {
        whenever(toothbrushesForProfileUseCase.profileToothbrushesOnceAndStream(any()))
            .thenReturn(Flowable.fromIterable(listOf()))

        studiesForProfileUseCaseImpl
            .provide(1986L)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue { it == "" }
    }

    @Test
    fun `provide doesn't emit errors but empty String instead`() {
        whenever(toothbrushesForProfileUseCase.profileToothbrushesOnceAndStream(any()))
            .thenReturn(Flowable.error(Exception()))

        studiesForProfileUseCaseImpl
            .provide(1986L)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue { it == "" }
    }

    @Test
    fun `provide correctly builds and formats studies list including shared toothbrushes`() {
        val profileId = 1986L
        val mac1 = "mac1"
        val mac2 = "mac2"
        val mac3 = "mac3"

        val connection1 = KLTBConnectionBuilder
            .createWithDefaultState(true)
            .withMac(mac1)
            .withOwnerId(profileId)
            .build() as KLTBConnection

        val connection2 = KLTBConnectionBuilder
            .createWithDefaultState(true)
            .withMac(mac2)
            .withSharedMode()
            .build() as KLTBConnection

        val connection3 = KLTBConnectionBuilder
            .createWithDefaultState(true)
            .withMac(mac3)
            .withOwnerId(profileId)
            .build() as KLTBConnection

        val expectedStudy1 = "study1"
        val expectedStudy2 = NO_STUDY
        val expectedStudy3 = "study3"
        whenever(studiesRepository.getStudy(mac1)).thenReturn(expectedStudy1)
        whenever(studiesRepository.getStudy(mac2)).thenReturn(expectedStudy2)
        whenever(studiesRepository.getStudy(mac3)).thenReturn(expectedStudy3)

        whenever(toothbrushesForProfileUseCase.profileToothbrushesOnceAndStream(profileId))
            .thenReturn(
                Flowable.fromIterable(
                    listOf(
                        listOf(
                            connection1,
                            connection2,
                            connection3
                        )
                    )
                )
            )

        studiesForProfileUseCaseImpl
            .provide(profileId)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue { it == "$expectedStudy1$STUDY_SEPARATOR$expectedStudy3" }

        verify(studiesRepository).getStudy(mac1)
        verify(studiesRepository).getStudy(mac2)
        verify(studiesRepository).getStudy(mac3)
        verify(toothbrushesForProfileUseCase).profileToothbrushesOnceAndStream(profileId)
    }
}
