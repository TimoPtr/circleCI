/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.ui

internal interface QuestionOfTheDayInteraction {

    fun onAnswerClick(answer: QuestionOfTheDayViewState.Answer)

    fun onButtonClick()

    fun close()
}
