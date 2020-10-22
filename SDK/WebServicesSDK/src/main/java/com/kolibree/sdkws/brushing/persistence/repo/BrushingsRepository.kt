package com.kolibree.sdkws.brushing.persistence.repo

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.CreateBrushingData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime

@Keep
interface BrushingsRepository {

    /**
     * Creates a Brushing locally and attempts to upload it to the backend
     *
     * <p>If the remote creation is not supported at the current instant, it stores the brushing for future upload
     *
     * <p>It'll emit an error if
     * <ul>
     * <li>Local persistence fails
     * <li>The backend returns a BAD_REQUEST error, which means that our request will never succeed
     * </ul>
     *
     * @return Single that returns the created brushing
     */
    fun addBrushing(
        brushingData: CreateBrushingData,
        profile: ProfileInternal,
        accountId: Long
    ): Single<Brushing>

    fun assignBrushings(
        brushings: List<Brushing>,
        profileInternal: ProfileInternal
    ): Single<Boolean>

    fun synchronizeBrushing(accountId: Long, profileId: Long): Single<Boolean>

    fun fetchRemoteBrushings(
        accountId: Long,
        profileId: Long,
        fromDate: LocalDate? = null,
        toDate: LocalDate? = null,
        limit: Int? = null
    ): Single<Unit>

    fun fetchRemoteBrushings(
        accountId: Long,
        profileId: Long,
        beforeBrushing: Brushing,
        limit: Int
    ): Single<Unit>

    fun getBrushings(profileId: Long): Single<List<Brushing>>
    fun brushingsFlowable(profileId: Long): Flowable<List<Brushing>>
    fun countBrushingsSince(startTime: OffsetDateTime, profileId: Long): Single<Long>
    fun countBrushings(gameId: String, profileId: Long): Single<Long>
    fun countBrushings(profileId: Long): Single<Long>
    fun getBrushingsSince(startTime: OffsetDateTime, profileId: Long): Single<List<Brushing>>
    fun getBrushingsBetween(
        begin: OffsetDateTime,
        end: OffsetDateTime,
        profileId: Long
    ): Single<List<Brushing>>

    fun getFirstBrushingSession(profileId: Long): Brushing?
    fun getFirstBrushingSessionFlowable(profileId: Long): Flowable<Brushing>
    fun getLastBrushingSession(profileId: Long): Brushing?
    fun getLastBrushingSessionFlowable(profileId: Long): Flowable<Brushing>
    fun deleteAll(): Completable
    fun getNonDeletedBrushings(): Flowable<List<Brushing>>
    fun deleteBrushing(accountId: Long, profileId: Long, brushing: Brushing): Completable
}
