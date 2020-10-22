/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.di

import android.app.Application
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.app.dagger.AppComponent
import com.kolibree.android.app.dagger.DaggerAppComponent
import com.kolibree.android.offlinebrushings.persistence.OrphanBrushingRepository
import com.kolibree.android.sdk.KolibreeAndroidSdk
import com.kolibree.android.sdk.dagger.SdkComponent
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.sdk.scan.ConnectionScannedTracker
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.charts.persistence.repo.StatRepository
import com.kolibree.statsoffline.models.api.AggregatedStatsRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import timber.log.Timber

class DaggerComponentTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val application: Application = context().applicationContext as Application

    private lateinit var sdkComponent: SdkComponent

    private lateinit var appComponent: AppComponent

    override fun setUp() {
        super.setUp()
        sdkComponent = KolibreeAndroidSdk.init(
            context = context()
        )
        appComponent = DaggerAppComponent.builder()
            .context(application)
            .sdkComponent(sdkComponent)
            .build()
    }

    @Test
    fun verifyBrushingsProcessorIsProvisioned() {
        assertNotNull(appComponent.brushingsProcessor())
    }

    @Test
    fun verifyAllExpectedBundleCreatorExist() {
        /*
        We can't access some of the classes, so I'm using names :-/
         */
        val expectedBundleCreatorNames =
            arrayOf(
                "LifetimeSmilesSynchronizableReadOnlyCreator",
                "ChallengesSynchronizableCatalogBundleCreator",
                "ChallengeProgressSynchronizableReadOnlyCreator",
                "TiersSynchronizableCatalogBundleCreator",
                "ProfileTierSynchronizableReadOnlyCreator",
                "ProfileSmilesSynchronizableReadOnlyCreator",
                "PrizesSynchronizableCatalogBundleCreator",
                "ProfileSmilesHistorySynchronizableReadOnlyCreator",
                "ProfilePersonalChallengeSynchronizableCreator",
                "InOffBrushingsCountBundleCreator",
                "BrushSyncReminderBundleCreator",
                "AccountBundleCreator",
                "AmazonDrsBundleCreator",
                "BrushHeadStatusSynchronizableCreator"
            )

        assertEquals(
            expectedBundleCreatorNames.size,
            appComponent.bundleCreators().size
        )

        expectedBundleCreatorNames.forEach { expectedClassName ->
            try {
                appComponent.bundleCreators().map { it.javaClass.simpleName }
                    .single { it == expectedClassName }
            } catch (e: NoSuchElementException) {
                Timber.e("Class $expectedClassName not present in ${appComponent.bundleCreators()}")
                throw e
            }
        }
    }

    @Test
    fun verifyAllExpectedTruncablesExist() {
        val expectedTruncables =
            arrayOf(
                ToothbrushRepository::class.java,
                StatRepository::class.java,
                AggregatedStatsRepository::class.java,
                OrphanBrushingRepository::class.java,
                ConnectionScannedTracker::class.java
            )

        /*
        We can't access some of the classes, so I'm using names :-/
         */
        val expectedTruncableNames =
            arrayOf(
                "CoachSettingsRepositoryImpl",
                "ApiSDKDatabaseInteractorImpl",
                "SynchronizableTrackingEntityDao_Impl",
                "PersonalChallengeV1RepositoryImpl",
                "IntegritySealDataStore",
                "GameProgressDao_Impl",
                "CartDao_Impl",
                "ProfileSmilesDao_Impl",
                "LifetimeSmilesDao_Impl",
                "BrushHeadReplaceDateManager",
                "ChallengeProgressDao_Impl",
                "ProfileTierDao_Impl",
                "FeedbackDao_Impl",
                "SmilesHistoryEventsDao_Impl",
                "RewardsSynchronizedVersions",
                "ShortTaskDao_Impl",
                "InOffBrushingsCountDao_Impl",
                "StatsSynchronizedVersions",
                "BrushSyncReminderDao_Impl",
                "AccountSynchronizedVersions",
                "AmazonDrsSynchronizedVersions",
                "BrushReminderDao_Impl",
                "HeadspacePartnershipDao_Impl"
            )

        assertEquals(
            appComponent.truncables().joinToString(separator = "\n") { it.javaClass.simpleName },
            expectedTruncables.size + expectedTruncableNames.size,
            appComponent.truncables().size
        )

        expectedTruncables.forEach { expectedClass ->
            try {
                appComponent.truncables().filterIsInstance(expectedClass).single()
            } catch (e: NoSuchElementException) {
                Timber.e("Class $expectedClass not present in ${appComponent.truncables()}")
                throw e
            }
        }

        expectedTruncableNames.forEach { expectedClassName ->
            try {
                appComponent.truncables().map { it.javaClass.simpleName }
                    .single { it == expectedClassName }
            } catch (e: NoSuchElementException) {
                Timber.e("Class $expectedClassName not present in ${appComponent.truncables()}")
                throw e
            }
        }
    }

    @Test
    fun verifyExpectedProfileDeletedHooks() {
        /*
        We can't access some of the classes, so I'm using names :-/
         */
        val expectedHooks =
            arrayOf(
                "ReassignSharedToothbrushHook",
                "ResetActiveProfileHook",
                "ForgetDeletedProfileToothbrushesHook"
            )

        val hooks = appComponent.profileDeletedHooks()

        expectedHooks.forEach { expectedClassName ->
            try {
                hooks.map { it.javaClass.simpleName }
                    .single { it == expectedClassName }
            } catch (e: NoSuchElementException) {
                Timber.e("Class $expectedClassName not present in $hooks")
                throw e
            }
        }
    }
}
