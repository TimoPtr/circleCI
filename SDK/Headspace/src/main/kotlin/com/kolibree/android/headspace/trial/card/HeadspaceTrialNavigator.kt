/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.trial.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.navigation.NavigationHelper
import com.kolibree.android.app.ui.dialog.alertDialog
import com.kolibree.android.headspace.R
import javax.inject.Inject

@VisibleForApp
interface HeadspaceTrialNavigator : NavigationHelper {
    fun confirmDismissCard(onConfirmAction: () -> Unit, onCancelAction: () -> Unit)
}

internal class HeadspaceTrialNavigatorImpl(
    navigationHelper: NavigationHelper
) : BaseNavigator<BaseMVIFragment<*, *, *, *, *>>(),
    HeadspaceTrialNavigator,
    NavigationHelper by navigationHelper {

    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun confirmDismissCard(
        crossinline onConfirmAction: () -> Unit,
        crossinline onCancelAction: () -> Unit
    ) = withOwner {
        context?.let { context ->
            alertDialog(context) {
                title(R.string.headspace_close_confirm_title)
                body(R.string.headspace_close_confirm)
                containedButton {
                    title(R.string.ok)
                    action {
                        onConfirmAction()
                        dismiss()
                    }
                }
                textButtonTertiary {
                    title(R.string.cancel)
                    action {
                        onCancelAction()
                        dismiss()
                    }
                }
            }.show()
        }
    }
}

internal class HeadspaceTrialNavigatorFactory @Inject constructor(
    private val navigationHelper: NavigationHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HeadspaceTrialNavigatorImpl(navigationHelper = navigationHelper) as T
}
