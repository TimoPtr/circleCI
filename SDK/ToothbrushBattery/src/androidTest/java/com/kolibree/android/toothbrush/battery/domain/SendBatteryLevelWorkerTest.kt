/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.domain

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.commons.models.StrippedMac
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.toothbrush.battery.data.BatteryLevelApi
import com.kolibree.android.toothbrush.battery.data.model.SendBatteryLevelRequest
import com.kolibree.android.toothbrush.battery.domain.SendBatteryLevelWorker.Companion.INPUT_JSON
import com.kolibree.android.worker.LazyWorkManagerImpl
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class SendBatteryLevelWorkerTest : BaseInstrumentationTest() {

    private val gson: Gson = GsonBuilder().create()
    private val batteryLevelApi: BatteryLevelApi = mock()

    private lateinit var factory: SendBatteryLevelWorker.Factory
    private lateinit var configurator: SendBatteryLevelWorker.Configurator

    override fun context(): Context {
        return ApplicationProvider.getApplicationContext()
    }

    override fun setUp() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context(), config)
        val lazyWorkManager = LazyWorkManagerImpl(ApplicationContext(context()))
        factory = SendBatteryLevelWorker.Factory(gson, batteryLevelApi)
        configurator = SendBatteryLevelWorker.Configurator(lazyWorkManager, gson)
    }

    @Test
    fun returnsFailureWhenInputIsMissing() {
        val worker = buildWorker()

        val observer = worker.createWork().test()
        observer.assertComplete()
        observer.assertValue(ListenableWorker.Result.failure())
        verifyZeroInteractions(batteryLevelApi)
    }

    @Test
    fun returnsFailureWhenInputIsInvalid() {
        val worker = buildWorker(inputJson = "{\"some\":  \"invalid json\"}")

        val observer = worker.createWork().test()
        observer.assertComplete()
        observer.assertValue(ListenableWorker.Result.failure())
        verifyZeroInteractions(batteryLevelApi)
    }

    @Test
    fun returnsFailureWhenRequestFails() {
        val input = SendBatteryLevelWorker.Input(
            accountId = 1,
            profileId = 2,
            request = SendBatteryLevelRequest(
                macAddress = StrippedMac.fromMac("mac"),
                serialNumber = "serial",
                discreteLevel = 0
            )
        )

        val worker = buildWorker(inputJson = gson.toJson(input))

        whenever(batteryLevelApi.sendBatteryLevel(any(), any(), any()))
            .thenReturn(Single.error(IllegalStateException("test")))

        val observer = worker.createWork().test()
        observer.assertComplete()
        observer.assertValue(ListenableWorker.Result.failure())

        verify(batteryLevelApi).sendBatteryLevel(
            accountId = input.accountId,
            profileId = input.profileId,
            body = input.request
        )
    }

    @Test
    fun returnsSuccessWhenRequestSucceeds() {
        val input = SendBatteryLevelWorker.Input(
            accountId = 1,
            profileId = 2,
            request = SendBatteryLevelRequest(
                macAddress = StrippedMac.fromMac("mac"),
                serialNumber = "serial",
                discreteLevel = 0
            )
        )

        val worker = buildWorker(inputJson = gson.toJson(input))

        whenever(batteryLevelApi.sendBatteryLevel(any(), any(), any()))
            .thenReturn(Single.just(Response.success(null)))

        val observer = worker.createWork().test()
        observer.assertComplete()
        observer.assertValue(ListenableWorker.Result.success())

        verify(batteryLevelApi).sendBatteryLevel(
            accountId = input.accountId,
            profileId = input.profileId,
            body = input.request
        )
    }

    @Test
    fun configuratorEnqueuesWorkWithCorrectName() {
        val testAccountId = 10L
        val testProfileId = 20L
        val testMac = StrippedMac.fromMac("mac")
        val testSerial = "serial"
        val testRequest = SendBatteryLevelRequest(
            macAddress = testMac,
            serialNumber = testSerial,
            discreteLevel = 0
        )

        configurator
            .sendBatteryLevel(testAccountId, testProfileId, testRequest)
            .test()

        val expectedWorkName = SendBatteryLevelWorker.createWorkName(
            accountId = testAccountId,
            profileId = testProfileId,
            macAddress = testMac,
            serialNumber = testSerial
        )

        val workInfos = WorkManager.getInstance(context())
            .getWorkInfosForUniqueWork(expectedWorkName)
            .get()

        assertEquals(1, workInfos.size)

        val workInfo = workInfos.first()
        assert(workInfo.state == WorkInfo.State.ENQUEUED)
    }

    private fun buildWorker(inputJson: String? = null): SendBatteryLevelWorker {
        val inputData = when {
            inputJson != null -> workDataOf(INPUT_JSON to inputJson)
            else -> Data.EMPTY
        }

        return TestListenableWorkerBuilder<SendBatteryLevelWorker>(context = context())
            .setWorkerFactory(factory)
            .setInputData(inputData)
            .build() as SendBatteryLevelWorker
    }
}
