/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.job

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker.Result
import androidx.work.NetworkType
import androidx.work.testing.TestListenableWorkerBuilder
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayUseCase
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.worker.LazyWorkManagerImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.ChronoUnit

class QuestionOfTheDayWorkerTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val useCase: QuestionOfTheDayUseCase = mock()
    private val builder = QuestionOfTheDayWorker.Builder()
    private val workerConfiguration =
        QuestionOfTheDayWorkerConfigurator(
            builder,
            LazyWorkManagerImpl(ApplicationContext(context()))
        )
    private val factory = QuestionOfTheDayWorker.Factory(useCase, workerConfiguration)

    private lateinit var worker: QuestionOfTheDayWorker

    override fun setUp() {
        super.setUp()

        worker = TestListenableWorkerBuilder<QuestionOfTheDayWorker>(context = context())
            .setWorkerFactory(factory)
            .build() as QuestionOfTheDayWorker
    }

    @Test
    fun startWork_refresh_questions_and_returns_success() {
        whenever(useCase.refreshQuestions()).thenReturn(Completable.complete())

        val result = worker.startWork().get()

        verify(useCase).refreshQuestions()
        assertEquals(result, Result.success())
    }

    @Test
    fun startWork_should_retry_if_it_fails() {
        whenever(useCase.refreshQuestions()).thenReturn(Completable.error(Exception()))

        val result = worker.startWork().get()

        verify(useCase).refreshQuestions()
        assertEquals(result, Result.retry())
    }

    @Test
    fun if_today_is_3h59am_the_builder_should_build_the_config_for_4h00am_the_same_day() {
        val hours = 3
        val minutes = 59
        val dayOfMonth = 21
        val todayDate = OffsetDateTime.of(
            2020, 1, dayOfMonth, hours,
            minutes, 0, 0, ZoneOffset.UTC
        )

        TrustedClock.setFixedDate(todayDate)

        val buildRequest = builder.buildRequest()

        // 60000 = 1 minute
        val expectedDelay = Duration.of(1, ChronoUnit.MINUTES).toMillis()
        assertEquals(expectedDelay, buildRequest.workSpec.initialDelay)
    }

    @Test
    fun if_today_is_4h01am_the_builder_should_build_the_config_for_4h00am_the_next_day() {

        val hours = 4
        val minutes = 1
        val dayOfMonth = 21
        val todayDate = OffsetDateTime.of(
            2020, 1, dayOfMonth, hours,
            minutes, 0, 0, ZoneOffset.UTC
        )

        TrustedClock.setFixedDate(todayDate)

        val buildRequest = builder.buildRequest()

        val expectedDelay = Duration.of(1, ChronoUnit.DAYS).minusMinutes(1).toMillis()
        assertEquals(expectedDelay, buildRequest.workSpec.initialDelay)
    }

    @Test
    fun build_config_should_rely_on_internet() {
        val buildRequest = builder.buildRequest()

        assertEquals(NetworkType.CONNECTED, buildRequest.workSpec.constraints.requiredNetworkType)
    }
}
