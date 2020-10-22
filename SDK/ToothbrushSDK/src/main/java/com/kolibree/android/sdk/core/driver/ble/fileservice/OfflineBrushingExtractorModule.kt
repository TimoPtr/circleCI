/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble.fileservice

import com.kolibree.android.processedbrushings.ProcessedBrushingsModule
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushingsExtractor
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.di.KLTBConnectionScope
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceInteractorImpl
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileServiceOfflineBrushingsExtractor
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileSessionImpl
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.legacy.LegacyStoredBrushingsExtractor
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.KpiSpeedProviderModule
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.android.sdk.util.RnnWeightProviderModule
import com.kolibree.kml.StoredBrushingProcessor
import dagger.Module
import dagger.Provides
import javax.inject.Provider

@Module(
    includes = [
        RnnWeightProviderModule::class,
        KpiSpeedProviderModule::class,
        ProcessedBrushingsModule::class]
)
internal object OfflineBrushingExtractorModule {
    @Provides
    @KLTBConnectionScope
    fun providesOfflineBrushingExtractor(
        klNordicBleManager: KLNordicBleManager,
        fileSessionProvider: Provider<FileSessionImpl>
    ): OfflineBrushingsExtractor {
        if (klNordicBleManager.isFileServiceImplemented()) {
            return FileServiceOfflineBrushingsExtractor(
                FileServiceInteractorImpl(
                    klNordicBleManager,
                    fileSessionProvider
                )
            )
        }

        return LegacyStoredBrushingsExtractor(
            klNordicBleManager
        )
    }

    /*
    Do NOT add scope here
     */
    @Provides
    fun providesStoredBrushingProcessor(
        rnnWeightProvider: RnnWeightProvider?,
        kpiSpeedProvider: KpiSpeedProvider?,
        angleProvider: AngleProvider,
        transitionProvider: TransitionProvider,
        thresholdProvider: ThresholdProvider,
        zoneValidatorProvider: ZoneValidatorProvider
    ): StoredBrushingProcessor {

        checkNotNull(rnnWeightProvider) { "WeightProvider should not be null did you provide the TB model" }

        checkNotNull(kpiSpeedProvider) { "KpiSpeedProvider should not be null did you provide the TB model" }

        return StoredBrushingProcessor(
            rnnWeightProvider.getRnnWeight(),
            angleProvider.getKPIAngle(),
            kpiSpeedProvider.getKpiSpeed(),
            transitionProvider.getTransition(),
            thresholdProvider.getThresholdBalancing(),
            zoneValidatorProvider.getZoneValidator()
        )
    }
}
