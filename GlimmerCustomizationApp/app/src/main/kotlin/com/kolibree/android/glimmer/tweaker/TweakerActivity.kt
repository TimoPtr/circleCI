/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.glimmer.R
import com.kolibree.android.glimmer.databinding.ActivityTweakerBinding
import com.kolibree.android.glimmer.tweaker.TweakerActions.ShowError
import com.kolibree.android.glimmer.tweaker.curve.CurveFragment
import com.kolibree.android.glimmer.tweaker.led.mode.ModeLedFragment
import com.kolibree.android.glimmer.tweaker.led.signal.LedSignalFragment
import com.kolibree.android.glimmer.tweaker.led.special.SpecialLedFragment
import com.kolibree.android.glimmer.tweaker.mode.ModeFragment
import com.kolibree.android.glimmer.tweaker.pattern.PatternFragment
import com.kolibree.android.glimmer.tweaker.sequence.SequenceFragment

internal class TweakerActivity :
    BaseMVIActivity<
        TweakerViewState,
        TweakerActions,
        TweakerViewModel.Factory,
        TweakerViewModel,
        ActivityTweakerBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tweakerPager.adapter = TweakerFragmentStateAdapter(
            fm = supportFragmentManager,
            lifecycle = lifecycle
        )

        TabLayoutMediator(binding.tweakerPagerIndicator, binding.tweakerPager) { tab, position ->
            tab.setText(titleForTabAt(position))
        }.attach()
    }

    override fun getViewModelClass(): Class<TweakerViewModel> = TweakerViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_tweaker

    override fun execute(action: TweakerActions) {
        when (action) {
            is ShowError ->
                Toast.makeText(this, action.error.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun titleForTabAt(position: Int): Int {
        if (position >= fragments.size) {
            throw IllegalArgumentException("Invalid position $position")
        }

        return fragments[position].first
    }
}

@Keep
fun startTweakerActivity(context: Context) = context
    .startActivity(Intent(context, TweakerActivity::class.java))

internal class TweakerFragmentStateAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment {
        if (position >= fragments.size) {
            throw IllegalArgumentException("Invalid position $position")
        }

        return fragments[position].second()
    }
}

private val fragments = listOf<Pair<Int, () -> Fragment>>(
    R.string.mode to { ModeFragment.newInstance() },
    R.string.sequence to { SequenceFragment.newInstance() },
    R.string.brushing_pattern to { PatternFragment.newInstance() },
    R.string.curve to { CurveFragment.newInstance() },
    R.string.mode_led to { ModeLedFragment.newInstance() },
    R.string.led_signal to { LedSignalFragment.newInstance() },
    R.string.special_led to { SpecialLedFragment.newInstance() }
)
