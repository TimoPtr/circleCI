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
import dagger.Module

@VisibleForApp
@Module(includes = [QuestionOfTheDayDataModule::class, QuestionOfTheDayCoreModule::class])
object QuestionOfTheDayModule
