package com.kolibree.android.app.ui.fragment.home

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.FragmentPagerAdapter
import com.kolibree.android.app.ui.common.NonSwipeableViewPager

/**
 * ViewPager that notifies children when they are active and their tab is clicked again
 *
 * This ViewPager is non swipeable
 */
class ReclickableViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    NonSwipeableViewPager(context, attrs) {

    override fun setCurrentItem(nextPosition: Int) {
        val reclickableChildren = getReclickableTab(nextPosition)

        super.setCurrentItem(nextPosition)

        reclickableChildren?.onReclicked()
    }

    override fun setCurrentItem(nextPosition: Int, smoothScroll: Boolean) {
        val reclickableChildren = getReclickableTab(nextPosition)

        super.setCurrentItem(nextPosition, smoothScroll)

        reclickableChildren?.onReclicked()
    }

    /**
     * Returns a ReclickableChildren if the next active position is the same as the active one and the children is a
     * Fragment that implements ReclickableChildren
     *
     * If those conditions are not met, it returns null
     */
    private fun getReclickableTab(nextPosition: Int): ReclickableChildren? {
        if (currentItem == nextPosition && adapter != null) {
            val currentItem = (adapter as? FragmentPagerAdapter)?.instantiateItem(this, nextPosition)

            return currentItem as? ReclickableChildren
        }

        return null
    }
}

interface ReclickableChildren {
    /**
     * User clicked on the already Active tab
     */
    fun onReclicked()
}
