/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.container

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.feature.ShowShopTabsFeature
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.rewards.SmilesUseCase
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.domain.model.StoreDetails
import com.kolibree.android.shop.presentation.list.ShopListScrollUseCase
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class ShopContainerViewModelTest : BaseUnitTest() {

    private val storeDetails = mock<StoreDetails>()

    private val initialViewState = ShopContainerViewState.initial()

    private val shopifyClientWrapper: ShopifyClientWrapper = mock()

    private val smilesUseCase: SmilesUseCase = mock()

    private val shopListScrollUseCase: ShopListScrollUseCase = mock()

    private lateinit var viewModel: ShopContainerViewModel

    override fun setup() {
        whenever(shopifyClientWrapper.getStoreDetails()).thenReturn(Single.just(storeDetails))
        whenever(shopListScrollUseCase.getItemIdToScroll()).thenReturn(Observable.empty())
        whenever(smilesUseCase.smilesAmountStream())
            .thenReturn(Flowable.never())

        viewModel = ShopContainerViewModel(
            initialViewState,
            shopifyClientWrapper,
            smilesUseCase,
            shopListScrollUseCase,
            setOf(ConstantFeatureToggle(ShowShopTabsFeature, initialValue = true))
        )
    }

    @Before
    fun doBefore() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    @After
    fun doAfter() {
        FailEarly.overrideDelegateWith(TestDelegate)
    }

    @Test
    fun `onCreate subscribes to all expected data streams`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(shopifyClientWrapper).getStoreDetails()
        verify(smilesUseCase).smilesAmountStream()
        verify(shopifyClientWrapper).getStoreDetails()
    }

    @Test
    fun `fetchStoreDetails updates view state`() {
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertEquals(storeDetails, viewModel.getViewState()!!.storeDetails)
    }

    @Test
    fun `onCloseDiscountBannerClick emits new state with discountBannerVisible false`() {
        val testObserver = viewModel.viewStateFlowable.test()

        viewModel.onCloseDiscountBannerClick()

        testObserver.assertLastValueWithPredicate { it.discountBannerVisible.not() }
    }

    @Test
    fun `watchCurrentAmountOfSmiles emits new state with current smiles count`() {
        val testObserver = viewModel.viewStateFlowable.test()

        val expectedSmilesCount = 1986

        whenever(smilesUseCase.smilesAmountStream())
            .thenReturn(Flowable.just(expectedSmilesCount))

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        testObserver
            .assertNotComplete()
            .assertNoErrors()
            .assertLastValueWithPredicate { it.currentSmilesCount == expectedSmilesCount }
    }

    @Test
    fun `switch to product tab if scroll to product request came`() {
        val actionTestObservable = viewModel.actionsObservable.test()

        whenever(shopListScrollUseCase.getItemIdToScroll())
            .thenReturn(Observable.fromIterable(listOf("0", "1", "2")))

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        actionTestObservable.assertValues(
            ShopContainerActions.SwitchToProductTab,
            ShopContainerActions.SwitchToProductTab,
            ShopContainerActions.SwitchToProductTab
        )
    }
}
