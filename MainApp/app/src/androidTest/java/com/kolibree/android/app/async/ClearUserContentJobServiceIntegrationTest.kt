/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.android.app.async

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.clock.TrustedClock.getNowOffsetDateTime
import com.kolibree.android.offlinebrushings.OrphanBrushing
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.test.BaseJobServiceTest
import com.kolibree.android.test.dagger.EspressoStatsOfflineModule
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.createWithDefaultState
import com.kolibree.android.test.utils.SdkBuilder.Companion.create
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test

/** Created by miguelaragues on 14/3/18.  */
class ClearUserContentJobServiceIntegrationTest : BaseJobServiceTest() {
    private var insertedOrphanBrushingId: Long = 0

    @Test
    @Throws(InterruptedException::class)
    fun testClearUserContentJobService() {
        EspressoStatsOfflineModule.useMockRepository = true
        val connection: KLTBConnection = createWithDefaultState().build()
        create().withKLTBConnections(connection).build()

        prepareDatabaseContent()
        preparePreferencesContent()

        verify(component().kolibreeService(), never()).forget(connection)
        verify(component().avroFileUploader(), never()).deletePendingFiles()

        extraSetupBeforeLaunchingJob()

        whenever(component().aggregatedStatsRepository().truncate())
            .thenReturn(Completable.complete())

        launchJobAndWait(AppClearUserContentJobService.createBuilder(context()).build())

        verifyDatabasesAreTruncated()
        verifyPreferencesAreClearedButSecretsAreKept()
        verify(component().kolibreeService()).forget(connection)
        verify(component().avroFileUploader()).deletePendingFiles()

        extraVerifications()
    }

    private fun preparePreferencesContent() {
        val value = "should not be here after test"
        sharedPreferences().edit()
            .putString(TEST_KEY, value).apply()
        Assert.assertTrue(testPreferencesContainsTestKey(sharedPreferences()))
        Assert.assertEquals(
            value,
            sharedPreferences().getString(
                TEST_KEY,
                ""
            )
        )
        // create secret preferences that should survive a logout
        secretPreferences().edit()
            .putString(TEST_KEY, value).apply()
        Assert.assertTrue(testPreferencesContainsTestKey(secretPreferences()))
    }

    private fun secretPreferences(): SharedPreferences {
        return context().getSharedPreferences("secret_test", Context.MODE_PRIVATE)
    }

    private fun sharedPreferences(): SharedPreferences {
        return context().getSharedPreferences(
            TEST_PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    private fun testPreferencesContainsTestKey(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.contains(TEST_KEY)
    }

    /*
  UTILS
   */
    private fun prepareDatabaseContent() {
        insertedOrphanBrushingId = component()
            .orphanBrushingRepository()
            .insert(OrphanBrushing.create(0, 0, "", getNowOffsetDateTime(), "", ""))
        readInsertedOrphanBrushingObservable().test().assertValueCount(1)
    }

    private fun readInsertedOrphanBrushingObservable(): Single<OrphanBrushing> {
        return component().orphanBrushingRepository().read(insertedOrphanBrushingId)
    }

    private fun verifyDatabasesAreTruncated() {
        component().toothbrushRepository().listAll().test()
            .assertValue { obj: List<AccountToothbrush?> -> obj.isEmpty() }
        verify(component().aggregatedStatsRepository())
            .truncate()
        readInsertedOrphanBrushingObservable().test().assertValueCount(0)
    }

    private fun verifyPreferencesAreClearedButSecretsAreKept() {
        Assert.assertFalse(testPreferencesContainsTestKey(sharedPreferences()))
        Assert.assertTrue(testPreferencesContainsTestKey(secretPreferences()))
    }

    private fun extraSetupBeforeLaunchingJob() {
        verify(
            component().kolibreeProReminders(),
            never()
        ).stopAllPeriodicReminders()
    }

    private fun extraVerifications() {
        verify(component().kolibreeProReminders())
            .stopAllPeriodicReminders()
    }

    companion object {
        private const val TEST_PREFS_NAME = "test_prefs_name"
        private const val TEST_KEY = "test_prefs_key"
    }
}
