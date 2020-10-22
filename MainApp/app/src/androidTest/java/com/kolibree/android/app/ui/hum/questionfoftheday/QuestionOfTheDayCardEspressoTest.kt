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
import androidx.test.espresso.PerformException
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
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Available
import com.kolibree.android.test.dagger.FakeQuestionOfTheDayRepository
import org.hamcrest.Matchers.not
import org.junit.Test

internal class QuestionOfTheDayCardEspressoTest : HomeScreenActivityEspressoTest() {

    private val questionRepository: FakeQuestionOfTheDayRepository
        get() = component().questionOfTheDayRepository()

    override fun setUp() {
        super.setUp()
        prepareMocks()
    }

    @Test
    fun questionOfTheDayCardIsDisplayed() {
        val mockQuestion = QuestionOfTheDay(
            id = 1,
            question = "Mock question 1",
            points = 120,
            answers = listOf(QuestionOfTheDay.Answer(1, "answer", correct = true))
        )

        questionRepository.mock(Available(mockQuestion))

        launchActivity()
        scrollToQuestionOfTheDayCard()

        checkCard(
            expectedQuestion = mockQuestion.question,
            expectedPoints = context().resources.getQuantityString(
                R.plurals.question_of_the_day_card_points,
                mockQuestion.points,
                mockQuestion.points
            ),
            pointsVisible = true
        )
        makeScreenshot(
            activity.findViewById(R.id.question_card),
            "HomeTab_QuestionOfTheDayCard"
        )
    }

    @Test
    fun questionOfTheDayPointsAreHiddenWhenQuestionAnswered() {
        questionRepository.mock(QuestionOfTheDayStatus.AlreadyAnswered)

        launchActivity()
        scrollToQuestionOfTheDayCard()

        checkCard(
            expectedQuestion = context().getString(R.string.question_of_the_day_card_question_not_available),
            pointsVisible = false
        )
    }

    // Card should not be visible - Espresso should throw PerformException
    @Test(expected = PerformException::class)
    fun questionOfTheDayCardIsHiddenWhenQuestionNotAvailable() {
        questionRepository.mock(QuestionOfTheDayStatus.NotAvailable)

        launchActivity()
        scrollToQuestionOfTheDayCard()
    }

    @Test
    fun doNotOpenQuestionScreenIfAlreadyAnswered() {
        questionRepository.mock(QuestionOfTheDayStatus.AlreadyAnswered)

        launchActivity()
        scrollToQuestionOfTheDayCard()

        onView(withId(R.id.question_card)).perform(click())
        onView(withId(R.id.question_of_the_day_view)).check(doesNotExist())
    }

    private fun checkCard(
        expectedQuestion: String = "",
        expectedPoints: String? = null,
        pointsVisible: Boolean = false
    ) {
        onView(withId(R.id.question_card)).check(matches(isDisplayed()))
        onView(withId(R.id.question_card_icon)).check(matches(isDisplayed()))
        onView(withId(R.id.question_card_title)).check(matches(isDisplayed()))
        onView(withId(R.id.question_card_question)).check(matches(isDisplayed()))
        onView(withId(R.id.question_card_question)).check(matches(withText(expectedQuestion)))

        if (pointsVisible) {
            onView(withId(R.id.question_card_points)).check(matches(isDisplayed()))
            onView(withId(R.id.question_card_points)).check(matches(withText(expectedPoints)))
        } else {
            onView(withId(R.id.question_card_points)).check(matches(not(isDisplayed())))
        }
    }
}
