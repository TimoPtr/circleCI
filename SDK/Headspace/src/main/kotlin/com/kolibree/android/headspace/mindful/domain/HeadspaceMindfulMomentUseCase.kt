/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.domain

import android.content.Context
import com.kolibree.android.annotation.VisibleForApp
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

@VisibleForApp
interface HeadspaceMindfulMomentUseCase {

    /**
     * Provides Headspace mindful moment screen details for specified day. It can be either
     * - [HeadspaceMindfulMomentStatus.Available]
     * - [HeadspaceMindfulMomentStatus.NotAvailable]
     */
    fun getHeadspaceMindfulMomentStatus(): Flowable<HeadspaceMindfulMomentStatus>
}

internal class HeadspaceMindfulMomentUseCaseImpl @Inject constructor(
    private val context: Context
) : HeadspaceMindfulMomentUseCase {

    // TODO At this point it is just a mock implementation. We will replace that one API is ready.
    override fun getHeadspaceMindfulMomentStatus(): Flowable<HeadspaceMindfulMomentStatus> {
        val momentNumber = Random.nextInt(0..MINDFUL_MOMENT_COLORS.lastIndex)
        return fetchLottieAnimationJson(momentNumber)
            .map<HeadspaceMindfulMomentStatus> { json ->
                HeadspaceMindfulMomentStatus.Available(
                    HeadspaceMindfulMoment(
                        quote = MINDFUL_MOMENT_QUOTE,
                        animationJson = json,
                        backgroundColorHexString = MINDFUL_MOMENT_COLORS[momentNumber].first,
                        textColorHexString = MINDFUL_MOMENT_COLORS[momentNumber].second
                    )
                )
            }
            .toFlowable()
    }

    private fun fetchLottieAnimationJson(fileNumber: Int): Single<String> = Single.fromCallable {
        context.assets
            .open("mindful_moment_$fileNumber.json")
            .bufferedReader()
            .use {
                it.readText()
            }
    }.doOnSubscribe {
        check(fileNumber in 0..MINDFUL_MOMENT_COLORS.lastIndex) { "Invalid input" }
    }

    private companion object {
        const val MINDFUL_MOMENT_QUOTE =
            "Mindfulness is less about changing the mind and more about changing our perspective"
        val MINDFUL_MOMENT_COLORS = listOf(
            Pair("#f47c31", "#ffffff"),
            Pair("#ffffff", "#544F59"),
            Pair("#F5F2ED", "#544F59"),
            Pair("#FFE14F", "#544F59"),
            Pair("#413D45", "#FFFFFF"),
            Pair("#32AAFF", "#FFFFFF")
        )
    }
}

internal object NoOpHeadspaceMindfulMomentUseCase : HeadspaceMindfulMomentUseCase {

    override fun getHeadspaceMindfulMomentStatus(): Flowable<HeadspaceMindfulMomentStatus> =
        Flowable.empty()
}
