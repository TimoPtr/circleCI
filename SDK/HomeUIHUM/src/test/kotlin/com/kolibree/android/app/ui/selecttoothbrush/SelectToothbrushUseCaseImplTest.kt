/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selecttoothbrush

import com.kolibree.account.utils.ToothbrushesForProfileUseCase
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Maybe
import org.junit.Test

class SelectToothbrushUseCaseImplTest : BaseUnitTest() {

    private val toothbrushProvider: ToothbrushesForProfileUseCase = mock()
    private val navigator: SelectToothbrushNavigator = mock()
    private val iconProvider: SelectToothbrushIconProvider = mock()

    private lateinit var useCase: SelectToothbrushUseCase

    override fun setup() {
        super.setup()
        useCase = SelectToothbrushUseCaseImpl(
            toothbrushProvider, navigator, iconProvider
        )
    }

    @Test
    fun `do not show picker if no toothbrush available`() {
        whenever(toothbrushProvider.currentProfileToothbrushesOnceAndStream())
            .thenReturn(Flowable.empty())

        val testObserver = useCase.selectToothbrush().test()

        verifyZeroInteractions(navigator)
        verifyZeroInteractions(iconProvider)

        testObserver.assertValueCount(0)
        testObserver.assertComplete()
    }

    @Test
    fun `do not show picker if only one toothbrush available`() {
        val testToothbrush = testConnections.first()

        whenever(toothbrushProvider.currentProfileToothbrushesOnceAndStream())
            .thenReturn(Flowable.just(listOf(testToothbrush)))

        val testObserver = useCase.selectToothbrush().test()

        verifyZeroInteractions(navigator)
        verifyZeroInteractions(iconProvider)

        testObserver.assertValue(testToothbrush)
        testObserver.assertComplete()
    }

    @Test
    fun `loads icons for all toothbrushes`() {
        whenever(toothbrushProvider.currentProfileToothbrushesOnceAndStream())
            .thenReturn(Flowable.just(testConnections))

        whenever(navigator.selectToothbrush(any()))
            .thenReturn(Maybe.empty())

        val testObserver = useCase.selectToothbrush().test()

        for (testBrush in testConnections) {
            verify(iconProvider).getIconFor(testBrush)
        }

        testObserver.assertComplete()
    }

    @Test
    fun `displays all available toothbrushes`() {
        val mockResId = 0
        val expectedToothbrushInfoList = testConnections.map {
            SelectToothbrushItem(it, mockResId)
        }

        whenever(toothbrushProvider.currentProfileToothbrushesOnceAndStream())
            .thenReturn(Flowable.just(testConnections))

        whenever(iconProvider.getIconFor(any()))
            .thenReturn(mockResId)

        whenever(navigator.selectToothbrush(any()))
            .thenReturn(Maybe.empty())

        val testObserver = useCase.selectToothbrush().test()

        verify(navigator).selectToothbrush(expectedToothbrushInfoList)

        testObserver.assertComplete()
    }

    @Test
    fun `returns selected toothbrush`() {
        val selectedIndex = 3
        val selectedToothbrush = testConnections[selectedIndex]

        whenever(toothbrushProvider.currentProfileToothbrushesOnceAndStream())
            .thenReturn(Flowable.just(testConnections))

        whenever(navigator.selectToothbrush(any())).thenAnswer {
            val toothbrushes = it.getArgument(0) as List<SelectToothbrushItem>
            Maybe.just(toothbrushes[selectedIndex])
        }

        val testObserver = useCase.selectToothbrush().test()

        testObserver.assertValue(selectedToothbrush)
        testObserver.assertComplete()
    }

    private val testConnections = listOf(
        mockConnection(
            name = "Brush 1",
            model = ToothbrushModel.ARA
        ),
        mockConnection(
            name = "Brush 2",
            model = ToothbrushModel.CONNECT_E2
        ),
        mockConnection(
            name = "Brush 3",
            model = ToothbrushModel.PLAQLESS
        ),
        mockConnection(
            name = "Brush 4",
            model = ToothbrushModel.GLINT
        ),
        mockConnection(
            name = "Brush 5",
            model = ToothbrushModel.HUM_BATTERY
        )
    )

    private fun mockConnection(
        name: String,
        model: ToothbrushModel
    ): KLTBConnection {
        return KLTBConnectionBuilder.createAndroidLess()
            .withName(name)
            .withModel(model)
            .build()
    }
}
