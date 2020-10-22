/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.kolibree.android.app.anim.HumAnimator
import com.kolibree.android.app.anim.add
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.insets.withWindowInsetsOwner
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.app.widget.AnimatorGroup
import com.kolibree.android.homeui.hum.R

internal abstract class AnimatedBottomGroupFragment<
    VS : BaseViewState,
    A : BaseAction,
    VMF : BaseViewModel.Factory<VS>,
    VM : BaseViewModel<VS, A>,
    B : ViewDataBinding> : BaseMVIFragment<VS, A, VMF, VM, B>() {

    abstract fun animatedBottomGroup(): AnimatorGroup?

    private var initialShowup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialShowup = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        withWindowInsetsOwner { insets ->
            view?.setPadding(0, insets.topStatusBarWindowInset(), 0, insets.bottomNavigationBarInset())
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wireAnimations()
    }

    protected fun navigate(actionId: Int) = findNavController().navigateSafe(actionId)

    private fun wireAnimations() {
        animatedBottomGroup()?.let {
            it.clear()
            if (initialShowup) {
                val resources = it.context.resources
                val duration =
                    resources.getInteger(R.integer.onboarding_transition_duration).toLong()
                val distance =
                    resources.getDimension(R.dimen.onboarding_fade_up_vertical_distance)

                it.add(HumAnimator.FadeIn(duration))
                it.add(HumAnimator.TranslateY(from = distance, to = 0f, duration = duration))
            } else {
                it.add(HumAnimator.Show)
            }
            initialShowup = false
        }
    }
}
