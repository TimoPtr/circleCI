/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide1

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.plaqless.R
import com.kolibree.android.plaqless.databinding.FragmentSlideOneBinding

internal class SlideOneFragment :
    BaseMVIFragment<SlideOneViewState,
        SlideOneAction,
        SlideOneViewModel.Factory,
        SlideOneViewModel,
        FragmentSlideOneBinding>() {

    companion object {
        @JvmStatic
        fun newInstance(): SlideOneFragment {
            val fragment = SlideOneFragment()

            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        withPreDraw {
            binding.line1.drawLineBetween(binding.slide1Info1, binding.slide1Description1)

            binding.line2.drawLineBetween(binding.slide1Info2, binding.slide1Description2)

            binding.line3.drawLineBetween(binding.slide1Info3, binding.slide1Description3)
        }
    }

    private fun withPreDraw(block: () -> Unit) {
        val root = binding.root
        root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                root.viewTreeObserver.removeOnPreDrawListener(this)
                block()
                return true
            }
        })
    }

    override fun getViewModelClass(): Class<SlideOneViewModel> = SlideOneViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_slide_one

    override fun execute(action: SlideOneAction) {
        // no-op
    }
}
