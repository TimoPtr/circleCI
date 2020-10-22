package com.kolibree.android.sba.testbrushing.intro

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.sba.testbrushing.TestBrushingNavigator
import com.kolibree.android.sba.testbrushing.base.mvi.BaseTestBrushingViewModel
import javax.inject.Inject

internal class TestBrushIntroViewModel(
    viewState: TestBrushIntroViewState?,
    private val navigator: TestBrushingNavigator
) : BaseTestBrushingViewModel<TestBrushIntroViewState>(viewState ?: TestBrushIntroViewState()) {

    fun userClickNext() {
        navigator.navigateToSessionScreen()
    }

    fun userClickDoLater() {
        navigator.finishScreen()
    }

    class Factory @Inject constructor(
        private val navigator: TestBrushingNavigator
    ) : BaseViewModel.Factory<TestBrushIntroViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TestBrushIntroViewModel(viewState, navigator) as T
        }
    }
}
