/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.viewpager

import android.content.Context
import android.view.View
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kolibree.android.defensive.Preconditions

@Keep
class TabFragmentController(
    private val context: Context,
    fragmentManager: FragmentManager,
    private val lifecycle: Lifecycle,
    private val viewPager: ViewPager2,
    private val tabLayout: TabLayout
) {
    private var tabs: List<Tab> = emptyList()

    private val adapter = Adapter(fragmentManager, lifecycle)

    private val tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
        val currentTab = tabs[position]
        tab.text = currentTab.title(context)
    }

    init {
        viewPager.adapter = adapter
        attachMediatorToLifecycle()
    }

    fun setTabs(tabs: List<Tab>) {
        Preconditions.checkArgument(tabs.doNotHaveMoreThanOneDefault())
        Preconditions.checkArgument(tabs.containUniqueIds())

        adapter.calculateDiff(tabs)
        this.tabs = tabs

        // autorefresh doesn't seem to work properly...
        tabLayoutMediator.detach()
        tabLayoutMediator.attach()

        refreshTabLayoutVisibility()
        showOverflowForAtLeastTwoTabs()
        setDefaultCurrentItem()
    }

    fun setCurrentTab(tab: Tab, smoothScroll: Boolean) {
        tabs.indexOf(tab)
            .takeIf { it != -1 }
            ?.let { viewPager.setCurrentItem(it, smoothScroll) }
    }

    private fun attachMediatorToLifecycle() {
        tabLayoutMediator.attach()
        lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onDestroy(owner: LifecycleOwner) {
                tabLayoutMediator.detach()
                lifecycle.removeObserver(this)
            }
        })
    }

    private fun setDefaultCurrentItem() {
        tabs.indexOfFirst { it.isDefault }
            .takeIf { it >= 0 }
            ?.let { positionToSelect -> viewPager.currentItem = positionToSelect }
    }

    private fun showOverflowForAtLeastTwoTabs() {
        viewPager.isUserInputEnabled = tabs.size > 1
    }

    private fun refreshTabLayoutVisibility() {
        tabLayout.visibility = if (tabs.size > 1) View.VISIBLE else View.GONE
    }

    private inner class Adapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        fun calculateDiff(newTabs: List<Tab>) {
            val diffUtilResult = getDiffUtilResult(old = tabs, new = newTabs)
            diffUtilResult.dispatchUpdatesTo(this)
        }

        private fun getDiffUtilResult(
            old: List<Tab>,
            new: List<Tab>
        ): DiffUtil.DiffResult {

            return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    old[oldItemPosition].id == new[newItemPosition].id

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean =
                    old[oldItemPosition] == new[newItemPosition]

                override fun getOldListSize(): Int = old.size

                override fun getNewListSize(): Int = new.size
            })
        }

        override fun getItemCount(): Int = tabs.size

        override fun getItemId(position: Int): Long = getItem(position).id

        override fun containsItem(itemId: Long): Boolean = tabs.any { it.id == itemId }

        override fun createFragment(position: Int): Fragment = getItem(position).fragmentCreator()

        fun getItem(position: Int): Tab = tabs[position]
    }
}

private fun List<Tab>.doNotHaveMoreThanOneDefault(): Boolean = count { it.isDefault } <= 1

private fun List<Tab>.containUniqueIds(): Boolean = map { it.id }.distinct() == map { it.id }
