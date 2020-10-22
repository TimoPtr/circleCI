package com.kolibree.android.rewards.feedback.tier

import androidx.annotation.Keep
import com.kolibree.android.app.base.BaseAction

@Keep
sealed class NewTierAction : BaseAction {
    object Close : NewTierAction()
}
