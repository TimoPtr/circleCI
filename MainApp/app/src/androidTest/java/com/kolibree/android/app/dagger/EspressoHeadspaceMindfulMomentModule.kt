/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.headspace.mindful.di.HeadspaceMindfulMomentCoreModule
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMoment
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentStatus
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentUseCase
import dagger.Module
import dagger.Provides
import io.reactivex.Flowable
import io.reactivex.Single

@Module(includes = [HeadspaceMindfulMomentCoreModule::class])
internal class EspressoHeadspaceMindfulMomentModule {

    @Provides
    fun provideHeadspaceUseCase(): HeadspaceMindfulMomentUseCase =
        FakeHeadspaceMindfulMomentUseCase()

    internal class FakeHeadspaceMindfulMomentUseCase : HeadspaceMindfulMomentUseCase {
        override fun getHeadspaceMindfulMomentStatus(): Flowable<HeadspaceMindfulMomentStatus> =
            fetchLottieAnimationJson()
                .map<HeadspaceMindfulMomentStatus> {
                    HeadspaceMindfulMomentStatus.Available(
                        HeadspaceMindfulMoment(
                            quote = MINDFUL_MOMENT_QUOTE,
                            animationJson = it,
                            backgroundColorHexString = MINDFUL_MOMENT_BACKGROUND,
                            textColorHexString = MINDFUL_MOMENT_FOREGROUND
                        )
                    )
                }.toFlowable()

        private fun fetchLottieAnimationJson(): Single<String> = Single.fromCallable {
            InstrumentationRegistry.getInstrumentation()
                .context
                .assets
                .open(MINDFUL_MOMENT_ANIMATION_FILE)
                .bufferedReader()
                .use {
                    it.readText()
                }
        }

        companion object {
            private const val MINDFUL_MOMENT_ANIMATION_FILE = "mindful_moment.json"

            const val MINDFUL_MOMENT_QUOTE = "SAMPLE QUOTE"
            const val MINDFUL_MOMENT_BACKGROUND = "#000000"
            const val MINDFUL_MOMENT_FOREGROUND = "#FFFFFF"
        }
    }
}
