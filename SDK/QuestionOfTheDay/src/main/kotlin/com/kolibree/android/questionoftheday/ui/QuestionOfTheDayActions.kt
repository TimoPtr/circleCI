package com.kolibree.android.questionoftheday.ui

import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.questionoftheday.domain.AlreadyAnsweredException

internal sealed class QuestionOfTheDayActions : BaseAction {

    class ShowUnknownError(val error: Error) : QuestionOfTheDayActions()
    class ShowAlreadyAnsweredError(val error: AlreadyAnsweredException) : QuestionOfTheDayActions()
}
