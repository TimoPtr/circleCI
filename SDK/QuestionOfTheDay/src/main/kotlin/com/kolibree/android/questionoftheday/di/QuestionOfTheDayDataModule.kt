/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.di

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.hum.questionoftheday.data.room.QuestionRoomAppDatabase
import com.kolibree.android.hum.questionoftheday.data.room.QuestionRoomAppDatabase.Companion.DATABASE_NAME
import com.kolibree.android.questionoftheday.data.api.QuestionApi
import com.kolibree.android.questionoftheday.data.repo.QuestionOfTheDayRepository
import com.kolibree.android.questionoftheday.data.repo.QuestionRepository
import com.kolibree.android.questionoftheday.data.room.dao.QuestionDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module(includes = [DaoModule::class, ApiModule::class, RepositoryModule::class])
internal object QuestionOfTheDayDataModule {

    @Provides
    @AppScope
    fun provideQuestionDatabase(context: Context): QuestionRoomAppDatabase {
        return Room.databaseBuilder(context, QuestionRoomAppDatabase::class.java, DATABASE_NAME)
            .build()
    }
}

@Module
private abstract class RepositoryModule {
    @Binds
    internal abstract fun bindsQuestionOfTheDayRepository(impl: QuestionRepository):
        QuestionOfTheDayRepository
}

@Module
private object DaoModule {
    @Provides
    fun providesQuestionDao(appDatabase: QuestionRoomAppDatabase): QuestionDao =
        appDatabase.questionDao()
}

@Module
private object ApiModule {
    @Provides
    fun provideQuestionApi(retrofit: Retrofit): QuestionApi =
        retrofit.create(QuestionApi::class.java)
}
