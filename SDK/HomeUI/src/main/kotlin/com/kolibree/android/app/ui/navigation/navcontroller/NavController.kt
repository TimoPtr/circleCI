/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.navigation.navcontroller

import android.annotation.SuppressLint
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.R
import java.util.Deque
import timber.log.Timber

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun AppCompatActivity.setupNavigationController(bottomNavigation: BottomNavigationView) {
    val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
    with(bottomNavigation) {
        setupWithNavController(navController)
        setOnNavigationItemReselectedListener {
            // no-op, reselection won't cause fragment recreation
        }
        setOnNavigationItemSelectedListener { item ->
            navController.navigateTo(item)
        }
    }
}

private fun NavController.navigateTo(item: MenuItem): Boolean {
    val fragmentPopped = popToAlreadyExistingFragment(item)

    return if (fragmentPopped) true else {
        val builder = NavOptions.Builder().setLaunchSingleTop(true)
        val options = builder.build()
        return try {
            navigateSafe(item.itemId, null, options)
            true
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            false
        }
    }
}

private fun NavController.popToAlreadyExistingFragment(item: MenuItem): Boolean {
    return try {
        val backStackDestinations = (backStackField.get(this) as Deque<*>).map { backStackEntry ->
            val destinationField = backStackEntry::class.java.getDeclaredField("mDestination")
            destinationField.isAccessible = true
            destinationField.get(backStackEntry) as NavDestination
        }
        if (backStackDestinations.map { it.id }.contains(item.itemId)) {
            popBackStack(backStackDestinations.first { it.id == item.itemId }.id, false)
            true
        } else false
    } catch (e: RuntimeException) {
        FailEarly.fail(exception = e, message = "Navigation reflection mechanism failed")
        false
    }
}

private val backStackField =
    NavController::class.java.getDeclaredField("mBackStack").also { it.isAccessible = true }
