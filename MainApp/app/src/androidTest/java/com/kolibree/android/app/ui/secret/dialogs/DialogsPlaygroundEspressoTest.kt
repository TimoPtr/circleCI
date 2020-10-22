/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.secret.dialogs

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import com.kolibree.R
import com.kolibree.android.app.ui.settings.secret.dialogs.DialogsPlaygroundActivity
import com.kolibree.android.test.BaseActivityTestRule
import com.kolibree.android.test.BaseEspressoTest
import com.kolibree.android.test.KolibreeActivityTestRule
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Test

internal class DialogsPlaygroundEspressoTest : BaseEspressoTest<DialogsPlaygroundActivity>() {

    override fun createRuleForActivity(): BaseActivityTestRule<DialogsPlaygroundActivity> {
        return KolibreeActivityTestRule.Builder(DialogsPlaygroundActivity::class.java)
            .launchActivity(false)
            .build()
    }

    @Test
    fun alertDialogWithStringsTest() {
        launchActivity()

        clickAlertWithStringsButton()

        onView(withId(R.id.feature_image))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.title_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Test"))))

        onView(withId(R.id.body_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Hello World!"))))

        onView(withText("Contained Button"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(withText("Outlined Button"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun alertDialogWithStringIdsTest() {
        launchActivity()

        clickAlertWithStringIdsButton()

        onView(withId(R.id.feature_image))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.title_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Toothbrush ID"))))

        onView(withId(R.id.body_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Help Center"))))

        onView(withText("Contained Button"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(withText("Text Button"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun alertDialogWithFeatureImageTest() {
        launchActivity()

        clickAlertWithFeatureImageButton()

        onView(withId(R.id.feature_image))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(withId(R.id.title_text))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.body_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Tinted Feature Image"))))

        onView(withText("Contained Button"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(withText("Text Button"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun alertDialogWithFeatureImageIdTest() {
        launchActivity()

        clickAlertWithFeatureImageIdButton()

        onView(withId(R.id.feature_image))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(withId(R.id.title_text))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.body_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Feature Image"))))

        onView(withText("Contained Button"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(withText("Text Button"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    @Suppress("LongMethod")
    fun singleSelectWithButtons() {
        launchActivity()

        clickSingleSelectWithButtonsButton()

        onView(withId(R.id.feature_image))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.title_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Test"))))

        onView(withId(R.id.body_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Hello World!"))))

        onView(withText("Option 1"))
            .inRoot(isDialog())
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        isAssignableFrom(MaterialRadioButton::class.java),
                        isChecked()
                    )
                )
            )

        onView(withText("Option 2"))
            .inRoot(isDialog())
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        isAssignableFrom(MaterialRadioButton::class.java),
                        not(isChecked())
                    )
                )
            )

        onView(withText("Option 2"))
            .perform(click())

        onView(withText("Option 1"))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), not(isChecked()))))

        onView(withText("Option 2"))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), isChecked())))
    }

    @Test
    @Suppress("LongMethod")
    fun singleSelectWithoutButtons() {
        launchActivity()

        clickSingleSelectWithoutButtonsButton()

        onView(withId(R.id.feature_image))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.title_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Test"))))

        onView(withId(R.id.body_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Hello World!"))))

        onView(withText("Option 1"))
            .inRoot(isDialog())
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        isAssignableFrom(MaterialRadioButton::class.java),
                        isChecked()
                    )
                )
            )

        onView(withText("Option 2"))
            .inRoot(isDialog())
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        isAssignableFrom(MaterialRadioButton::class.java),
                        not(isChecked())
                    )
                )
            )

        onView(withText("Option 2"))
            .perform(click())

        onView(withText("Option 1"))
            .check(doesNotExist())
    }

    @Test
    @Suppress("LongMethod")
    fun multiSelectWithButtons() {
        launchActivity()

        clickMultiSelectWithButtonsButton()

        onView(withId(R.id.feature_image))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.title_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Test"))))

        onView(withId(R.id.body_text))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), withText("Hello World!"))))

        onView(withText("Option 1"))
            .inRoot(isDialog())
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        isAssignableFrom(MaterialCheckBox::class.java),
                        isChecked()
                    )
                )
            )

        onView(withText("Option 2"))
            .inRoot(isDialog())
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        isAssignableFrom(MaterialCheckBox::class.java),
                        not(isChecked())
                    )
                )
            )

        onView(withText("Option 2"))
            .perform(click())

        onView(withText("Option 1"))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), isChecked())))

        onView(withText("Option 2"))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), isChecked())))

        onView(withText("Option 1"))
            .perform(click())

        onView(withText("Option 1"))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), not(isChecked()))))

        onView(withText("Option 2"))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), isChecked())))

        onView(withText("Option 2"))
            .perform(click())

        onView(withText("Option 1"))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), not(isChecked()))))

        onView(withText("Option 2"))
            .inRoot(isDialog())
            .check(matches(allOf(isDisplayed(), not(isChecked()))))
    }

    private fun launchActivity() {
        activityTestRule.launchActivity(createIntent())
    }

    private fun createIntent(): Intent {
        return Intent()
    }

    private fun clickAlertWithStringsButton() {
        onView(withId(R.id.alert_with_strings))
            .perform(click())
    }

    private fun clickAlertWithStringIdsButton() {
        onView(withId(R.id.alert_with_string_ids))
            .perform(click())
    }

    private fun clickAlertWithFeatureImageButton() {
        onView(withId(R.id.alert_with_feature_image))
            .perform(click())
    }

    private fun clickAlertWithFeatureImageIdButton() {
        onView(withId(R.id.alert_with_feature_image_id))
            .perform(click())
    }

    private fun clickSingleSelectWithButtonsButton() {
        onView(withId(R.id.single_select_with_buttons))
            .perform(click())
    }

    private fun clickSingleSelectWithoutButtonsButton() {
        onView(withId(R.id.single_select_without_buttons))
            .perform(click())
    }

    private fun clickMultiSelectWithButtonsButton() {
        onView(withId(R.id.multi_select_with_buttons))
            .perform(click())
    }
}
