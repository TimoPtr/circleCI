package com.kolibree.android.questionoftheday.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.widget.snackbar.showErrorSnackbar
import com.kolibree.android.hum.questionoftheday.R
import com.kolibree.android.hum.questionoftheday.databinding.ActivityQuestionOfTheDayBinding
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayActions.ShowAlreadyAnsweredError
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayActions.ShowUnknownError
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class QuestionOfTheDayActivity : BaseMVIActivity<
    QuestionOfTheDayViewState,
    QuestionOfTheDayActions,
    QuestionOfTheDayViewModel.Factory,
    QuestionOfTheDayViewModel,
    ActivityQuestionOfTheDayBinding>(),
    TrackableScreen {

    override fun getViewModelClass() = QuestionOfTheDayViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_question_of_the_day

    override fun execute(action: QuestionOfTheDayActions) {
        when (action) {
            is ShowUnknownError -> binding.root.showErrorSnackbar(action.error)
            is ShowAlreadyAnsweredError ->
                Toast.makeText(this, action.error.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun getScreenName(): AnalyticsEvent = QuestionOfTheDayAnalytics.main()

    fun extractQuestion(): QuestionOfTheDay {
        return intent.getParcelableExtra(EXTRA_QUESTION) ?: error("Missing argument")
    }

    override fun onBackPressed() {
        viewModel.close()
    }
}

internal const val EXTRA_ANSWER_TIME = "EXTRA_ANSWER_TIME"
private const val EXTRA_QUESTION = "EXTRA_QUESTION"

internal fun questionOfTheDayScreenIntent(
    context: Context,
    questionOfTheDay: QuestionOfTheDay
): Intent {
    return Intent(context, QuestionOfTheDayActivity::class.java).apply {
        putExtra(EXTRA_QUESTION, questionOfTheDay)
    }
}
