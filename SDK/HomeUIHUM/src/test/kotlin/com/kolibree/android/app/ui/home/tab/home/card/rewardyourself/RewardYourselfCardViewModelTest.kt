/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.rewardyourself

import androidx.lifecycle.Lifecycle
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.domain.RewardYourselfItemsUseCase
import com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.domain.UserCreditsUseCase
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.presentation.list.ShopListScrollUseCase
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.verify

class RewardYourselfCardViewModelTest : BaseUnitTest() {

    private val rewardYourselfItemsUseCase: RewardYourselfItemsUseCase = mock()
    private val userCreditsUseCase: UserCreditsUseCase = mock()
    private val humHomeNavigator: HumHomeNavigator = mock()
    private val shopListScrollUseCase: ShopListScrollUseCase = mock()
    private val currentProfileProvider: CurrentProfileProvider = mock()
    private lateinit var viewModel: RewardYourselfCardViewModel

    override fun setup() {
        super.setup()
        viewModel = RewardYourselfCardViewModel(
            RewardYourselfCardViewState.initial(DynamicCardPosition.ZERO),
            rewardYourselfItemsUseCase,
            userCreditsUseCase,
            humHomeNavigator,
            shopListScrollUseCase,
            currentProfileProvider
        )
    }

    @Test
    fun `do not display card if points are not available`() {
        prepareMocks(
            profile = mockProfile("Aurelien"),
            items = listOf(mockItem(1))
        )

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertFalse(viewModel.getViewState()!!.visible)
    }

    @Test
    fun `do not display card if items are not available`() {
        prepareMocks(
            profile = mockProfile("Timothy"),
            userCredits = Price.create(1, Currency.getInstance(Locale.getDefault()))
        )

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertFalse(viewModel.getViewState()!!.visible)
    }

    @Test
    fun `do not display card if item list is empty`() {
        prepareMocks(
            profile = mockProfile("Chaofan"),
            items = emptyList(),
            userCredits = Price.create(1, Currency.getInstance(Locale.getDefault()))
        )

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertFalse(viewModel.getViewState()!!.visible)
    }

    @Test
    fun `do not display card if name is not available`() {
        prepareMocks(
            items = listOf(mockItem(1)),
            userCredits = Price.create(1, Currency.getInstance(Locale.getDefault()))
        )

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertFalse(viewModel.getViewState()!!.visible)
    }

    @Test
    fun `display card if data available`() {
        prepareMocks(
            profile = mockProfile("Kornel"),
            items = listOf(mockItem(1)),
            userCredits = Price.create(1, Currency.getInstance(Locale.getDefault()))
        )

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertTrue(viewModel.getViewState()!!.visible)
    }

    @Test
    fun `update view state if data available`() {
        val mockName = "Lukasz"
        val mockItems = (0..10).map(::mockItem)
        val mockCredits = Price.create(BigDecimal(12), Currency.getInstance(Locale.getDefault()))

        prepareMocks(
            profile = mockProfile(mockName),
            items = mockItems,
            userCredits = mockCredits
        )

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val viewState = viewModel.getViewState()!!
        assertEquals(mockName, viewState.userName)
        assertEquals(mockCredits, viewState.userCredits)
        assertEquals(mockItems, viewState.items.map { it.item })
    }

    @Test
    fun `navigates to shop tab after item click`() {
        val mockItem = mockItem(1)
        viewModel.onItemClick(mockItem)

        verify(humHomeNavigator).navigateToShopTab()
        verify(shopListScrollUseCase).scrollToItem(mockItem.id)
    }

    private fun prepareMocks(
        profile: Profile? = null,
        items: List<RewardYourselfItem>? = null,
        userCredits: Price? = null
    ) {
        whenever(currentProfileProvider.currentProfileFlowable())
            .thenReturn(profile?.let { Flowable.just(it) } ?: Flowable.empty())

        whenever(rewardYourselfItemsUseCase.getRewardItems())
            .thenReturn(items?.let { Flowable.just(it) } ?: Flowable.empty())

        whenever(userCreditsUseCase.getUserCredits())
            .thenReturn(userCredits?.let { Flowable.just(it) } ?: Flowable.empty())
    }

    private fun mockItem(id: Int) = RewardYourselfItem(
        id = "ID $id",
        imageUrl = "www.imageurl.com/$id",
        name = "Name $id",
        price = Price.create(BigDecimal(id), Currency.getInstance(Locale.getDefault()))
    )

    private fun mockProfile(name: String) = ProfileBuilder.create()
        .withName(name)
        .build()
}
