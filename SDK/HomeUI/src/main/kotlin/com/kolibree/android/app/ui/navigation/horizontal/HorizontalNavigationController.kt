/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.navigation.horizontal

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.kolibree.android.failearly.FailEarly

/**
 * Our custom horizontal navigation controller, alternative to nav controller and view pager.
 * Can be used for bottom navigation, tab navigation etc.
 *
 * Features:
 * - lazy initialization - fragments are created when they are needed, ensuring smooth app start
 * - avoid fragment recreation - when fragment is created once, it doesn't need to be recreated
 * again, ensuring smooth UI
 * - back navigation to home page handled automatically
 *
 * TODO add automatic container creation (?)
 * TODO add bindings to wire it with view model
 *
 * @param context context for resource retrieval
 * @param containerLayout layout holding containers for each navigation item
 * @param fragmentManager fragment manager used to perform fragment transactions
 * @param navigationItems list of items this controller needs to handle. Needs to have at least
 * one element.
 * @param startingItem navigation graph's entry point, controller will open this item
 * after its initialization if the back stack is empty
 * @param backStack stack of navigation, used during back navigation and after the view is restored
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
class HorizontalNavigationController(
    private val context: Context,
    private val containerLayout: ViewGroup,
    private val fragmentManager: FragmentManager,
    private val navigationItems: List<HorizontalNavigationItem<*>>,
    private val startingItem: HorizontalNavigationItem<*>
) {
    var currentItem: HorizontalNavigationItem<*> = startingItem
        private set

    init {
        FailEarly.failInConditionMet(
            navigationItems.isEmpty(),
            "You need at least 1 item to navigate"
        )
        FailEarly.failInConditionMet(
            navigationItems.contains(startingItem).not(),
            "navigationItems have to contain startingPoint in it"
        )

        performNavigationTo(startingItem)
    }

    fun <T : Fragment> navigateTo(
        navigationItem: HorizontalNavigationItem<T>,
        andThen: (T) -> Unit = {}
    ) {
        if (navigationItem != currentItem) {
            performNavigationTo(navigationItem)
        }
        (fragmentManager.findFragmentByTag(currentItem.fragmentClassName) as? T)?.let {
            andThen(it)
        } ?: FailEarly.fail("Fragment of type ${navigationItem.fragmentClassName} was not found!")
    }

    fun navigateBack(): Boolean {
        if (currentItem != startingItem) {
            navigateTo(startingItem)
            return true
        }
        return false
    }

    private fun <T : Fragment> performNavigationTo(navigationItem: HorizontalNavigationItem<T>) {
        navigationItems.forEach {
            val container = containerForItem(it)
            val fragment = fragmentManager.findFragmentByTag(it.fragmentTag)

            val transaction = fragmentManager.beginTransaction()
                .setReorderingAllowed(true)

            if (it.containerId == navigationItem.containerId) {
                // This is us!
                fragment?.let {
                    transaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
                } ?: run {
                    // Fragment was not added to the container yet - so let's add it now
                    val newFragmentInstance = fragmentManager.fragmentFactory.instantiate(
                        context.classLoader,
                        it.fragmentClassName
                    )
                    transaction.add(container.id, newFragmentInstance, it.fragmentTag)
                }

                container.visibility = View.VISIBLE
            } else if (container.visibility != View.GONE) {
                // We are navigating to other fragment, so this container has to hide itself
                fragment?.let {
                    transaction.setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                }
                container.visibility = View.GONE
            }

            transaction.commitNowAllowingStateLoss()
        }

        currentItem = navigationItem
    }

    private fun containerForItem(it: HorizontalNavigationItem<*>) =
        checkNotNull(containerLayout.findViewById<ViewGroup>(it.containerId))
}
