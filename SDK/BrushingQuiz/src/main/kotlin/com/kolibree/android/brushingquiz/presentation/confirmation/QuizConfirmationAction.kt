/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.confirmation

import com.kolibree.android.app.base.BaseAction

internal sealed class QuizConfirmationAction : BaseAction {
    object FinishSuccessAction : QuizConfirmationAction()
    object FinishCancelAction : QuizConfirmationAction()

    object ShowErrorToothbrushNotPaired : QuizConfirmationAction()
    object ShowErrorUnknown : QuizConfirmationAction()
}
