/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.quiz

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kolibree.android.app.ui.common.DataBindableViewPagerAdapter
import com.kolibree.android.brushingquiz.logic.models.QuizScreen
import com.kolibree.android.brushingquiz.presentation.quiz.question.QuizScreenFragment

@SuppressLint("WrongConstant")
internal class QuizPagerAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT),
    DataBindableViewPagerAdapter<QuizScreen> {
    override fun update(content: List<QuizScreen>) {
        /*
        we only want to update the ViewPager when the List<QuizScreen> is first provided, not
        when the user answers a question
         */
        if (content.size != quizScreens.size) {
            quizScreens.clear()
            quizScreens.addAll(content)
            notifyDataSetChanged()
        }
    }

    private val quizScreens: ArrayList<QuizScreen> = ArrayList()

    override fun getItem(position: Int) = QuizScreenFragment.newInstance(quizScreens, position)

    override fun getCount() = quizScreens.size
}
