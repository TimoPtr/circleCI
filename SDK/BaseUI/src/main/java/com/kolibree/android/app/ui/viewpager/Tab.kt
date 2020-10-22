/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.viewpager

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.kolibree.android.baseui.R
import com.kolibree.android.failearly.FailEarly

/**
 * Represents single item in [ViewPager2] with [TabLayout] navigation.
 *
 * @param id unique ID of the tab, a list of tabs should not contain 2 tabs with the same id
 * @param isDefault this tab will be selected as default, only 1 tab in the list should have this
 * @param titleRes name to be displayed in [TabLayout], represented as string ID
 * @param titleRes name to be displayed in [TabLayout], represented as string
 * @param fragmentCreator returns new instance of the fragment associated with this tab
 */
@Keep
data class Tab(
    val id: Long,
    val isDefault: Boolean = false,
    @StringRes private val titleRes: Int? = null,
    private val title: String? = null,
    val fragmentCreator: () -> Fragment
) {
    init {
        FailEarly.failInConditionMet(
            titleRes == null && title == null,
            "Either titleRes or title needs to be set"
        )
        FailEarly.failInConditionMet(
            titleRes != null && title != null,
            "Either titleRes or title needs to be set"
        )
    }

    fun title(context: Context) = when {
        title != null -> title
        titleRes != null -> context.getString(titleRes)
        else -> {
            FailEarly.fail("We shouldn't end up here...")
            context.getString(R.string.empty)
        }
    }
}
