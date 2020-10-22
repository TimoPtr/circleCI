package com.kolibree.android.sba.testbrushing.progress

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.sba.testbrushing.TestBrushingNavigator
import com.kolibree.android.sba.testbrushing.base.FadeInAction
import com.kolibree.android.sba.testbrushing.base.NoneAction
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyBaseTestBrushingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@SuppressLint("DeobfuscatedPublicSdkClass")
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
class TestBrushProgressViewModel(
    private val testBrushProgressController: TestBrushProgressController,
    private val navigator: TestBrushingNavigator,
    private val model: ToothbrushModel
) : LegacyBaseTestBrushingViewModel<TestBrushProgressViewState>(TestBrushProgressViewState()) {

    override fun initViewState(): TestBrushProgressViewState {
        return viewState.copy(hasBuildUpStep = model.isPlaqless)
    }

    @VisibleForTesting
    val tickerDisposable = testBrushProgressController.controllerObservable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
        { count -> onTick(count) },
        { t -> t.printStackTrace() }
    )

    @VisibleForTesting
    fun onTick(count: Long) {

        when (count) {
            1L -> emitState(viewState.copy(isProgressStep1Completed = true, action = FadeInAction(count.toInt())))
            2L -> emitState(viewState.copy(isProgressStep2Completed = true, action = FadeInAction(count.toInt())))
            3L -> emitState(viewState.copy(isProgressStep3Completed = true, action = FadeInAction(count.toInt())))
            4L -> emitState(viewState.copy(isProgressStep4Completed = true, action = FadeInAction(count.toInt())))
            5L -> {
                navigator.navigateToResultsScreen()
                disposeTicker()
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        disposeTicker()
    }

    @VisibleForTesting
    fun disposeTicker() = tickerDisposable.forceDispose()

    @VisibleForTesting
    fun getController() = testBrushProgressController

    override fun resetActionViewState() = viewState.copy(action = NoneAction)

    @Suppress("UNCHECKED_CAST")
    class Factory constructor(
        private val navigator: TestBrushingNavigator,
        private val testBrushProgressController: TestBrushProgressController,
        private val model: ToothbrushModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TestBrushProgressViewModel(testBrushProgressController, navigator, model) as T
        }
    }
}
