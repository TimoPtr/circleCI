/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.insets.withWindowInsets
import com.kolibree.android.app.ui.home.BottomNavigationTab.ACTIVITIES
import com.kolibree.android.app.ui.home.BottomNavigationTab.DASHBOARD
import com.kolibree.android.app.ui.home.BottomNavigationTab.PROFILE
import com.kolibree.android.app.ui.home.BottomNavigationTab.SHOP
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.navigation.horizontal.HorizontalNavigationController
import com.kolibree.android.app.ui.navigation.horizontal.HorizontalNavigationItem
import com.kolibree.android.app.ui.navigation.horizontal.itemForTriggerId
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityHomeScreenBinding
import com.kolibree.android.shop.presentation.checkout.extractCheckout
import com.kolibree.android.shop.presentation.checkout.isCheckoutFlow
import com.kolibree.android.shop.presentation.checkout.isCheckoutProcessing
import com.kolibree.android.shop.presentation.checkout.isVisitShop
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.NonTrackableScreen
import javax.inject.Inject

@VisibleForApp
class HomeScreenActivity : BaseMVIActivity<
    HomeScreenViewState,
    HomeScreenAction,
    HomeScreenViewModel.Factory,
    HomeScreenViewModel,
    ActivityHomeScreenBinding>(), NonTrackableScreen {

    @Inject
    internal lateinit var featureToggles: FeatureToggleSet

    @Inject
    internal lateinit var homeNavigator: HumHomeNavigator

    private lateinit var horizontalNavigationController: HorizontalNavigationController

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()

        super.onCreate(savedInstanceState)

        setupView()
        viewModel.getViewState()?.let {
            handleNewProfile(it.currentProfile)
            bottomNavigationItems.itemForTriggerId(it.selectedTabId)?.let { navigationItem ->
                handleBottomNavigation(navigationItem)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val handled = homeNavigator.onActivityResult(requestCode, resultCode, data)
        if (!handled) {
            when {
                isCheckoutFlow(requestCode) && isCheckoutProcessing(resultCode) ->
                    extractCheckout(data)?.let {
                        viewModel.userFinishedCheckoutFlow(it)
                    }
                isCheckoutFlow(requestCode) && isVisitShop(resultCode) ->
                    handleBottomNavigation(shopBottomNavigationItem)
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @SuppressWarnings("ComplexMethod")
    override fun execute(action: HomeScreenAction) {
        when (action) {
            is CurrentProfileChanged -> handleNewProfile(action.profile)
            is ShowProgressDialog -> showProgressDialog()
            is HideProgressDialog -> hideProgressDialog()
            else -> FailEarly.fail("Action $action was not handled")
        }
    }

    private fun handleNewProfile(profile: Profile) {
        binding.bottomNavigation.menu.findItem(R.id.bottom_navigation_profile)
            ?.let { accountMenuItem -> accountMenuItem.title = profile.firstName }
    }

    private fun showProgressDialog() {
        // TODO use HUM dialogs
    }

    private fun hideProgressDialog() {
        // TODO use HUM dialogs
    }

    override fun getViewModelClass(): Class<HomeScreenViewModel> =
        HomeScreenViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_home_screen

    private fun setupView() {
        setupHorizontalNavigation()
    }

    override fun onBackPressed() {
        when {
            viewModel.onBackPressed() -> Unit
            horizontalNavigationController.navigateBack() -> syncBottomNavigationState()
            else -> super.onBackPressed()
        }
    }

    private fun <T : Fragment> handleBottomNavigation(
        item: HorizontalNavigationItem<T>,
        andThen: (T) -> Unit = {}
    ) {
        horizontalNavigationController.navigateTo(item) { targetFragment ->
            syncBottomNavigationState()
            andThen(targetFragment)
        }
    }

    private var lastScreenId = R.id.bottom_navigation_home

    private fun setupHorizontalNavigation() {
        horizontalNavigationController = HorizontalNavigationController(
            this,
            binding.rootContentLayout,
            supportFragmentManager,
            navigationItems = bottomNavigationItems,
            startingItem = dashboardBottomNavigationItem
        )
        with(binding) {
            withWindowInsets(binding.rootContentLayout) {
                viewModel?.setTopOffset(topStatusBarWindowInset())
                bottomNavigation.updatePadding(0, 0, 0, bottomNavigationBarInset())
            }
            bottomNavigation.setOnNavigationItemSelectedListener { item ->
                val target = bottomNavigationItems.first { it.matchesTrigger(item.itemId) }
                if (horizontalNavigationController.currentItem == target) {
                    return@setOnNavigationItemSelectedListener true
                }

                handleBottomNavigation(target)

                viewModel?.tabSelected(item.itemId)

                if (item.itemId == R.id.bottom_navigation_profile) {
                    profileTabVisible()
                }
                if (item.itemId == R.id.bottom_navigation_home) {
                    homeTabVisible()
                }

                sendNavigationAnalyticsEvent(item.itemId, lastScreenId)
                lastScreenId = item.itemId

                true
            }
        }
    }

    private fun sendNavigationAnalyticsEvent(currentScreenId: Int, lastScreenId: Int) {
        val fromScreen = screenNameEvent(lastScreenId)
        val toScreen = screenNameEvent(currentScreenId)
        HomeScreenAnalytics.navigationEvent(fromScreen, toScreen)
    }

    private fun screenNameEvent(screenId: Int): AnalyticsEvent = when (screenId) {
        R.id.bottom_navigation_home -> HomeScreenAnalytics.main()
        R.id.bottom_navigation_shop -> HomeScreenAnalytics.shop()
        R.id.bottom_navigation_activities -> HomeScreenAnalytics.activities()
        else -> HomeScreenAnalytics.profile()
    }

    private fun profileTabVisible() {
        viewModel.synchronize()
    }

    private fun homeTabVisible() {
        viewModel.synchronize()
    }

    private fun syncBottomNavigationState() {
        binding.bottomNavigation.selectedItemId =
            horizontalNavigationController.currentItem.triggerId
    }

    @VisibleForApp
    fun openTab(tab: BottomNavigationTab) {
        when (tab) {
            DASHBOARD -> handleBottomNavigation(dashboardBottomNavigationItem)
            SHOP -> handleBottomNavigation(shopBottomNavigationItem)
            ACTIVITIES -> handleBottomNavigation(activitiesBottomNavigationItem)
            PROFILE -> handleBottomNavigation(profileBottomNavigationItem)
        }
    }

    fun showSmileCounter() {
        val alreadyOnDashboard =
            horizontalNavigationController.currentItem == dashboardBottomNavigationItem
        handleBottomNavigation(dashboardBottomNavigationItem) { dashboardFragment ->
            dashboardFragment.showSmileCounter(animate = alreadyOnDashboard)
        }
    }

    fun notifyTestBrushingFinished() {
        viewModel.onTestBrushingFinished()
    }

    fun onBluetoothPermissionRetrieved(permissionGranted: Boolean) {
        viewModel.onBluetoothPermissionRetrieved(permissionGranted)
    }

    fun onLocationPermissionRetrieved(permissionGranted: Boolean) {
        viewModel.onLocationPermissionRetrieved(permissionGranted)
    }

    fun onLocationSettingsClosed() {
        viewModel.onLocationSettingsClosed()
    }

    fun onCelebrationScreenClosed() {
        viewModel.onCelebrationScreenClosed()
    }

    fun onLowBatteryDismissed() {
        viewModel.onLowBatteryDismissed()
    }
}

@Keep
fun startHomeScreenIntent(context: Context) {
    context.startActivity(Intent(context, HomeScreenActivity::class.java))
}
