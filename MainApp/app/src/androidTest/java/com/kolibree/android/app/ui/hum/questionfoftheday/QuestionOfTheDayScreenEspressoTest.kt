/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.questionfoftheday

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToQuestionOfTheDayCard
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Available
import com.kolibree.android.test.dagger.FakeQuestionOfTheDayRepository
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Test

internal class QuestionOfTheDayScreenEspressoTest : HomeScreenActivityEspressoTest() {

    private val mockAnswers = listOf(
        "Answer 1",
        "Answer 2"
    )

    private val mockQuestion = QuestionOfTheDay(
        id = 1,
        question = "Mock question",
        points = 120,
        answers = mockAnswers.mapIndexed { index, text ->
            QuestionOfTheDay.Answer(index.toLong(), text, correct = index == 0)
        }
    )

    private val questionRepository: FakeQuestionOfTheDayRepository
        get() = component().questionOfTheDayRepository()

    override fun setUp() {
        super.setUp()

        prepareMocks()
        questionRepository.mock(Available(mockQuestion))
        launchActivity()
        scrollToQuestionOfTheDayCard()
        onView(withId(R.id.question_card)).perform(click())
    }

    @Test
    fun displayCorrectData() {
        checkQuestionScreen(
            expectedQuestion = mockQuestion.question,
            expectedAnswers = mockAnswers
        )
    }

    @Test
    fun closeQuestionScreenWithCloseButton() {
        onView(withId(R.id.question_of_the_day_close)).perform(click())
        onView(withId(R.id.question_of_the_day_view)).check(doesNotExist())
    }

    @Test
    fun showSuccessAfterCorrectAnswerSelected() {
        val correctAnswer = mockQuestion.answers.first { it.correct }

        clickOnAnswer(correctAnswer.text)

        checkQuestionScreen(
            expectedQuestion = context().getString(R.string.question_of_the_day_correct_answer_title),
            expectedBody = context().resources.getQuantityString(
                R.plurals.question_of_the_day_correct_answer_body,
                mockQuestion.points,
                mockQuestion.points
            ),
            expectedAnswers = mockAnswers,
            expectedButton = context().getString(R.string.question_of_the_day_correct_answer_button)
        )
    }

    @Test
    fun showFailAfterWrongAnswerSelected() {
        val wrongAnswer = mockQuestion.answers.first { !it.correct }

        clickOnAnswer(wrongAnswer.text)

        checkQuestionScreen(
            expectedQuestion = context().getString(R.string.question_of_the_day_wrong_answer_title),
            expectedBody = context().getString(R.string.question_of_the_day_wrong_answer_body),
            expectedAnswers = mockAnswers,
            expectedButton = context().getString(R.string.question_of_the_day_wrong_answer_button)
        )
    }

    private fun clickOnAnswer(text: String) {
        onView(
            allOf(
                withId(R.id.question_of_the_day_answer), withText(text)
            )
        ).perform(click())
    }

    private fun checkQuestionScreen(
        expectedQuestion: String = "",
        expectedBody: String? = null,
        expectedAnswers: List<String> = emptyList(),
        expectedButton: String? = null
    ) {
        onView(withId(R.id.question_of_the_day_view)).check(matches(isDisplayed()))
        onView(withId(R.id.question_of_the_day_close)).check(matches(isDisplayed()))
        onView(withId(R.id.question_of_the_day_icon)).check(matches(isDisplayed()))
        onView(withId(R.id.question_of_the_day_answers)).check(matches(isDisplayed()))

        onView(withId(R.id.question_of_the_day_title)).check(matches(isDisplayed()))
        onView(withId(R.id.question_of_the_day_title)).check(matches(withText(expectedQuestion)))

        if (expectedBody == null) {
            onView(withId(R.id.question_of_the_day_body)).check(matches(not(isDisplayed())))
        } else {
            onView(withId(R.id.question_of_the_day_body)).check(matches(isDisplayed()))
            onView(withId(R.id.question_of_the_day_body)).check(matches(withText(expectedBody)))
        }

        if (expectedBody == null) {
            onView(withId(R.id.question_of_the_day_button)).check(matches(not(isDisplayed())))
        } else {
            onView(withId(R.id.question_of_the_day_button)).check(matches(isDisplayed()))
            onView(withId(R.id.question_of_the_day_button)).check(matches(withText(expectedButton)))
        }

        for (expectedAnswer in expectedAnswers) {
            onView(
                allOf(withId(R.id.question_of_the_day_answer), withText(expectedAnswer))
            ).check(matches(isDisplayed()))
        }
    }
}
