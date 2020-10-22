/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.di

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayActivity
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayNavigator
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class QuestionOfTheDayActivityModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: QuestionOfTheDayActivity): AppCompatActivity

    internal companion object {

        @Provides
        fun providesNavigator(activity: QuestionOfTheDayActivity): QuestionOfTheDayNavigator {
            return activity.createNavigatorAndBindToLifecycle(QuestionOfTheDayNavigator::class)
        }

        @Provides
        fun provideQuestionOfTheDay(activity: QuestionOfTheDayActivity): QuestionOfTheDay {
            return activity.extractQuestion()
        }
    }
}
