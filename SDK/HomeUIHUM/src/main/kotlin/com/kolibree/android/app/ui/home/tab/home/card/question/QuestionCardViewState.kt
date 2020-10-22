/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.question

import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class QuestionCardViewState(
    override val position: DynamicCardPosition,
    override val visible: Boolean,
    val questionOfTheDay: QuestionOfTheDay?
) : DynamicCardViewState {

    @IgnoredOnParcel
    val pointsVisible = questionOfTheDay != null

    override fun asBindingModel(): DynamicCardBindingModel {
        return QuestionCardBindingModel(this)
    }

    companion object {
        fun initial(position: DynamicCardPosition) = QuestionCardViewState(
            position = position,
            visible = false,
            questionOfTheDay = null
        )
    }
}
