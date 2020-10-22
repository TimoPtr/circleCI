/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.quiz

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.fragment.findNavController
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.brushingquiz.R
import com.kolibree.android.brushingquiz.databinding.FragmentBrushingQuizBinding
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationFragment
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.tracker.NonTrackableScreen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

internal class QuizFragment : BaseMVIFragment<
    QuizViewState,
    QuizActions,
    QuizViewModel.Factory,
    QuizViewModel,
    FragmentBrushingQuizBinding>(),
    NonTrackableScreen,
    HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.quizViewpager.adapter = QuizPagerAdapter(childFragmentManager)
    }

    override fun getLayoutId(): Int = R.layout.fragment_brushing_quiz

    fun onBackPressed(): Boolean = viewModel.onBackPressed()

    fun quizAnswerClickListener() = viewModel

    fun beforeOnClose() = viewModel.sendCurrentProgramEvent()

    override fun getViewModelClass(): Class<QuizViewModel> =
        QuizViewModel::class.java

    override fun execute(action: QuizActions) {
        when (action) {
            is NavigateToConfirmBrushingProgramAction -> navigateToConfirmation(
                actionId = action.navigationActionId,
                brushingMode = action.selectedBrushingMode
            )
            FinishBrushingProgramAction -> activity?.finish()
        }
    }

    private fun navigateToConfirmation(@IdRes actionId: Int, brushingMode: BrushingMode) {
        findNavController().navigateSafe(actionId, QuizConfirmationFragment.createBundle(brushingMode))
    }
}
