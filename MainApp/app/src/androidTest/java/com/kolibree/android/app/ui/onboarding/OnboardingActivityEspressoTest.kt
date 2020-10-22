/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.app.dagger.EspressoGoogleSignInModule
import com.kolibree.android.app.dagger.EspressoSingleThreadSchedulerModule
import com.kolibree.android.test.BaseEspressoTest
import com.kolibree.android.test.KLBaseActivityTestRule
import com.kolibree.android.test.KolibreeActivityTestRule
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.sdkws.data.request.CreateAccountData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit

internal abstract class OnboardingActivityEspressoTest : BaseEspressoTest<OnboardingActivity>() {

    private val scheduler = TestScheduler()

    protected fun launchActivity(skipSplash: Boolean = true) {

        activityTestRule.launchActivity(null)
        if (skipSplash) skipSplash()
    }

    protected fun emulateMagicLinkClick() {
        activityTestRule.launchActivity(magicLinkIntent())
    }

    fun advanceTimeBySeconds(seconds: Long) {
        EspressoSingleThreadSchedulerModule.scheduler.advanceTimeBy(
            seconds,
            TimeUnit.SECONDS
        )
    }

    protected fun skipSplash() {
        advanceTimeBySeconds(SPLASH_DURATION.seconds)
        IdlingResourceFactory.viewVisibility(
            R.id.get_ready_container,
            View.VISIBLE
        ).waitForIdle()

        onView(withId(R.id.get_ready_container)).check(matches(isDisplayed()))
    }

    protected fun prepareMocksForSuccessfulEmailLogin(email: String) {
        whenever(component().kolibreeConnector().validateMagicLinkCode("imacode"))
            .thenReturn(Single.just("imacode"))

        whenever(component().kolibreeConnector().login("imacode"))
            .thenReturn(Completable.complete())

        whenever(component().kolibreeConnector().requestMagicLink(email))
            .thenReturn(Completable.complete())
    }

    private fun magicLinkIntent(): Intent {
        val intent = Intent(context(), OnboardingActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data =
            Uri.parse("https://kolibree.com/validate_code/kolibree/?app=kolibree&code=imacode")
        return intent
    }

    protected fun prepareMocksForSuccessfulGoogleLogin() {
        prepareGoogleSignInWrapper()

        whenever(component().kolibreeConnector().loginByGoogle(any()))
            .thenReturn(Completable.complete())
    }

    protected fun prepareMocksForUnsuccessfulGoogleLogin(e: Exception) {
        prepareGoogleSignInWrapper()

        whenever(component().kolibreeConnector().loginByGoogle(any()))
            .thenReturn(Completable.error(e))
    }

    private fun prepareGoogleSignInWrapper() {
        whenever(
            EspressoGoogleSignInModule.wrapperMock.maybeFillDataForLogin(
                any(),
                any()
            )
        )
            .thenAnswer {
                it.getArgument(1, CreateAccountData.Builder::class.java)
                    .setGoogleId("1")
                    .setGoogleIdToken("token")
                    .setEmail("test@test.com")
                return@thenAnswer true
            }
    }

    override fun setUp() {
        super.setUp()
        AppMocker.create()
            .prepareForMainScreen()
            .mock()
    }

    override fun createRuleForActivity(): KLBaseActivityTestRule<OnboardingActivity> {
        return KolibreeActivityTestRule.Builder(OnboardingActivity::class.java)
            .launchActivity(false)
            .build()
    }
}
