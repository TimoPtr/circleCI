/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slides

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kolibree.android.plaqless.howto.intro.slide1.SlideOneFragment
import com.kolibree.android.plaqless.howto.intro.slide2.SlideTwoFragment
import com.kolibree.android.plaqless.howto.intro.slide3.SlideThreeFragment

@SuppressLint("WrongConstant")
internal class SlidesPagerAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            SLIDE_ONE_INX -> SlideOneFragment()
            SLIDE_TWO_INX -> SlideTwoFragment()
            SLIDE_THREE_INX -> SlideThreeFragment()
            else -> SlideOneFragment()
        }
    }

    override fun getCount() = SLIDES
}

const val SLIDES = 3
const val SLIDE_ONE_INX = 0
const val SLIDE_TWO_INX = 1
const val SLIDE_THREE_INX = 2
