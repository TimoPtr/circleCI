/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.host

import android.annotation.SuppressLint
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModelSet
import com.kolibree.android.app.ui.card.DynamicCardViewState
import com.kolibree.android.failearly.FailEarly
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * Holds the state of all cards added to the dynamic card list.
 *
 * NOTE: if you want to add a new card to the home view, this class doesn't need any changes.
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
@Parcelize
data class DynamicCardHostViewState(
    private val cardsStates: Map<Class<out DynamicCardBindingModel>, DynamicCardBindingModel>
) : BaseViewState {

    init {
        sanitizeState(
            cardsStates
        )
    }

    /**
     * Returns sorted list of cards to be displayed on the UI.
     * Only cards that return true from [DynamicCardBindingModel.visible] are present in the list.
     * This way DiffUtils can calculate proper diff when card visibility state changed.
     */
    @IgnoredOnParcel
    val cards: List<DynamicCardBindingModel> = cardsStates
        .map { it.value }
        .filter { it.visible }

    /**
     * Returns a copy of the state with updated state of particular [DynamicCardBindingModel]
     */
    fun copyWithUpdatedCardState(cardState: DynamicCardBindingModel): DynamicCardHostViewState {
        val updatedStates = cardsStates.toMutableMap()
            .also { it[cardState::class.java] = cardState }
            .toList()
            .sortedWith(positionSorter)
            .toMap()
        return copy(cardsStates = updatedStates)
    }

    companion object {

        /**
         * Initial state with states taken from injected home card view models.
         */
        fun fromViewModels(cardViewModels: DynamicCardViewModelSet) =
            fromViewStates(
                cardViewModels.mapNotNull { it.getViewState() })

        /**
         * Initial state with states taken from injected home card view states.
         */
        private fun fromViewStates(cardStates: List<DynamicCardViewState>) =
            fromBindingModels(
                cardStates.map { it.asBindingModel() })

        /**
         * Initial state with states taken from injected home card binding models.
         */
        fun fromBindingModels(cardStates: List<DynamicCardBindingModel>) =
            DynamicCardHostViewState(cardStates
                .map { it::class.java to it }
                .sortedWith(positionSorter)
                .toMap()
            )

        /**
         * Map should not contain cards with the same layout or position - this method detects this
         * implementation error
         */
        fun sanitizeState(updatedStates: Map<Class<out DynamicCardBindingModel>, DynamicCardBindingModel>) {
            FailEarly.failInConditionMet(
                findDuplicatesForCriteria(
                    updatedStates
                ) { it.layoutId },
                "Please check `layoutId` of cards, there is a duplicate"
            )
            FailEarly.failInConditionMet(
                findDuplicatesForCriteria(
                    updatedStates
                ) { it.position },
                "Please check `position` of cards, there is a duplicate"
            )
        }

        private inline fun <reified T> findDuplicatesForCriteria(
            updatedStates: Map<Class<out DynamicCardBindingModel>, DynamicCardBindingModel>,
            crossinline criteria: (DynamicCardBindingModel) -> T
        ): Boolean {
            return updatedStates.map { it.value }
                .groupingBy(criteria)
                .eachCount()
                .any { it.value > 1 }
        }

        /**
         * Card sorting comparator
         * TODO add sorting based on user settings when card reordering is added
         */
        private val positionSorter =
            Comparator<Pair<Class<out DynamicCardBindingModel>, DynamicCardBindingModel>> { lhs, rhs ->
                return@Comparator lhs.second.position.compareTo(rhs.second.position)
            }
    }
}
