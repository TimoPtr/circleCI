/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.extensions.onAnimationEnd
import com.kolibree.android.app.insets.WindowInsetsMediator
import com.kolibree.android.app.insets.WindowInsetsOwner
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.ui.activity.getCurrentNavFragment
import com.kolibree.android.app.ui.home.startHomeScreenIntent
import com.kolibree.android.app.ui.onboarding.OnboardingAnalytics.goBack
import com.kolibree.android.app.ui.onboarding.emailcheck.EmailCheckFragment
import com.kolibree.android.app.ui.onboarding.getready.GetReadyFragment
import com.kolibree.android.app.ui.onboarding.login.LoginFragment
import com.kolibree.android.app.ui.widget.showUnderConstructionToast
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityOnboardingBinding
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.NonTrackableScreen
import com.kolibree.android.tracker.TrackableScreen
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@VisibleForApp
class OnboardingActivity : BaseMVIActivity<
    OnboardingSharedViewState,
    OnboardingActions,
    OnboardingActivityViewModel.Factory,
    OnboardingActivityViewModel,
    ActivityOnboardingBinding
    >(), NonTrackableScreen, WindowInsetsOwner {

    private lateinit var windowInsetMediator: WindowInsetsMediator

    override fun withWindowInsets(block: (WindowInsets) -> Unit) {
        windowInsetMediator.withWindowInsets(block)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()

        super.onCreate(savedInstanceState)
        windowInsetMediator = WindowInsetsMediator(binding.rootContentLayout)
        windowInsetMediator.withWindowInsets { insets ->
            binding.toolbar.setPadding(0, insets.topStatusBarWindowInset(), 0, 0)
            binding.progressView.setPadding(
                0,
                insets.topStatusBarWindowInset(),
                0,
                insets.bottomNavigationBarInset()
            )
        }
    }

    @VisibleForTesting(otherwise = PROTECTED)
    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onMagicLinkIntent(intent)
    }

    override fun getViewModelClass(): Class<OnboardingActivityViewModel> =
        OnboardingActivityViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_onboarding

    override fun execute(action: OnboardingActions) = when (action) {
        is OnboardingActivityAction.ShowUnderConstructionMessage -> showUnderConstructionToast()
        is OnboardingActivityAction.OpenHomeScreen -> goHome()
        is OnboardingActivityAction.RestartLoginFlow -> restartLoginFlow()
        else -> FailEarly.fail("Action not recognized")
    }

    private fun restartLoginFlow() {
        when (getCurrentlyVisibleFragment()) {
            is EmailCheckFragment -> findNavController(this, R.id.nav_host_fragment).popBackStack()
            is LoginFragment -> {
                /* no-op */
            }
            is GetReadyFragment -> {
                /* no-op */
            }
            else -> FailEarly.fail(
                "We're in ${getCurrentlyVisibleFragment()?.javaClass?.canonicalName} " +
                    "which cannot be handled"
            )
        }
    }

    override fun onBackPressed() {
        when {
            // we need to check the sharedViewState in order to check
            // if any fragment from PairingFlow is displaying a progressDialog
            viewModel.getSharedViewState()?.progressVisible() == true -> { // no-op
            }
            getCurrentlyVisibleFragment() is GetReadyFragment -> finish()
            else -> {
                Analytics.send(
                    ((getCurrentlyVisibleFragment() as? TrackableScreen)?.let {
                        it.getScreenName() + goBack()
                    } ?: goBack()))
                super.onBackPressed()
            }
        }
    }

    private fun getCurrentlyVisibleFragment(): Fragment? =
        getCurrentNavFragment(R.id.nav_host_fragment)

    private fun goHome() {
        lifecycleScope.launch {
            viewModel.onUserNavigatingHome()
            showSuccess()
            startHomeScreenIntent(this@OnboardingActivity)
            finish()
        }
    }

    private suspend fun showSuccess() = suspendCancellableCoroutine<Unit> { continuation ->
        if (!binding.progressView.isShown) {
            continuation.resume(Unit)
        } else {
            binding.successIndicator.apply {
                alpha = 0.0F
                visibility = View.VISIBLE
                onAnimationEnd {
                    continuation.resume(Unit)
                }

                animate().alpha(1.0f).start()
                playAnimation()
            }
        }
    }
}

@Keep
fun startHumOnboardingIntent(context: Context) {
    context.startActivity(Intent(context, OnboardingActivity::class.java))
}
