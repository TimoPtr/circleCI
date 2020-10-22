/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.children
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.kolibree.android.failearly.FailEarly

internal class CollapsingToolbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CollapsingToolbarLayout(context, attrs, defStyleAttr) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (isInEditMode)
            return

        (parent as? AppBarLayout)?.also { appBarLayout ->
            children.filterIsInstance<AppBarLayout.OnOffsetChangedListener>()
                .forEach {
                    appBarLayout.addOnOffsetChangedListener(it)
                }
        } ?: FailEarly.fail(
            "HumCollapsingToolbarLayout only works with AppBarLayout as a direct parent"
        )
    }

    override fun onDetachedFromWindow() {
        (parent as? AppBarLayout)?.also { appBarLayout ->
            children.filterIsInstance<AppBarLayout.OnOffsetChangedListener>()
                .forEach { appBarLayout.removeOnOffsetChangedListener(it) }
        }
        super.onDetachedFromWindow()
    }
}
