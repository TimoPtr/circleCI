/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.brushhead.worker

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker.Result
import androidx.work.NetworkType
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadRepository
import com.kolibree.android.app.ui.toothbrushsettings.worker.Builder
import com.kolibree.android.app.ui.toothbrushsettings.worker.Factory
import com.kolibree.android.app.ui.toothbrushsettings.worker.ReplaceBrushHeadWorker
import com.kolibree.android.app.ui.toothbrushsettings.worker.ReplaceBrushHeadWorker.Companion.TOOTHBRUSH_MAC
import com.kolibree.android.app.ui.toothbrushsettings.worker.ReplaceBrushHeadWorker.Companion.TOOTHBRUSH_SERIAL_NUMBER
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.BaseInstrumentationTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.OffsetDateTime

class ReplaceBrushHeadWorkerTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val brushHeadRepository: BrushHeadRepository = mock()
    private val builder = Builder()
    private val factory = Factory(brushHeadRepository)

    private lateinit var worker: ReplaceBrushHeadWorker

    private fun buildWorkerWithData(mac: String, serial: String) {
        val inputData = workDataOf(
            TOOTHBRUSH_MAC to mac,
            TOOTHBRUSH_SERIAL_NUMBER to serial
        )

        worker = TestListenableWorkerBuilder<ReplaceBrushHeadWorker>(
            context = context(),
            inputData = inputData
        ).setWorkerFactory(factory).build() as ReplaceBrushHeadWorker
    }

    @Test
    fun startWork_should_succeed_when_notifyBrushHeadReplaced_complete() {
        val (mac, serial, _) = startWorkHappyPath()

        buildWorkerWithData(mac, serial)
        val result = worker.startWork().get()

        assertEquals(Result.success(), result)
    }

    @Test
    fun startWork_should_retry_when_the_call_to_api_fails() {
        val (mac, serial, localDate) = startWorkHappyPath()

        whenever(brushHeadRepository.sendReplacedDateToApiCompletable(mac, serial, localDate))
            .thenReturn(Completable.error(Exception()))

        buildWorkerWithData(mac, serial)
        val result = worker.startWork().get()

        assertEquals(Result.retry(), result)
    }

    @Test
    fun build_config_should_rely_on_internet() {
        val buildRequest = builder.buildRequest()

        assertEquals(NetworkType.CONNECTED, buildRequest.workSpec.constraints.requiredNetworkType)
    }

    private fun startWorkHappyPath(): Triple<String, String, OffsetDateTime> {
        val mac = "aa:bb"
        val serial = "serial"
        val localDate = TrustedClock.getNowOffsetDateTime()

        whenever(brushHeadRepository.brushHeadInformationOnce(mac))
            .thenReturn(Single.just(brushHeadInfo(mac = mac, resetDate = localDate)))

        whenever(
            brushHeadRepository.sendReplacedDateToApiCompletable(
                mac,
                serial,
                localDate
            )
        )
            .thenReturn(Completable.complete())

        return Triple(mac, serial, localDate)
    }
}
