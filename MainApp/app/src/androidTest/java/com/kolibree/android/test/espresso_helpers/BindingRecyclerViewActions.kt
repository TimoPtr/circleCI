/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.espresso_helpers

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import timber.log.Timber

/**
 * View actions for [RecyclerView] with databinding inside
 *
 * Source: https://gist.github.com/madsbf/895228e6e5655c22c4913d49fdd5c545
 */
object BindingRecyclerViewActions {

    @JvmStatic
    fun bindedScrollTo(itemViewMatcher: Matcher<View>): Array<ViewAction> {
        return arrayOf(
            prepare(),
            RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(itemViewMatcher),
            sweep()
        )
    }

    private fun prepare(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())
            }

            override fun getDescription(): String? {
                return null
            }

            override fun perform(uiController: UiController, view: View) {
                if ((view as RecyclerView).adapter is BindingRecyclerViewAdapter<*>) {
                    try {
                        val f = RecyclerView::class.java.getDeclaredField("mLayoutOrScrollCounter")
                        f.isAccessible = true
                        f.setInt(view, 1)
                    } catch (e: NoSuchFieldException) {
                        Timber.e(e, "field not found in recyclerView in prepare")
                    } catch (e: IllegalAccessException) {
                        Timber.e(e, "reflexion failed in prepare")
                    }
                }
            }
        }
    }

    private fun sweep(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())
            }

            override fun getDescription(): String? {
                return null
            }

            override fun perform(uiController: UiController, view: View) {
                if ((view as RecyclerView).adapter is BindingRecyclerViewAdapter<*>) {
                    try {
                        val f = RecyclerView::class.java.getDeclaredField("mLayoutOrScrollCounter")
                        f.isAccessible = true
                        f.setInt(view, 0)
                        f.isAccessible = false
                    } catch (e: NoSuchFieldException) {
                        Timber.e(e, "field not found in recyclerView in perform")
                    } catch (e: IllegalAccessException) {
                        Timber.e(e, "reflexion failed in perform")
                    }
                }
            }
        }
    }
}
