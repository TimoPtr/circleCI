package com.kolibree.sdkws.brushing

import com.google.common.base.Optional
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.android.commons.interfaces.RemoteBrushingsProcessor
import com.kolibree.android.network.toParsedResponseSingle
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.sdkws.api.ConnectivityApiManagerImpl
import com.kolibree.sdkws.brushing.models.BrushingApiModel
import com.kolibree.sdkws.brushing.models.BrushingsResponse
import com.kolibree.sdkws.brushing.models.CreateMultipleBrushingSessionsBody
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.DeleteBrushingData
import io.reactivex.Single
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

private const val ESTIMATED_BRUSHINGS_PER_DAY = 2
private const val MINIMAL_BRUSHINGS_LIMIT = 50

/**
 * Call the WS using retrofit if there is a connexion, otherwise throw an error
 */
internal class BrushingApiManagerImpl
constructor(
    private val brushingApi: BrushingApi,
    private val connectivityApiManagerImpl: ConnectivityApiManagerImpl,
    private val checkupCalculator: CheckupCalculator,
    private val remoteBrushingsProcessor: RemoteBrushingsProcessor?,
    private val scope: CoroutineScope
) : BrushingApiManager {

    @Inject
    constructor(
        brushingApi: BrushingApi,
        connectivityApiManagerImpl: ConnectivityApiManagerImpl,
        checkupCalculator: CheckupCalculator,
        remoteBrushingsProcessor: Optional<RemoteBrushingsProcessor>
    ) : this(
        brushingApi = brushingApi,
        connectivityApiManagerImpl = connectivityApiManagerImpl,
        checkupCalculator = checkupCalculator,
        remoteBrushingsProcessor = remoteBrushingsProcessor.orNull(),
        scope = GlobalScope
    )

    override fun deleteBrushing(
        accountId: Long,
        profileId: Long,
        brushingId: Long
    ): Single<Boolean> {
        return when {
            !connectivityApiManagerImpl.hasConnectivity() -> connectivityApiManagerImpl.syncWhenConnectivityAvailable()
            else -> brushingApi.deleteBrushing(
                accountId,
                profileId,
                DeleteBrushingData(listOf(brushingId))
            )
                .map { true }
                .doAfterSuccess { notifyBrushingsRemoved() }
        }
    }

    override fun deleteBrushings(
        accountId: Long,
        profileId: Long,
        brushings: List<Brushing>
    ): Single<Boolean> {
        return when {
            !connectivityApiManagerImpl.hasConnectivity() -> connectivityApiManagerImpl.syncWhenConnectivityAvailable()
            else -> brushingApi
                .deleteBrushing(
                    accountId, profileId,
                    DeleteBrushingData(brushings.mapNotNull { it.kolibreeId })
                )
                .map { true }
                .doAfterSuccess { notifyBrushingsRemoved() }
        }
    }

    override fun getLatestBrushings(
        accountId: Long,
        profileId: Long
    ): Single<BrushingsResponse> {
        val timeWindow = defaultBrushingTimeWindow()
        val limit = Math.max(
            ChronoUnit.DAYS.between(
                timeWindow.first,
                timeWindow.second
            ).toInt() * ESTIMATED_BRUSHINGS_PER_DAY,
            MINIMAL_BRUSHINGS_LIMIT
        )
        return when {
            !connectivityApiManagerImpl.hasConnectivity() -> connectivityApiManagerImpl.syncWhenConnectivityAvailable()
            else -> brushingApi.getBrushings(
                accountId,
                profileId,
                beginDate = timeWindow.first.format(DATE_FORMATTER),
                endDate = timeWindow.second.format(DATE_FORMATTER),
                limit = limit
            ).toParsedResponseSingle()
        }
    }

    override fun getBrushingsInDateRange(
        accountId: Long,
        profileId: Long,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        limit: Int?
    ): Single<BrushingsResponse> {
        return when {
            !connectivityApiManagerImpl.hasConnectivity() -> connectivityApiManagerImpl.syncWhenConnectivityAvailable()
            else -> brushingApi.getBrushings(
                accountId,
                profileId,
                beginDate = fromDate?.format(DATE_FORMATTER),
                endDate = toDate?.format(DATE_FORMATTER),
                limit = limit
            ).toParsedResponseSingle()
        }
    }

    override fun getBrushingsOlderThanBrushing(
        accountId: Long,
        profileId: Long,
        beforeBrushing: Brushing,
        limit: Int?
    ): Single<BrushingsResponse> {
        return when {
            !connectivityApiManagerImpl.hasConnectivity() -> connectivityApiManagerImpl.syncWhenConnectivityAvailable()
            else -> brushingApi.getBrushings(
                accountId,
                profileId,
                beforeBrushingId = beforeBrushing.kolibreeId,
                limit = limit
            ).toParsedResponseSingle()
        }
    }

    override fun createBrushings(
        accountId: Long,
        profileId: Long,
        brushings: List<BrushingInternal>
    ):
        Single<BrushingsResponse> {
        return when {
            !connectivityApiManagerImpl.hasConnectivity() -> connectivityApiManagerImpl.syncWhenConnectivityAvailable()
            else -> brushingApi.createBrushings(accountId, profileId,
                CreateMultipleBrushingSessionsBody(
                    brushings.map {
                        it.extractCreateBrushingData(checkupCalculator)
                    }
                )
            )
                .toParsedResponseSingle()
                .doAfterSuccess { notifyBrushingsCreated() }
        }
    }

    override fun createBrushing(accountId: Long, profileId: Long, brushing: BrushingInternal):
        Single<BrushingsResponse> {
        return createBrushings(
            accountId = accountId,
            profileId = profileId,
            brushings = listOf(brushing)
        )
    }

    override fun assignBrushings(
        accountId: Long,
        profileId: Long,
        brushings: List<Brushing>
    ): Single<Boolean> {
        return when {
            !connectivityApiManagerImpl.hasConnectivity() -> connectivityApiManagerImpl.syncWhenConnectivityAvailable()
            else -> brushingApi.assignBrushings(accountId, profileId, brushings.map
            {
                BrushingApiModel(
                    it.goalDuration,
                    it.kolibreeId,
                    it.processedData
                )
            })
                .map { true }
                .doAfterSuccess { notifyBrushingsCreated() }
        }
    }

    private fun notifyBrushingsRemoved() {
        remoteBrushingsProcessor?.let { processor -> scope.launch { processor.onBrushingsRemoved() } }
    }

    private fun notifyBrushingsCreated() {
        remoteBrushingsProcessor?.let { processor -> scope.launch { processor.onBrushingsCreated() } }
    }

    private fun defaultBrushingTimeWindow(): Pair<LocalDate, LocalDate> = Pair(
        TrustedClock.getNowLocalDate().minusMonths(2).withDayOfMonth(1),
        TrustedClock.getNowLocalDate()
    )
}
