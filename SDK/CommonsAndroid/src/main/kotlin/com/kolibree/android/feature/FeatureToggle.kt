/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature

import androidx.annotation.Keep
import com.kolibree.android.feature.FeatureToggle.Companion
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

/**
 * Represents toggle for specified feature of value [T]. Pairs feature with its value.
 *
 * Should not contain any injections, as this may lower their usage flexibility. If you need
 * more sophisticated logic underneath your toggle, please consider adding a [Companion] to it.
 *
 * In order to make use of your toggle, you need to add it to Dagger graph.
 * Below example for persistent toggle:
 *
 * @Provides
 * @IntoSet
 * fun provideMyFeatureToggle(context: Context): FeatureToggle<*> {
 *     return PersistentFeatureToggle(context, MyFeature)
 * }
 *
 * Check [SampleFeatureToggleModule] for more details
 */
@Keep
interface FeatureToggle<T : Any> {

    /**
     * Feature for this toggle
     */
    val feature: Feature<T>

    /**
     * Value of the toggle
     */
    var value: T

    /**
     * A companion class for [FeatureToggle], associated with one particular toggle.
     *
     * When user asks to change associated toggle's value in secret settings, Companion is
     * responsible for execution of the associated logic - calling web service, changing value
     * of other toggles etc.
     *
     * This allows toggles to stay independent of any other object, so they can be injected
     * and used in very place we want.
     *
     * Sample setup:
     *
     *  class MyFeatureToggleCompanion @Inject constructor(
     *      featureToggleSet: FeatureToggleSet,
     *      private val sampleLogicRepository: SampleLogicRepository
     *  ) : FeatureToggle.Companion(featureToggleSet, MyFeature) {
     *
     *      override fun getInitialValue() {
     *          //no-op
     *      }
     *
     *      override fun executeUpdate(enabled: Boolean): Completable =
     *          sampleLogicRepository.executeLogicAssociatedWithMyFeature(enabled)
     *  }
     *
     *  @Provides
     *  @IntoSet
     *  fun provideMyFeatureToggleCompanionIntoSet(
     *      companion: MyFeatureToggleCompanion
     *  ) : FeatureToggle.Companion<*> {
     *      return companion
     *  }
     *
     * @param featureToggleSet all available feature toggles in the app
     * @param associatedFeature feature we want to associate the companion with
     */
    @Keep
    abstract class Companion<T : Any>(
        featureToggleSet: FeatureToggleSet,
        associatedFeature: Feature<T>
    ) {

        @Suppress("UNCHECKED_CAST")
        protected val toggle = featureToggleSet.toggleForFeature(associatedFeature)

        val feature = toggle.feature

        val featureValue = toggle.value

        /**
         * If your companion needs an initialization stage - this is the place to put it.
         */
        abstract fun initialize()

        fun update(value: T): Completable =
            executeUpdate(value)
                .subscribeOn(Schedulers.io())
                .doOnComplete { toggle.value = value }

        /**
         * Main logic placeholder. Called when the toggle value is about to change.
         * @param value new value of the toggle to be handled by companion
         * @return Completable that ends with success if the logic execution went OK or an exception
         * if there was any issue during execution.
         */
        protected abstract fun executeUpdate(value: T): Completable
    }
}
