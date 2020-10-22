/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.confirmation

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.ui.fragment.sanitizedArguments
import com.kolibree.android.app.widget.snackbar.snackbar
import com.kolibree.android.brushingquiz.R
import com.kolibree.android.brushingquiz.databinding.FragmentQuizConfirmationBinding
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelper
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationAction.FinishCancelAction
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationAction.FinishSuccessAction
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationAction.ShowErrorToothbrushNotPaired
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationAction.ShowErrorUnknown
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import javax.inject.Inject

internal class QuizConfirmationFragment :
    BaseMVIFragment<
        QuizConfirmationViewState,
        QuizConfirmationAction,
        QuizConfirmationViewModel.Factory,
        QuizConfirmationViewModel,
        FragmentQuizConfirmationBinding>(),
    TrackableScreen {

    @Inject
    lateinit var analyticsHelper: BrushingQuizAnalyticsHelper

    companion object {
        private const val EXTRA_BRUSHING_MODE_INDEX = "extra_brushing_mode_index"
        @JvmStatic
        fun newInstance(): QuizConfirmationFragment = QuizConfirmationFragment()

        fun createBundle(brushingMode: BrushingMode): Bundle {
            return Bundle().apply {
                putSerializable(EXTRA_BRUSHING_MODE_INDEX, brushingMode)
            }
        }
    }

    override fun getViewModelClass(): Class<QuizConfirmationViewModel> =
        QuizConfirmationViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_quiz_confirmation

    override fun execute(action: QuizConfirmationAction) {
        when (action) {
            FinishSuccessAction -> finishWithResult(Activity.RESULT_OK)
            FinishCancelAction -> finishWithResult(Activity.RESULT_CANCELED)
            ShowErrorToothbrushNotPaired -> showToothbrushNotPairedError()
            ShowErrorUnknown -> showTryAgainError()
        }
    }

    private fun finishWithResult(result: Int) {
        requireActivity().apply {
            setResult(result)

            finish()
        }
    }

    private fun showTryAgainError() {
        showError(R.string.something_went_wrong)
    }

    private fun showToothbrushNotPairedError() {
        showError(R.string.brushing_quiz_confirmation_try_it_not_paired)
    }

    private fun showError(@StringRes messageResId: Int) {
        val rootView: View = requireActivity().window.decorView.findViewById(android.R.id.content)

        snackbar(rootView) {
            message(messageResId)
            duration(Snackbar.LENGTH_LONG)
        }.show()
    }

    override fun getScreenName(): AnalyticsEvent = analyticsHelper.getScreenNameForQuizResult()

    fun selectedBrushingMode(): BrushingMode =
        sanitizedArguments().getSerializable(EXTRA_BRUSHING_MODE_INDEX) as BrushingMode
}
