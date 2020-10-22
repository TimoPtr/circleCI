/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import androidx.annotation.StringRes
import com.kolibree.android.app.base.BaseAction
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate

internal sealed class SettingsActions : BaseAction {
    data class ScrollToPosition(val position: Int) : SettingsActions()
    object ShowLogoutConfirmationDialog : SettingsActions()
    object ShowDeleteAccountConfirmationDialog : SettingsActions()
    object ShowDeleteAccountError : SettingsActions()
    class EditText(
        val currentValue: String? = null,
        @StringRes val title: Int? = null,
        @StringRes val hintText: Int? = null,
        val action: (String) -> Unit = {}
    ) : SettingsActions()

    /**
     * Action for displaying a single select dialog
     */
    class SingleSelect<T>(
        @StringRes val titleRes: Int? = null,
        val options: List<SingleSelectOption<T>> = listOf(),
        val currentOption: T? = null,
        val saveAction: (T) -> Unit = {}
    ) : SettingsActions()

    data class SingleSelectOption<T>(val value: T, @StringRes val nameRes: Int)

    data class ShowGetMyDataDialog(val sentTo: String?) : SettingsActions()

    class EditBrushingDuration(
        val currentValue: Duration = Duration.ZERO,
        val action: (Duration) -> Unit = {}
    ) : SettingsActions()

    class EditBirthDate(
        val currentValue: LocalDate? = null,
        val action: (LocalDate?) -> Unit = {}
    ) : SettingsActions()
}
