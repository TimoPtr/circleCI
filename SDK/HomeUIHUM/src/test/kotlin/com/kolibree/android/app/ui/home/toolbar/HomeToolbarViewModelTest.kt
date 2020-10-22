/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbar

import androidx.lifecycle.Lifecycle
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewState.Companion.initial
import com.kolibree.android.app.ui.home.tracker.BottomNavigationEventTracker
import com.kolibree.android.app.ui.selectprofile.SelectProfileDialogUseCase
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.livedata.setTestValue
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class HomeToolbarViewModelTest : BaseUnitTest() {

    private val cartRepository: CartRepository = mock()
    private val homeNavigator: HumHomeNavigator = mock()
    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val selectProfileDialogUseCase: SelectProfileDialogUseCase = mock()

    private lateinit var viewModel: HomeToolbarViewModel

    override fun setup() {
        super.setup()
        viewModel = HomeToolbarViewModel(
            initial(),
            homeNavigator,
            cartRepository,
            currentProfileProvider,
            selectProfileDialogUseCase
        )
        whenever(currentProfileProvider.currentProfileFlowable())
            .thenReturn(Flowable.never())
    }

    /*
    onStart
     */
    @Test
    fun `onStart subscribes to getCartProductsCount and invokes onNewCartProductsCount`() {
        whenever(cartRepository.getCartProductsCount()).thenReturn(Flowable.just(1))

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)

        verify(cartRepository).getCartProductsCount()
    }

    /*
    toolbarToothbrushIconClick
     */

    @Test
    fun `toolbarToothbrushIconClick navigates to SetupToothbrush screen if no toothbrush added`() {
        viewModel.toolbarIconResult.setTestValue(ToolbarIconResult(ToolbarIcon.NoToothbrush))

        viewModel.toolbarToothbrushIconClick()

        verify(homeNavigator).navigateToSetupToothbrushScreen()
    }

    @Test
    fun `toolbarToothbrushIconClick navigates to Toothbrush settings screen if has single toothbrush`() {
        val mac = "mac:tb:01"
        viewModel.toolbarIconResult.setTestValue(
            ToolbarIconResult(
                toolbarIcon = ToolbarIcon.ToothbrushConnected,
                relatedMacAddress = mac
            )
        )

        viewModel.toolbarToothbrushIconClick()

        verify(homeNavigator).navigateToToothbrushScreen(mac)
    }

    @Test
    fun `toolbarToothbrushIconClick send event when current page is dashboard`() {
        BottomNavigationEventTracker.dashboardVisible()

        viewModel.toolbarToothbrushIconClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Dashboard_TBSetting_Menu"))
    }

    @Test
    fun `toolbarToothbrushIconClick send event when current page is activities`() {
        BottomNavigationEventTracker.activitiesVisible()

        viewModel.toolbarToothbrushIconClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Activities_TBSetting_Menu"))
    }

    @Test
    fun `toolbarToothbrushIconClick send event when current page is profile`() {
        BottomNavigationEventTracker.profileVisible()

        viewModel.toolbarToothbrushIconClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_TBSetting_Menu"))
    }

    @Test
    fun `toolbarToothbrushIconClick send event when current page is shop`() {
        BottomNavigationEventTracker.shopVisible()

        viewModel.toolbarToothbrushIconClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Shop_TBSetting_Menu"))
    }

    /*
    toolbarCartIconClick
     */
    @Test
    fun `toolbarCartIconClick navigates to Checkout`() {
        viewModel.toolbarCartIconClick()

        verify(homeNavigator).showCheckoutScreen()
    }

    @Test
    fun `toolbarCartIconClick send event when current page is dashboard`() {
        BottomNavigationEventTracker.dashboardVisible()

        viewModel.toolbarCartIconClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Dashboard_GoToShop"))
    }

    @Test
    fun `toolbarCartIconClick send event when current page is activities`() {
        BottomNavigationEventTracker.activitiesVisible()

        viewModel.toolbarCartIconClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Activities_GoToShop"))
    }

    @Test
    fun `toolbarCartIconClick send event when current page is profile`() {
        BottomNavigationEventTracker.profileVisible()

        viewModel.toolbarCartIconClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Setting_GoToShop"))
    }

    @Test
    fun `toolbarCartIconClick send event when current page is shop`() {
        BottomNavigationEventTracker.shopVisible()

        viewModel.toolbarCartIconClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Shop_GoToShop"))
    }

    /*
    onProfileNameClick
     */
    @Test
    fun `onProfileNameClick executed SelectProfile use case`() {
        whenever(selectProfileDialogUseCase.showDialogAndHandleSelectedItem())
            .thenReturn(Completable.complete())

        viewModel.onProfileNameClick()

        verify(selectProfileDialogUseCase).showDialogAndHandleSelectedItem()
    }

    /*
    isToothbrushMacAddress
     */

    @Test
    fun `isToothbrushMacAddress returns true if mac is not empty`() {
        viewModel.toolbarIconResult.setTestValue(
            ToolbarIconResult(relatedMacAddress = "not:empty:mac")
        )
        assertTrue(viewModel.isToothbrushMacAddress())
    }

    @Test
    fun `isToothbrushMacAddress returns false if mac is empty`() {
        viewModel.toolbarIconResult.setTestValue(
            ToolbarIconResult(relatedMacAddress = "")
        )
        assertFalse(viewModel.isToothbrushMacAddress())
    }

    /*
    isNoToothbrush
     */

    @Test
    fun `isNoToothbrush returns true if icon is null`() {
        assertTrue(viewModel.isNoToothbrush(null))
    }

    @Test
    fun `isNoToothbrush returns true if icon NoToothbrush`() {
        assertTrue(
            viewModel.isNoToothbrush(
                ToolbarIconResult(
                    ToolbarIcon.NoToothbrush
                )
            )
        )
    }

    @Test
    fun `isNoToothbrush returns false if not icon NoToothbrush`() {
        assertFalse(
            viewModel.isNoToothbrush(
                ToolbarIconResult(
                    ToolbarIcon.ToothbrushConnected
                )
            )
        )
        assertFalse(
            viewModel.isNoToothbrush(
                ToolbarIconResult(
                    ToolbarIcon.ToothbrushConnectedOta
                )
            )
        )
        assertFalse(
            viewModel.isNoToothbrush(
                ToolbarIconResult(
                    ToolbarIcon.ToothbrushDisconnected
                )
            )
        )
    }
}
