/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.quiz.question

import android.os.Bundle
import android.view.View
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.ui.fragment.sanitizedArguments
import com.kolibree.android.brushingquiz.R
import com.kolibree.android.brushingquiz.databinding.FragmentQuizQuestionBinding
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelper
import com.kolibree.android.brushingquiz.logic.models.QuizScreen
import com.kolibree.android.tracker.TrackableScreen
import javax.inject.Inject

internal class QuizScreenFragment :
    BaseMVIFragment<
        QuizScreenViewState,
        QuizScreenAction,
        QuizScreenViewModel.Factory,
        QuizScreenViewModel,
        FragmentQuizQuestionBinding>(),
    TrackableScreen {

    @Inject
    lateinit var analyticsHelper: BrushingQuizAnalyticsHelper

    companion object {
        private const val EXTRA_SCREENS = "extra_quiz_screens"
        private const val EXTRA_CURRENT_SCREEN_INDEX = "extra_current_screen"

        @JvmStatic
        fun newInstance(
            quizScreens: ArrayList<QuizScreen>,
            currentScreen: Int
        ) = QuizScreenFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(EXTRA_SCREENS, quizScreens)
                putInt(EXTRA_CURRENT_SCREEN_INDEX, currentScreen)
            }
        }
    }

    override fun getViewModelClass() = QuizScreenViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getLayoutId(): Int = R.layout.fragment_quiz_question

    override fun execute(action: QuizScreenAction) {
        // no-op
    }

    override fun getScreenName() =
        analyticsHelper.getScreenNameForQuizQuestionIndex(getCurrentScreenIndex())

    fun getQuizScreen(): QuizScreen {
        return sanitizedArguments().let {
            val currentScreenIndex = it.getInt(EXTRA_CURRENT_SCREEN_INDEX)

            getScreens()?.get(currentScreenIndex)
        } ?: throw IllegalStateException("QuizScreenFragment needs a QuizScreen")
    }

    fun getTotalScreens(): Int {
        return getScreens()?.size
            ?: throw IllegalStateException("QuizScreenFragment needs to know total screens")
    }

    private fun getScreens() =
        sanitizedArguments().getParcelableArrayList<QuizScreen>(EXTRA_SCREENS)

    fun getCurrentScreenIndex(): Int {
        return getScreens()?.indexOf(getQuizScreen())
            ?: throw IllegalStateException("QuizScreenFragment needs to know screen index")
    }
}
