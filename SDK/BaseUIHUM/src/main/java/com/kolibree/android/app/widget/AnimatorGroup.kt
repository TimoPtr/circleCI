package com.kolibree.android.app.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.constraintlayout.widget.ConstraintLayout
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.failearly.FailEarly

/**
 * Convenience class for calling animations on the group of [View]s,
 * defined inside [ConstraintLayout]. Inspired by [androidx.constraintlayout.widget.Group].
 */
@VisibleForApp
class AnimatorGroup @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintHelper(context, attrs, defStyleAttr) {

    private var launched = false

    private val animators = mutableListOf<Pair<ValueAnimator, (View, Any) -> Unit>>()

    override fun init(attrs: AttributeSet) {
        super.init(attrs)
        launched = false
        mUseViewMeasure = false
    }

    fun addAnimator(animator: ValueAnimator, updateFunction: (View, Any) -> Unit) {
        animators += animator to updateFunction
    }

    fun addAnimator(animatorPair: Pair<ValueAnimator, (View, Any) -> Unit>) {
        animators += animatorPair
    }

    override fun updatePreLayout(container: ConstraintLayout) {
        FailEarly.failIfNotExecutedOnMainThread()

        if (launched) return

        if (mReferenceIds != null) {
            setIds(mReferenceIds)
        }
        animators.forEach { animatorPair ->
            animatorPair.first.addUpdateListener { valueAnimator ->
                for (i in 0 until mCount) {
                    val id = mIds[i]
                    val view = container.getViewById(id)
                    if (view != null) {
                        animatorPair.second(view, valueAnimator.animatedValue)
                    }
                }
            }
            animatorPair.first.start()
        }
        launched = true
    }

    fun clear() {
        animators.clear()
        launched = false
    }
}
