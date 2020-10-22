/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.di

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.questionoftheday.domain.MarkAsAnsweredUseCase
import com.kolibree.android.questionoftheday.domain.MarkAsAnsweredUseCaseImpl
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayUseCase
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayUseCaseImpl
import com.kolibree.android.questionoftheday.domain.SendAnswerUseCase
import com.kolibree.android.questionoftheday.domain.SendAnswerUseCaseImpl
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@VisibleForApp
@Module(includes = [UseCaseModule::class, InjectorModule::class])
abstract class QuestionOfTheDayCoreModule

@Module
internal abstract class InjectorModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [QuestionOfTheDayActivityModule::class])
    abstract fun bindQuestionOfTheDayActivity(): QuestionOfTheDayActivity
}

@Module
private abstract class UseCaseModule {

    @Binds
    abstract fun bindsUseCase(impl: QuestionOfTheDayUseCaseImpl): QuestionOfTheDayUseCase

    @Binds
    abstract fun bindsMarkUseCase(impl: MarkAsAnsweredUseCaseImpl): MarkAsAnsweredUseCase

    @Binds
    abstract fun bindsSendUseCase(impl: SendAnswerUseCaseImpl): SendAnswerUseCase
}
