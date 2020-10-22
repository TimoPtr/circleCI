package com.kolibree.android.sba.testbrushing.optimize

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.sba.testbrushing.TestBrushingNavigator
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyBaseTestBrushingViewModel
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.profile.ProfileManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class OptimizeAnalysisViewModel(
    private val profileManager: ProfileManager,
    private val kolibreeConnector: IKolibreeConnector,
    private val testBrushingNavigator: TestBrushingNavigator
) :
    LegacyBaseTestBrushingViewModel<OptimizeAnalysisViewState>(OptimizeAnalysisViewState()) {

    override fun resetActionViewState() = OptimizeAnalysisViewState()

    private val serviceDisposables = CompositeDisposable()

    override fun onStop(owner: LifecycleOwner) {
        serviceDisposables.clear()
    }

    fun onLeftHandClicked() {
        emitState(viewState.copy(isLeftHanded = true))
    }

    fun onRightHandClicked() {
        emitState(viewState.copy(isLeftHanded = false))
    }

    fun onReduceNbBrushing() {

        if (viewState.amountBrushing > 0)
            emitState(viewState.copy(amountBrushing = viewState.amountBrushing - 1))
    }

    fun onAddNbBrushing() {
        emitState(viewState.copy(amountBrushing = viewState.amountBrushing + 1))
    }

    fun onNextClicked() {
        if (!viewState.isFormValid()) return

        kolibreeConnector.currentProfile?.let { profile ->
            serviceDisposables.addSafely(profileManager.updateProfile(kolibreeConnector.accountId,
                profile.copy(handedness = getHandedness(),
                    brushingNumber = viewState.amountBrushing))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateSuccess, this::handleException))
        }
    }

    private fun getHandedness(): Handedness {
        return when (viewState.isLeftHanded) {
            true -> Handedness.LEFT_HANDED
            else -> Handedness.RIGHT_HANDED
        }
    }

    private fun updateSuccess(profile: Profile) {
        testBrushingNavigator.navigateToProgressScreen()
    }

    class Factory @Inject constructor(
        private val profileManager: ProfileManager,
        private val kolibreeConnector: IKolibreeConnector,
        private val testBrushingNavigator: TestBrushingNavigator
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return OptimizeAnalysisViewModel(profileManager, kolibreeConnector, testBrushingNavigator) as T
        }
    }
}
