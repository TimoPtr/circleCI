/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.question

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.Available
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayStatus.NotAvailable
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class QuestionCardViewModel(
    initialViewState: QuestionCardViewState,
    private val questionOfTheDayUseCase: QuestionOfTheDayUseCase,
    private val humHomeNavigator: HumHomeNavigator
) : DynamicCardViewModel<
    QuestionCardViewState,
    QuestionCardInteraction,
    QuestionCardBindingModel>(initialViewState), QuestionCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is QuestionCardBindingModel

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy(::retrieveQuestion)
    }

    override fun onClick() {
        getViewState()?.questionOfTheDay?.let { question ->
            humHomeNavigator.showQuestionOfTheDay(question)
        }
    }

    private fun retrieveQuestion(): Disposable {
        return questionOfTheDayUseCase
            .questionStatusStream()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onNewQuestion, Timber::e)
    }

    private fun onNewQuestion(status: QuestionOfTheDayStatus) {
        updateViewState {
            copy(
                visible = status !is NotAvailable,
                questionOfTheDay = (status as? Available)?.questionOfTheDay
            )
        }
    }

    class Factory @Inject constructor(
        private val dynamicCardListConfiguration: DynamicCardListConfiguration,
        private val questionOfTheDayUseCase: QuestionOfTheDayUseCase,
        private val humHomeNavigator: HumHomeNavigator
    ) : BaseViewModel.Factory<QuestionCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = QuestionCardViewModel(
            viewState ?: QuestionCardViewState.initial(
                dynamicCardListConfiguration.getInitialCardPosition(modelClass)
            ),
            questionOfTheDayUseCase,
            humHomeNavigator
        ) as T
    }
}
