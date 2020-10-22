/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.controller

import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import com.kolibree.kml.MouthZone16
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import org.threeten.bp.Duration

/**
 * Base [CoachPlusController] implementation.
 *
 * Contains the brushing data logic, the zone sequence and associated methods.
 */
internal abstract class BaseCoachPlusControllerImpl(
    private val goalBrushingDuration: Duration
) : CoachPlusController {

    @VisibleForTesting
    var currentZoneIndex: Int = 0

    private val zoneChangePublishSubject: Subject<SupervisionInfo> =
        BehaviorSubject.createDefault(getCurrentSupervisionInfo())

    private val goalBrushingTimePerZone: Long // Millis

    init {
        goalBrushingTimePerZone = goalBrushingDuration.toMillis() /
            SEQUENCE.size
    }

    final override val zoneChangeObservable: Observable<SupervisionInfo> =
        zoneChangePublishSubject.hide()

    final override fun getSequenceLength(): Int = SEQUENCE.size

    final override fun getCurrentZone(): MouthZone16 = SEQUENCE[currentZoneIndex]

    @CallSuper
    override fun reset() {
        currentZoneIndex = 0
    }

    protected fun getGoalBrushingTimePerZone(): Long = goalBrushingTimePerZone

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun getGoalBrushingDuration(): Duration = goalBrushingDuration

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun hasMoreZones(): Boolean = currentZoneIndex < SEQUENCE.size - 1

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun brushNextZone() {
        currentZoneIndex++
        zoneChangePublishSubject.onNext(getCurrentSupervisionInfo())
    }

    @VisibleForTesting
    fun getCurrentSupervisionInfo() = SupervisionInfo(SEQUENCE[currentZoneIndex], KOLIBREE_LEGACY_SEQUENCE_ID)

    companion object {

        val SEQUENCE = arrayOf(
            MouthZone16.UpMolLeExt,
            MouthZone16.LoMolLeExt,
            MouthZone16.UpMolRiExt,
            MouthZone16.LoMolRiExt,
            MouthZone16.UpIncExt,
            MouthZone16.LoIncExt,
            MouthZone16.UpMolLeOcc,
            MouthZone16.UpMolLeInt,
            MouthZone16.LoMolLeInt,
            MouthZone16.LoMolLeOcc,
            MouthZone16.UpMolRiOcc,
            MouthZone16.UpMolRiInt,
            MouthZone16.LoMolRiInt,
            MouthZone16.LoMolRiOcc,
            MouthZone16.UpIncInt,
            MouthZone16.LoIncInt
        )

        @VisibleForTesting
            /* https://kolibree.atlassian.net/wiki/spaces/PROD/pages/2735966/Considering+jaw+moves+in+Coach+ */
        val KOLIBREE_LEGACY_SEQUENCE_ID: Byte = 0

        @VisibleForTesting
        const val DEFAULT_MAX_FAIL_TIME_MS = 5000L

        @VisibleForTesting
        const val TOLERANCE_TIME_MS = 1000L
    }
}
