/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.question

import android.content.Context
import android.view.View
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class QuestionCardBindingModel(
    val data: QuestionCardViewState,
    override val layoutId: Int = R.layout.home_card_question
) : DynamicCardBindingModel(data) {

    fun getQuestion(context: Context): String {
        return if (data.questionOfTheDay == null) {
            context.getString(R.string.question_of_the_day_card_question_not_available)
        } else {
            data.questionOfTheDay.question
        }
    }

    fun pointsVisibility(): Int {
        return if (data.pointsVisible) View.VISIBLE else View.GONE
    }

    fun points(context: Context): String {
        val points = data.questionOfTheDay?.points ?: 0
        return context.resources.getQuantityString(
            R.plurals.question_of_the_day_card_points,
            points,
            points.toString()
        )
    }
}
