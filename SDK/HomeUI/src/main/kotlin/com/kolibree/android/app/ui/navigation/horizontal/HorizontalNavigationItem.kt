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
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import java.io.Serializable

/**
 * Represents a single entry in horizontal navigation, associates the trigger with concrete action.
 *
 * @param id unique id of the item
 * @param triggerId id of view which is associated with this nav item (button, menu, tab etc.)
 * @param containerId id of view in which fragment should be inflated to
 * @param fragmentClass class of the fragment for inflation
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
data class HorizontalNavigationItem<T : Fragment>(
    val id: Int,
    @IdRes val triggerId: Int,
    @IdRes val containerId: Int,
    val fragmentClass: Class<T>
) : Serializable {

    val fragmentClassName = fragmentClass.canonicalName!!

    val fragmentTag = fragmentClassName

    fun matchesId(id: Int): Boolean = this.id == id

    fun matchesTrigger(@IdRes triggerId: Int): Boolean = this.triggerId == triggerId
}

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun List<HorizontalNavigationItem<*>>.itemForId(id: Int): HorizontalNavigationItem<*>? =
    firstOrNull { it.matchesId(id) }

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun List<HorizontalNavigationItem<*>>.itemForTriggerId(triggerId: Int): HorizontalNavigationItem<*>? =
    firstOrNull { it.matchesTrigger(triggerId) }
