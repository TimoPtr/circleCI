/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

internal class BrushingProgramToothbrushesUseCaseTest : BaseUnitTest() {
    private val toothbrushRepository = mock<ToothbrushRepository>()
    private val connectionProvider = mock<KLTBConnectionProvider>()

    private lateinit var brushingProgramUseCase: BrushingProgramToothbrushesUseCase

    override fun setup() {
        super.setup()

        brushingProgramUseCase =
            BrushingProgramToothbrushesUseCase(toothbrushRepository, connectionProvider)
    }

    /*
    profileToothbrushesWithBrushingProgram
     */
    @Test
    fun `profileKnownToothbrushes returns empty list if toothbrushRepository has no TB for profile`() {
        mockToothbrushRepositoryListAll(listOf())

        brushingProgramUseCase.profileToothbrushesWithBrushingProgram(CURRENT_PROFILE_ID)
            .test()
            .assertValue(listOf())
            .assertNoErrors()
    }

    @Test
    fun `profileKnownToothbrushes returns empty list if toothbrushRepository has TBs associated to a different profile`() {
        mockToothbrushRepositoryListAll(listOf())

        mockToothbrushRepositoryListAll(
            listOf(
                createFakeAccountToothbrush(OTHER_PROFILE_ID, ToothbrushModel.CONNECT_B1)
            )
        )

        brushingProgramUseCase.profileToothbrushesWithBrushingProgram(CURRENT_PROFILE_ID)
            .test()
            .assertValue(listOf())
            .assertNoErrors()
    }

    @Test
    fun `profileKnownToothbrushes emits single toothbrush list when both profiles own VSU-enabled devices`() {
        val expectedTB = createFakeAccountToothbrush(CURRENT_PROFILE_ID, ToothbrushModel.CONNECT_E2)
        mockToothbrushRepositoryListAll(
            listOf(
                createFakeAccountToothbrush(OTHER_PROFILE_ID, ToothbrushModel.CONNECT_B1),
                expectedTB
            )
        )

        brushingProgramUseCase
            .profileToothbrushesWithBrushingProgram(CURRENT_PROFILE_ID)
            .test()
            .assertValue(listOf(expectedTB))
    }

    @Test
    fun `profileKnownToothbrushes emits single toothbrush list when the profile owns two VSU-enabled devices`() {
        val expectedList = listOf(
            createFakeAccountToothbrush(CURRENT_PROFILE_ID, ToothbrushModel.CONNECT_B1),
            createFakeAccountToothbrush(CURRENT_PROFILE_ID, ToothbrushModel.CONNECT_E2)
        )
        mockToothbrushRepositoryListAll(expectedList)

        brushingProgramUseCase
            .profileToothbrushesWithBrushingProgram(CURRENT_PROFILE_ID)
            .test()
            .assertValue(expectedList)
    }

    /*
    toothbrushesWithBrushingProgramSupport
     */
    @Test
    fun `toothbrushesWithBrushingProgramSupport returns empty list if toothbrushRepository has no TB for profile`() {
        mockToothbrushRepositoryListAll(listOf())

        spyTestInstnace()

        doReturn(Single.just(listOf<AccountToothbrush>()))
            .whenever(brushingProgramUseCase)
            .profileToothbrushesWithBrushingProgram(CURRENT_PROFILE_ID)

        brushingProgramUseCase.toothbrushesWithBrushingProgramSupport(CURRENT_PROFILE_ID)
            .test()
            .assertValue(listOf())
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `toothbrushesWithBrushingProgramSupport fetches each toothbrush from KLTBConnectionProvider`() {
        mockToothbrushRepositoryListAll(listOf())

        spyTestInstnace()

        val profileToothbrush1 = mock<AccountToothbrush>()
        val mac1 = "mac1"
        whenever(profileToothbrush1.mac).thenReturn(mac1)

        val profileToothbrush2 = mock<AccountToothbrush>()
        val mac2 = "mac2"
        whenever(profileToothbrush2.mac).thenReturn(mac2)

        doReturn(Single.just(listOf(profileToothbrush1, profileToothbrush2)))
            .whenever(brushingProgramUseCase)
            .profileToothbrushesWithBrushingProgram(CURRENT_PROFILE_ID)

        val kltbConnection1 = mock<KLTBConnection>()
        val kltbConnection2 = mock<KLTBConnection>()
        whenever(connectionProvider.getKLTBConnectionSingle(mac1)).thenReturn(
            Single.just(
                kltbConnection1
            )
        )
        whenever(connectionProvider.getKLTBConnectionSingle(mac2)).thenReturn(
            Single.just(
                kltbConnection2
            )
        )

        brushingProgramUseCase.toothbrushesWithBrushingProgramSupport(CURRENT_PROFILE_ID)
            .test()
            .assertValue(listOf(kltbConnection1, kltbConnection2))
            .assertNoErrors()
    }

    /*
    Utils
     */

    private fun mockToothbrushRepositoryListAll(list: List<AccountToothbrush>) =
        whenever(toothbrushRepository.listAll()).thenReturn(Single.just(list))

    private fun createFakeAccountToothbrush(profileId: Long, toothbrushModel: ToothbrushModel) =
        AccountToothbrush("mac", "name", toothbrushModel, 1L, profileId)

    private fun spyTestInstnace() {
        brushingProgramUseCase = spy(brushingProgramUseCase)
    }
}

private const val CURRENT_PROFILE_ID = 1986L

private const val OTHER_PROFILE_ID = 1983L
