/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.domain

import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
sealed class QuestionOfTheDayStatus {

    @VisibleForApp
    data class Available(val questionOfTheDay: QuestionOfTheDay) : QuestionOfTheDayStatus()

    @VisibleForApp
    object AlreadyAnswered : QuestionOfTheDayStatus()

    @VisibleForApp
    object NotAvailable : QuestionOfTheDayStatus()

    @VisibleForApp
    object Expired : QuestionOfTheDayStatus()
}
