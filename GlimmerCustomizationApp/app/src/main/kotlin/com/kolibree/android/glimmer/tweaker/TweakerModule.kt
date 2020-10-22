/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker

import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.base.createViewModelAndBindToLifeCycle
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.glimmer.tweaker.curve.CurveFragment
import com.kolibree.android.glimmer.tweaker.led.mode.ModeLedFragment
import com.kolibree.android.glimmer.tweaker.led.signal.LedSignalFragment
import com.kolibree.android.glimmer.tweaker.led.special.SpecialLedFragment
import com.kolibree.android.glimmer.tweaker.mode.ModeFragment
import com.kolibree.android.glimmer.tweaker.pattern.PatternFragment
import com.kolibree.android.glimmer.tweaker.sequence.SequenceFragment
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class TweakerModule {

    internal companion object {

        @Provides
        fun providesNavigator(activity: TweakerActivity): TweakerNavigator {

            @Suppress("RemoveExplicitTypeArguments")
            return activity.createNavigatorAndBindToLifecycle(TweakerNavigator::class)
        }

        @Provides
        fun providesSharedViewModel(
            activity: TweakerActivity,
            tweakerViewModelFactory: TweakerViewModel.Factory
        ): TweakerSharedViewModel {
            return activity.createViewModelAndBindToLifeCycle<TweakerViewModel> { tweakerViewModelFactory }
        }
    }

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeModeFragment(): ModeFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeSequenceFragment(): SequenceFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeCurveFragment(): CurveFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeBrushingPatternFragment(): PatternFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeModeLedFragment(): ModeLedFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeLedSignalFragment(): LedSignalFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeSpecialLedFragment(): SpecialLedFragment
}
