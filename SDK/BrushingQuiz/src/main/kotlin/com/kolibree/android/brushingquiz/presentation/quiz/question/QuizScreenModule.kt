/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.quiz.question

import com.kolibree.android.brushingquiz.logic.models.QuizScreen
import com.kolibree.android.brushingquiz.presentation.quiz.QuizAnswerClickListener
import com.kolibree.android.brushingquiz.presentation.quiz.QuizFragment
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Module
internal object QuizScreenModule {

    @Provides
    fun providesQuizScreen(fragment: QuizScreenFragment): QuizScreen {
        return fragment.getQuizScreen()
    }

    @Provides
    @TotalScreens
    fun providesTotalScreens(fragment: QuizScreenFragment): Int {
        return fragment.getTotalScreens()
    }

    @Provides
    @CurrentScreenIndex
    fun providesCurrentScreenIndex(fragment: QuizScreenFragment): Int {
        return fragment.getCurrentScreenIndex()
    }
    @Provides
    fun providesAnswerClickListener(fragment: QuizScreenFragment): QuizAnswerClickListener {
        return (fragment.parentFragment as QuizFragment).quizAnswerClickListener()
    }
}

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
internal annotation class CurrentScreenIndex

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
internal annotation class TotalScreens
