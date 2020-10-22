/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.android.app.dagger.EspressoSingleThreadSchedulerModule
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.commons.AvailableUpdate.Companion.create
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.UpdateType
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_INSTALLING
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.Companion.fromProgressiveAction
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withDrawable
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withProgress
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.createAndroidLess
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.createWithDefaultState
import com.kolibree.android.test.mocks.createGruwareData
import com.kolibree.android.test.utils.createDummyFile
import com.kolibree.android.test.utils.createGruwareDataFromOtaUpdateType
import com.kolibree.android.toothbrushupdate.OtaUpdateType
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import org.hamcrest.Matchers.not
import org.junit.Test

internal class OtaUpdateActivityEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun checkOTAScreens() {
        val fwEventSubject = PublishSubject.create<OtaUpdateEvent>()
        val gruEventSubject =
            PublishSubject.create<OtaUpdateEvent>()
        val fwPath = context().cacheDir.toString() + "/fw"
        val gruPath = context().cacheDir.toString() + "/gru"
        val fwUpdate =
            create("1.0.0", fwPath, UpdateType.TYPE_FIRMWARE, 0L)
        val gruUpdate =
            create("2.0.0", gruPath, UpdateType.TYPE_GRU, 0L)
        val gruwaredata = createGruwareData(fwUpdate, gruUpdate)

        createDummyFile(context(), "fw")
        createDummyFile(context(), "gru")

        val connectionBuilder = createWithDefaultState()
            .withOTAUpdateSupport(fwEventSubject, fwUpdate)
            .withOTAUpdateSupport(gruEventSubject, gruUpdate)
            .withOTAAvailable(gruwaredata)

        prepareMocks(
            connectionBuilder = connectionBuilder,
            gruwareData = gruwaredata
        )

        launchOtaActivity(isMandatory = true, isRechargeableBrush = true)
        checkStartScreen(isMandatory = true, isRechargeableBrush = true)

        checkProgressDialog()

        checkInProgressScreen(fwEventSubject, gruEventSubject)

        pressBack() // back is ignored
    }

    @Test
    fun checkOtaErrorScreen() {
        val gruwareData = createGruwareDataFromOtaUpdateType(context(), OtaUpdateType.STANDARD)

        prepareMocks(
            connectionBuilder = createAndroidLess(),
            gruwareData = gruwareData
        )

        launchOtaActivity(isMandatory = true, isRechargeableBrush = true)
        checkStartScreen(isMandatory = true, isRechargeableBrush = true)

        checkProgressDialog()

        checkErrorScreen()
    }

    @Test
    fun checkStartScreenOptionalNonRechargeable() {
        launchOtaActivity(isMandatory = false, isRechargeableBrush = false)
        checkStartScreen(isMandatory = false, isRechargeableBrush = false)
    }

    @Test
    fun checkStartScreenCancelFinishActivity() {
        launchOtaActivity(isMandatory = false, isRechargeableBrush = false)

        onView(withId(R.id.cancel_button)).perform(click())
        onView(withId(R.id.cancel_button)).check(doesNotExist())
    }

    private fun checkStartScreen(isMandatory: Boolean, isRechargeableBrush: Boolean) {
        onView(withText(R.string.start_ota_title)).check(matches(isDisplayed()))
        onView(withText(R.string.start_ota_content)).check(matches(isDisplayed()))
        onView(withText(R.string.start_ota_subcontent1)).check(matches(isDisplayed()))
        onView(withText(R.string.start_ota_subcontent2)).check(matches(isDisplayed()))
        if (isRechargeableBrush) {
            onView(withText(R.string.start_ota_subcontent3)).check(matches(isDisplayed()))
        } else {
            onView(withText(R.string.start_ota_subcontent3)).check(matches(not(isDisplayed())))
        }
        onView(withText(R.string.start_ota_upgrade)).check(matches(isDisplayed()))
        if (isMandatory) {
            onView(withId(R.id.cancel_button)).check(matches(withText(R.string.start_ota_cancel_mandatory)))
        } else {
            onView(withId(R.id.cancel_button)).check(matches(withText(R.string.cancel)))
        }
    }

    private fun checkProgressDialog() {
        onView(withId(R.id.logo_animation)).check(matches(not(isDisplayed())))

        onView(withText(R.string.start_ota_upgrade)).perform(click())

        onView(withId(R.id.logo_animation)).check(matches(isDisplayed()))

        EspressoSingleThreadSchedulerModule.scheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        onView(withId(R.id.logo_animation)).check(matches(not(isDisplayed())))
    }

    private fun checkErrorScreen() {
        onView(withId(R.id.update_animation)).check(matches(not(isDisplayed())))
        onView(withId(R.id.in_progress_result_icon))
            .check(matches(isDisplayed()))
            .check(matches(withDrawable(R.drawable.ic_ota_fail)))

        onView(withText(R.string.ota_failure_title)).check(matches(isDisplayed()))
        onView(withText(R.string.ota_failure_content)).check(matches(isDisplayed()))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.progress_text)).check(matches(not(isDisplayed())))
        onView(withId(R.id.done_button)).check(matches(isDisplayed()))
    }

    private fun checkInProgressScreen(
        fwEventSubject: PublishSubject<OtaUpdateEvent>,
        gruEventSubject: PublishSubject<OtaUpdateEvent>
    ) {
        onView(withId(R.id.update_animation)).check(matches(isDisplayed()))
        onView(withId(R.id.in_progress_result_icon)).check(matches(not(isDisplayed())))
        onView(withId(R.id.done_button)).check(matches(not(isDisplayed())))

        checkProgress(0)

        val fwPercent = 56
        fwEventSubject.onNext(
            fromProgressiveAction(
                OTA_UPDATE_INSTALLING,
                fwPercent
            )
        )

        checkProgress(28)

        fwEventSubject.onNext(
            fromProgressiveAction(
                OTA_UPDATE_INSTALLING,
                100
            )
        )
        fwEventSubject.onComplete()

        checkProgress(50)

        gruEventSubject.onNext(
            fromProgressiveAction(
                OTA_UPDATE_INSTALLING,
                100
            )
        )
        gruEventSubject.onComplete()
        checkProgress(100)

        IdlingResourceFactory.viewVisibility(
            R.id.done_button,
            View.VISIBLE
        ).waitForIdle()

        onView(withId(R.id.done_button)).check(matches(isDisplayed()))
    }

    private fun checkProgress(progress: Int) {
        IdlingResourceFactory.textViewContent(
            R.id.progress_text,
            context().getString(R.string.in_progress_ota_progress, progress)
        ).waitForIdle()

        onView(withId(R.id.progress_text))
            .check(matches(isDisplayed()))
            .check(
                matches(
                    withText(
                        context().getString(
                            R.string.in_progress_ota_progress,
                            progress
                        )
                    )
                )
            )

        onView(withId(R.id.progress_bar))
            .check(matches(isDisplayed()))
            .check(matches(withProgress(progress)))
    }

    private fun launchOtaActivity(isMandatory: Boolean, isRechargeableBrush: Boolean) {
        launchActivity()
        context().startActivity(startOtaUpdateIntent(
            context(),
            isMandatory,
            KLTBConnectionBuilder.DEFAULT_MAC,
            if (isRechargeableBrush) KLTBConnectionBuilder.DEFAULT_MODEL else ToothbrushModel.CONNECT_B1
        ).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        })
    }
}
