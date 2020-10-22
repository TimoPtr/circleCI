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
import com.kolibree.android.app.ui.brushhead.repo.model.BrushHeadInformation
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionUseCase
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadDateWorker
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadDateWorker.Companion.TOOTHBRUSH_MAC
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadDateWorker.Companion.TOOTHBRUSH_SERIAL_NUMBER
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadDateWorkerBuilder
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadDateWorkerFactory
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.OffsetDateTime

class SyncBrushHeadDateWorkerTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val brushHeadConditionUseCase: BrushHeadConditionUseCase = mock()
    private val builder = SyncBrushHeadDateWorkerBuilder()
    private val factory = SyncBrushHeadDateWorkerFactory(brushHeadConditionUseCase)

    private lateinit var worker: SyncBrushHeadDateWorker

    private fun buildWorkerWithData(mac: String, serial: String) {
        val inputData = workDataOf(
            TOOTHBRUSH_MAC to mac,
            TOOTHBRUSH_SERIAL_NUMBER to serial
        )

        worker = TestListenableWorkerBuilder<SyncBrushHeadDateWorker>(
            context = context(),
            inputData = inputData
        ).setWorkerFactory(factory).build() as SyncBrushHeadDateWorker
    }

    @Test
    fun startWork_should_succeed_when_getBrushHeadInformationFromApi_returns_a_date() {
        val (mac, serial, expectedDateToWrite) = startWorkHappyPath()

        buildWorkerWithData(mac, serial)
        val result = worker.startWork().get()

        verify(brushHeadConditionUseCase).writeBrushHeadInfo(brushHeadInfo(mac = mac, resetDate = expectedDateToWrite))
        verify(brushHeadConditionUseCase, never()).updateBrushHeadDateIfNeeded(any())
        assertEquals(Result.success(), result)
    }

    @Test
    fun startWork_should_succeed_and_update_the_brush_head_data_when_getBrushHeadInformationFromApi_returns_a_404_error() {
        val (mac, serial, _) = startWorkHappyPath()

        whenever(brushHeadConditionUseCase.getBrushHeadInformationFromApi(mac, serial))
            .thenReturn(Single.error(ApiError("", ApiErrorCode.BRUSH_HEAD_NON_EXISTING, "")))

        whenever(brushHeadConditionUseCase.updateBrushHeadDateIfNeeded(mac))
            .thenReturn(Maybe.just(AccountToothbrush("", "", ARA, 0)))

        buildWorkerWithData(mac, serial)
        val result = worker.startWork().get()

        verify(brushHeadConditionUseCase).updateBrushHeadDateIfNeeded(mac)
        verify(brushHeadConditionUseCase, never()).writeBrushHeadInfo(any())
        assertEquals(Result.success(), result)
    }

    @Test
    fun startWork_should_fails_when_the_error_is_not_handled() {
        val (mac, serial, _) = startWorkHappyPath()

        whenever(brushHeadConditionUseCase.getBrushHeadInformationFromApi(mac, serial))
            .thenReturn(Single.error(UnknownError()))

        buildWorkerWithData(mac, serial)
        val result = worker.startWork().get()

        verify(brushHeadConditionUseCase, never()).updateBrushHeadDateIfNeeded(any())
        verify(brushHeadConditionUseCase, never()).writeBrushHeadInfo(any())
        assertEquals(Result.failure(), result)
    }

    @Test
    fun build_config_should_rely_on_internet() {
        val buildRequest = builder.buildRequest()

        assertEquals(NetworkType.CONNECTED, buildRequest.workSpec.constraints.requiredNetworkType)
    }

    private fun startWorkHappyPath(): Triple<String, String, OffsetDateTime> {
        val mac = "aa:bb"
        val serial = "serial"
        val expectedDate = TrustedClock.getNowOffsetDateTime()

        val brushHeadInfo = brushHeadInfo(mac = mac, resetDate = expectedDate)
        whenever(brushHeadConditionUseCase.getBrushHeadInformationFromApi(mac, serial))
            .thenReturn(Single.just(brushHeadInfo))

        whenever(brushHeadConditionUseCase.writeBrushHeadInfo(brushHeadInfo))
            .thenReturn(Completable.complete())

        return Triple(mac, serial, expectedDate)
    }
}

internal fun brushHeadInfo(
    mac: String = KLTBConnectionBuilder.DEFAULT_MAC,
    resetDate: OffsetDateTime = TrustedClock.getNowOffsetDateTime(),
    percentageLeft: Int = 100
): BrushHeadInformation {
    return BrushHeadInformation(
        macAddress = mac,
        resetDate = resetDate,
        percentageLeft = percentageLeft
    )
}
