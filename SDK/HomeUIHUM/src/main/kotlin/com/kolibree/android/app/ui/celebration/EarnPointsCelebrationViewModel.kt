/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.celebration

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.utils.setSpan
import com.kolibree.android.brushingquiz.BR
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.rewards.EarnPointsChallengeUseCase
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.databinding.livedata.distinctUntilChanged
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import me.tatarka.bindingcollectionadapter2.ItemBinding
import timber.log.Timber

internal class EarnPointsCelebrationViewModel(
    initialViewState: EarnPointsCelebrationViewState?,
    challenges: List<CompleteEarnPointsChallenge>,
    private val navigator: EarnPointsCelebrationNavigator,
    private val resourceProvider: EarnPointsCelebrationResourceProvider,
    private val earnPointsChallengeUseCase: EarnPointsChallengeUseCase
) : BaseViewModel<EarnPointsCelebrationViewState, BaseAction>(
    initialViewState ?: EarnPointsCelebrationViewState.from(challenges)
) {

    val items = mapNonNull(viewStateLiveData, emptyList()) { viewState ->
        viewState.items
    }.distinctUntilChanged()

    val selectedIndex = mapNonNull(viewStateLiveData, 0) { viewState ->
        viewState.selectedIndex
    }

    init {
        FailEarly.failInConditionMet(
            condition = getViewState()?.items.isNullOrEmpty(),
            message = "There must be at least one item to display!"
        )

        disposeOnCleared { challenges.markAsConsumed() }
    }

    fun itemBinding(): ItemBinding<EarnPointsChallenge> {
        return ItemBinding
            .of<EarnPointsChallenge>(BR.item, R.layout.item_celebration)
            .bindExtra(BR.viewModel, this)
    }

    fun animate(item: EarnPointsChallenge): LiveData<Boolean> {
        return mapNonNull(viewStateLiveData, false) { viewState ->
            viewState.items.indexOf(item) == viewState.selectedIndex
        }
    }

    fun body(context: Context, challenge: EarnPointsChallenge): Spanned {
        val challengeName = context.getString(resourceProvider.getName(challenge))

        val bodyPoints = context.resources.getQuantityString(
            R.plurals.earn_points_celebration_body_points,
            challenge.points,
            challenge.points.toString()
        )

        val body = context.getString(
            R.string.earn_points_celebration_body,
            challengeName,
            bodyPoints
        )

        return SpannableStringBuilder(body).apply {
            setSpan(challengeName, StyleSpan(Typeface.BOLD))
            setSpan(bodyPoints, StyleSpan(Typeface.BOLD))
        }
    }

    fun onButtonClick() {
        val viewState = getViewState() ?: return
        EarnPointsCelebrationAnalytics.done()

        if (viewState.selectedIndex >= viewState.items.size - 1) {
            navigator.finish()
        } else {
            updateViewState { copy(selectedIndex = selectedIndex + 1) }
        }
    }

    private fun List<CompleteEarnPointsChallenge>.markAsConsumed(): Disposable {
        return earnPointsChallengeUseCase
            .markAsConsumed(this)
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    class Factory @Inject constructor(
        private val challenges: List<CompleteEarnPointsChallenge>,
        private val navigator: EarnPointsCelebrationNavigator,
        private val resourceProvider: EarnPointsCelebrationResourceProvider,
        private val earnPointsChallengeUseCase: EarnPointsChallengeUseCase
    ) : BaseViewModel.Factory<EarnPointsCelebrationViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EarnPointsCelebrationViewModel(
                viewState,
                challenges,
                navigator,
                resourceProvider,
                earnPointsChallengeUseCase
            ) as T
    }
}
