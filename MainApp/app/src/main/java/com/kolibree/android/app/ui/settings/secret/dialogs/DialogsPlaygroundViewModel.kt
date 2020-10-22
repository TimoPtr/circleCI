/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.dialogs

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import javax.inject.Inject

internal class DialogsPlaygroundViewModel(initialViewState: DialogsPlaygroundViewState?) :
    BaseViewModel<DialogsPlaygroundViewState, DialogsPlaygroundActions>(
        initialViewState ?: DialogsPlaygroundViewState.initial()
    ) {

    fun alertWithStrings() {
        pushAction(DialogsPlaygroundActions.AlertWithStrings)
    }

    fun alertWithStringIds() {
        pushAction(DialogsPlaygroundActions.AlertWithStringIds)
    }

    fun textInputDialog() {
        pushAction(DialogsPlaygroundActions.TextInputDialog)
    }

    fun alertWithTintedFeatureImage() {
        pushAction(DialogsPlaygroundActions.AlertWithTintedFeatureImage)
    }

    fun alertWithFeatureImageId() {
        pushAction(DialogsPlaygroundActions.AlertWithFeatureImageId)
    }

    fun alertWithStylizedHeadline() {
        pushAction(DialogsPlaygroundActions.AlertWithStylizedHeadline)
    }

    fun singleSelectWithButtons() {
        pushAction(DialogsPlaygroundActions.SingleSelectWithButtons)
    }

    fun singleSelectWithoutButtons() {
        pushAction(DialogsPlaygroundActions.SingleSelectWithoutButtons)
    }

    fun multiSelectWithStrings() {
        pushAction(DialogsPlaygroundActions.MultiSelectWithStrings)
    }

    fun duration() {
        pushAction(DialogsPlaygroundActions.DurationDialog)
    }

    fun alertWithIcon() {
        pushAction(DialogsPlaygroundActions.AlertWithIcon)
    }

    fun carousel() {
        pushAction(DialogsPlaygroundActions.Carousel)
    }

    class Factory @Inject constructor() : BaseViewModel.Factory<DialogsPlaygroundViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DialogsPlaygroundViewModel(viewState) as T
    }
}
