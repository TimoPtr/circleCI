package com.kolibree.android.sdk.core.driver.ble.offlinebrushings

import com.kolibree.android.sdk.core.driver.ble.fileservice.OfflineBrushingExtractorModule
import io.reactivex.Completable
import io.reactivex.Single

/*
TODO this can't be shared between Legacy and new FileService?

The new one will feed bytes to KML and return a ProcessedBrushing16, which we can wrap in an
OfflineBrushing interface

Can we do the same for RecordedSession? Right now it depends on LegacyProcessedDataGenerator to
generate the json, but maybe we can move that class to ToothbrushSDK
 */
/**
 * Interface to extract offline brushings from a toothbrush
 *
 * Clients are responsible for closing extraction session. Failing to close the session will result
 * in unexpected behavior
 *
 * There's a single instance per toothbrush connection. See [OfflineBrushingExtractorModule]
 */
internal interface OfflineBrushingsExtractor {
    /**
     * Pop the current record without deleting it
     *
     * Post condition: finishExtractFileSession should be called when extraction session is
     * completed. Please wrap in a finally block
     */
    fun popRecord(): Single<OfflineBrushing>

    /**
     * Delete the current record
     *
     * Post condition: finishExtractFileSession should be called when extraction session is
     * completed. Please wrap in a finally block
     */
    fun deleteRecord(): Completable

    /**
     * Get the number of brushing sessions in the toothbrush memory
     *
     * Post condition: finishExtractFileSession should be called when extraction session is
     * completed. Please wrap in a finally block
     */
    fun recordCount(): Single<Int>

    fun startExtractFileSession(): Completable

    fun finishExtractFileSession(): Completable
}
