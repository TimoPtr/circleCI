/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.hum.questionoftheday.R
import com.kolibree.android.hum.questionoftheday.databinding.ItemQuestionAnswerBinding
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayViewState.Answer
import kotlin.math.max

@SuppressWarnings("LongMethod", "ComplexMethod")
@BindingAdapter("answers", "interaction", "clickable")
internal fun FlexboxLayout.setAnswers(
    answers: List<Answer>,
    interaction: QuestionOfTheDayInteraction,
    clickable: Boolean
) {
    val views = children.toList()
    val size = max(answers.size, views.size)
    val inflater = LayoutInflater.from(context)

    for (index in 0..size) {
        var view = views.getOrNull(index)
        val answer = answers.getOrNull(index)

        // We have too many views - remove what's left
        if (view != null && answer == null) {
            removeView(view)
            continue
        }

        // We have too many answers - add new view
        if (answer != null && view == null) {
            view = ItemQuestionAnswerBinding
                .inflate(inflater, this, true)
                .apply { root.binding = this }
                .root
        }

        // Bind new answer
        if (answer != null && view != null) {
            view.binding.also { binding ->
                binding.answer = answer
                binding.interaction = interaction
            }

            view.isClickable = clickable
        }
    }
}

/**
 * Stores [ItemQuestionAnswerBinding] inside [View] tag
 */
private var View.binding: ItemQuestionAnswerBinding
    get() = getTag(R.id.answer_binding_tag) as ItemQuestionAnswerBinding
    set(value) = setTag(R.id.answer_binding_tag, value)

@BindingAdapter("max_columns")
internal fun FlexboxLayout.setMaxColumns(maxColumns: Int) {
    check(maxColumns > 0) { "maxColumns must be greater than 0" }

    setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewRemoved(parent: View, child: View) = Unit

        override fun onChildViewAdded(parent: View, child: View) {
            val index = indexOfChild(child)
            val layoutParams = child.layoutParams as FlexboxLayout.LayoutParams
            layoutParams.isWrapBefore = index > 0 && index % maxColumns == 0
        }
    })
}

@SuppressWarnings("LongMethod", "ComplexMethod")
@BindingAdapter("answer")
internal fun MaterialButton.setAnswer(answer: Answer) {
    val strokeColor = when {
        answer.selected && !answer.confirmed -> R.color.transparent
        else -> R.color.white
    }

    val backgroundColor = when {
        answer.confirmed -> context.getColorFromAttr(R.attr.colorTertiaryLight)
        answer.selected -> context.getColor(R.color.white60)
        else -> context.getColor(R.color.transparent)
    }

    val textAppearance = when {
        answer.selected && !answer.confirmed -> R.style.TextAppearance_QuestionOfTheDay_Answer_Selected
        else -> R.style.TextAppearance_QuestionOfTheDay_Answer
    }

    backgroundTintList = ColorStateList.valueOf(backgroundColor)
    setStrokeColorResource(strokeColor)
    setTextAppearance(textAppearance)
    text = answer.text
}
