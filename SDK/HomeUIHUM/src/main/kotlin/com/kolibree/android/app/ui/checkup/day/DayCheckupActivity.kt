/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.day

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.checkup.base.BaseCheckupActivity
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityDayCheckupBinding
import com.kolibree.kml.MouthZone16
import java.util.UUID
import org.threeten.bp.OffsetDateTime

/** Whole day checkup screen (shows all the brushing sessions of a given day) */
@VisibleForApp // Kept for Espresso
class DayCheckupActivity :
    BaseCheckupActivity<
        DayCheckupViewState,
        DayCheckupViewModel.Factory,
        DayCheckupViewModel,
        ActivityDayCheckupBinding>() {

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            viewModel.onPageSelected(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.checkupPager.adapter = DayCheckupAdapter(supportFragmentManager)
    }

    override fun onResume() {
        super.onResume()

        TabLayoutMediator(binding.checkupPagerIndicator, binding.checkupPager) { _, _ ->
            // no-op
        }.attach()
        binding.checkupPager.registerOnPageChangeCallback(pageChangeCallback)
    }

    override fun onPause() {
        binding.checkupPager.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onPause()
    }

    override fun getViewModelClass() = DayCheckupViewModel::class.java

    override fun getLayoutId() = R.layout.activity_day_checkup

    internal fun forDateFromIntent() = intent.getSerializableExtra(EXTRA_FOR_DATE) as OffsetDateTime

    internal inner class DayCheckupAdapter(
        fm: FragmentManager
    ) : FragmentStateAdapter(fm, lifecycle) {

        private var data: List<DayCheckupItem> = listOf()

        fun setItems(items: List<Map<MouthZone16, Float>> = listOf()) {
            data = items.map { checkupData ->
                DayCheckupItem(
                    id = UUID.randomUUID().leastSignificantBits,
                    checkupData = checkupData
                )
            }
            notifyDataSetChanged()
        }

        override fun getItemCount() = data.size

        override fun getItemId(position: Int) = data[position].id

        override fun createFragment(position: Int) = DayCheckupFragment
            .create(data[position].checkupData)
    }

    internal data class DayCheckupItem(
        val id: Long,
        val checkupData: Map<MouthZone16, Float>
    )
}

/**
 * Create a [Intent] to start [DayCheckupActivity]
 *
 * @param ownerContext [Context]
 * @return [Intent]
 */
@Keep
fun startDayCheckupActivityIntent(ownerContext: Context, forDate: OffsetDateTime) = Intent(
    ownerContext,
    DayCheckupActivity::class.java
).apply {
    putExtra(EXTRA_FOR_DATE, forDate)
}

private const val EXTRA_FOR_DATE = "forDate"

@Keep
@BindingAdapter("checkupDataList")
fun ViewPager2.bindCheckupDataList(list: List<Map<MouthZone16, Float>>?) {
    list?.also {
        (adapter as? DayCheckupActivity.DayCheckupAdapter)?.setItems(list) ?: run {
            FailEarly.fail("Checkup binding was executed but adapter is still not set")
        }
    }
}
