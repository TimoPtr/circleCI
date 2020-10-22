/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation

import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationFragment
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationModule
import com.kolibree.android.brushingquiz.presentation.quiz.QuizFragment
import com.kolibree.android.brushingquiz.presentation.quiz.question.QuizScreenFragment
import com.kolibree.android.brushingquiz.presentation.quiz.question.QuizScreenModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [QuizFragmentsModule::class])
internal object BrushingQuizActivityModule

@Module
internal abstract class QuizFragmentsModule {
    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeBrushingQuizFragment(): QuizFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [QuizScreenModule::class])
    internal abstract fun contributeQuizScreenFragment(): QuizScreenFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [QuizConfirmationModule::class])
    internal abstract fun contributeQuizConfirmationFragment(): QuizConfirmationFragment
}
