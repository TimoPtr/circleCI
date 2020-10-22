/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test

import android.view.View
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.actionWithAssertions
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher

@Keep
fun scrollToBottom(): ViewAction =
    actionWithAssertions(object : ViewAction {

        override fun getDescription(): String =
            "Scroll RecyclerView to bottom"

        override fun getConstraints(): Matcher<View> =
            allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())

        override fun perform(uiController: UiController, view: View) {
            (view as? RecyclerView)?.also { recyclerView ->
                val items = recyclerView.adapter?.itemCount ?: 0
                if (items > 0) {
                    recyclerView.scrollToPosition(items - 1)
                    uiController.loopMainThreadUntilIdle()
                }
            }
        }
    })
