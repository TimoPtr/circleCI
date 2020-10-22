package com.kolibree.android.guidedbrushing.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.guidedbrushing.domain.BrushingTipsUseCase
import com.kolibree.android.guidedbrushing.ui.adapter.BrushingTipsData
import com.kolibree.android.guidedbrushing.ui.navigator.GuidedBrushingTipsNavigator
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class GuidedBrushingTipsViewModel(
    initialViewState: GuidedBrushingTipsViewState,
    private val navigator: GuidedBrushingTipsNavigator,
    private val brushingTipsUseCase: BrushingTipsUseCase
) : BaseViewModel<GuidedBrushingTipsViewState, NoActions>(
    initialViewState
) {

    val tips: LiveData<List<BrushingTipsData>> =
        mapNonNull(viewStateLiveData, initialViewState.tips) { viewState ->
            viewState.tips
        }

    fun close() {
        GuidedBrushingTipsAnalytics.close()
        navigator.finish()
    }

    fun onClickGotIt() {
        GuidedBrushingTipsAnalytics.gotIt()
        navigator.finish()
    }

    fun onClickNoShowAgain() {
        disposeOnCleared(::getNoShowAgainCompletable)
    }

    private fun getNoShowAgainCompletable(): Disposable {
        return brushingTipsUseCase.setHasClickedNoShowAgain()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                GuidedBrushingTipsAnalytics.noShowAgain()
                navigator.finish()
            }, Timber::e)
    }

    class Factory @Inject constructor(
        private val navigator: GuidedBrushingTipsNavigator,
        private val brushingTipsUseCase: BrushingTipsUseCase
    ) :
        BaseViewModel.Factory<GuidedBrushingTipsViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GuidedBrushingTipsViewModel(
                viewState ?: GuidedBrushingTipsViewState(),
                navigator,
                brushingTipsUseCase
            ) as T
    }
}
