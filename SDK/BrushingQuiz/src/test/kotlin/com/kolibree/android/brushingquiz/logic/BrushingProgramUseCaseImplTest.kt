/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.logic

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.BrushingProgramToothbrushesUseCase
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Test

class BrushingProgramUseCaseImplTest : BaseUnitTest() {

    private val toothbrushesUseCase = mock<BrushingProgramToothbrushesUseCase>()
    private val currentProfileProvider: CurrentProfileProvider = mock()

    private lateinit var brushingProgramUseCase: BrushingProgramUtilsImpl

    override fun setup() {
        super.setup()

        brushingProgramUseCase =
            BrushingProgramUtilsImpl(toothbrushesUseCase, currentProfileProvider)
    }

    @Test
    fun `shouldShowBrushingProgram emits false if toothbrushesWithBrushingProgramSupport returns empty list`() {
        whenever(toothbrushesUseCase.toothbrushesWithBrushingProgramSupport(CURRENT_PROFILE_ID))
            .thenReturn(Single.just(listOf()))

        brushingProgramUseCase
            .shouldShowBrushingProgram(CURRENT_PROFILE_ID)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(false)
    }

    @Test
    fun `shouldShowBrushingProgram emits true if toothbrushesWithBrushingProgramSupport returns list with content`() {
        val connection = mock<KLTBConnection>()

        whenever(toothbrushesUseCase.toothbrushesWithBrushingProgramSupport(CURRENT_PROFILE_ID))
            .thenReturn(Single.just(listOf(connection)))

        brushingProgramUseCase
            .shouldShowBrushingProgram(CURRENT_PROFILE_ID)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(true)
    }

    @Test
    fun `shouldShowBrushingProgram reacts to profile changes`() {
        val profile1 = ProfileBuilder.create().withId(1).build()
        val profile2 = ProfileBuilder.create().withId(2).build()
        val profile3 = ProfileBuilder.create().withId(3).build()

        whenever(currentProfileProvider.currentProfileFlowable())
            .thenReturn(Flowable.just(profile1, profile2, profile3))

        whenever(toothbrushesUseCase.toothbrushesWithBrushingProgramSupport(any()))
            .thenReturn(Single.just(listOf()))

        brushingProgramUseCase.shouldShowBrushingProgram().test()

        for (profile in listOf(profile1, profile2, profile3)) {
            verify(toothbrushesUseCase).toothbrushesWithBrushingProgramSupport(profile.id)
        }
    }
}

private const val CURRENT_PROFILE_ID = 1986L
