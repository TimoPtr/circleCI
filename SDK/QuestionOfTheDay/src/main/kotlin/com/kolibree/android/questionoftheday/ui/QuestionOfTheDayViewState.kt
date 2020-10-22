package com.kolibree.android.questionoftheday.ui

import android.content.Context
import android.os.Parcelable
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.hum.questionoftheday.R
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.OffsetDateTime

internal interface QuestionOfTheDayText {

    fun title(context: Context): String

    fun body(context: Context): String

    fun button(context: Context): String
}

@Parcelize
internal data class QuestionOfTheDayViewState(
    val iconRes: Int,
    val question: String,
    val points: Int,
    val answers: List<Answer>,
    val isLoading: Boolean,
    val isSuccess: Boolean,
    val answerTime: OffsetDateTime?
) : BaseViewState, QuestionOfTheDayText {

    constructor(questionOfTheDay: QuestionOfTheDay) : this(
        iconRes = R.drawable.question_icon,
        question = questionOfTheDay.question,
        points = questionOfTheDay.points,
        answers = questionOfTheDay.answers.map(::Answer),
        isLoading = false,
        isSuccess = false,
        answerTime = null
    )

    @IgnoredOnParcel
    val answered: Boolean = answers.any { it.selected }

    @IgnoredOnParcel
    val bodyVisible: Boolean = answered && !isLoading

    @IgnoredOnParcel
    val buttonVisible: Boolean = answered && !isLoading

    init {
        FailEarly.failInConditionMet(
            condition = answered && answerTime == null,
            message = "Answer time is missing!"
        )
    }

    @Parcelize
    internal data class Answer(
        val answer: QuestionOfTheDay.Answer,
        val selected: Boolean,
        val confirmed: Boolean
    ) : Parcelable {

        @IgnoredOnParcel
        val id: Long = answer.id

        @IgnoredOnParcel
        val text: String = answer.text

        @IgnoredOnParcel
        val isCorrect: Boolean = answer.correct

        constructor(answer: QuestionOfTheDay.Answer) : this(
            answer = answer,
            selected = false,
            confirmed = false
        )
    }

    override fun title(context: Context) = when {
        !answered || isLoading -> question
        isSuccess -> context.getString(R.string.question_of_the_day_correct_answer_title)
        else -> context.getString(R.string.question_of_the_day_wrong_answer_title)
    }

    override fun body(context: Context) = when {
        isSuccess -> context.resources.getQuantityString(
            R.plurals.question_of_the_day_correct_answer_body,
            points,
            points.toString()
        )
        else -> context.getString(R.string.question_of_the_day_wrong_answer_body)
    }

    override fun button(context: Context) = when {
        isSuccess -> context.getString(R.string.question_of_the_day_correct_answer_button)
        else -> context.getString(R.string.question_of_the_day_wrong_answer_button)
    }

    fun selectedAnswer() = answers.find { it.selected }

    fun selectAnswer(answer: Answer, answerTime: OffsetDateTime) = copy(
        answers = answers.map { it.copy(selected = answer.id == it.id) },
        isLoading = true,
        answerTime = answerTime
    )

    fun complete(success: Boolean) = copy(
        iconRes = if (success) R.drawable.question_icon_success else R.drawable.question_icon_fail,
        answers = answers.map { it.copy(confirmed = it.answer.correct) },
        isLoading = false,
        isSuccess = success
    )

    fun reset() = copy(
        iconRes = R.drawable.question_icon,
        answers = answers.map { it.copy(selected = false, confirmed = false) },
        isLoading = false,
        isSuccess = false,
        answerTime = null
    )
}
