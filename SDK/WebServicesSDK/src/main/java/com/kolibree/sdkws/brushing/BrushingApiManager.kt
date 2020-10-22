package com.kolibree.sdkws.brushing

import com.kolibree.sdkws.brushing.models.BrushingsResponse
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.data.model.Brushing
import io.reactivex.Single
import org.threeten.bp.LocalDate

internal interface BrushingApiManager {
    fun deleteBrushing(accountId: Long, profileId: Long, brushingId: Long): Single<Boolean>

    fun deleteBrushings(
        accountId: Long,
        profileId: Long,
        brushings: List<Brushing>
    ): Single<Boolean>

    fun getLatestBrushings(
        accountId: Long,
        profileId: Long
    ): Single<BrushingsResponse>

    fun getBrushingsInDateRange(
        accountId: Long,
        profileId: Long,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        limit: Int? = null
    ): Single<BrushingsResponse>

    fun getBrushingsOlderThanBrushing(
        accountId: Long,
        profileId: Long,
        beforeBrushing: Brushing,
        limit: Int? = null
    ): Single<BrushingsResponse>

    fun createBrushings(
        accountId: Long,
        profileId: Long,
        brushings: List<BrushingInternal>
    ): Single<BrushingsResponse>

    fun assignBrushings(
        accountId: Long,
        profileId: Long,
        brushings: List<Brushing>
    ): Single<Boolean>

    fun createBrushing(
        accountId: Long,
        profileId: Long,
        brushing: BrushingInternal
    ): Single<BrushingsResponse>
}
