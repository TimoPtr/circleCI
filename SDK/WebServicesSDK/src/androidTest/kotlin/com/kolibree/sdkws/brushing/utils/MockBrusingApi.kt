package com.kolibree.sdkws.brushing.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.sdkws.brushing.BrushingApi
import com.kolibree.sdkws.brushing.COINS
import com.kolibree.sdkws.brushing.DURATION
import com.kolibree.sdkws.brushing.GOAL_DURATION
import com.kolibree.sdkws.brushing.models.BrushingApiModel
import com.kolibree.sdkws.brushing.models.BrushingResponse
import com.kolibree.sdkws.brushing.models.BrushingsResponse
import com.kolibree.sdkws.brushing.models.CreateMultipleBrushingSessionsBody
import com.kolibree.sdkws.data.model.DeleteBrushingData
import io.reactivex.Single
import java.util.UUID
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate

internal class MockBrusingApi(
    private val delegate: BehaviorDelegate<BrushingApi>,
    private val brushings: ArrayList<BrushingResponse>
) : BrushingApi {

    override fun deleteBrushing(
        accountId: Long,
        profileId: Long,
        body: DeleteBrushingData
    ): Single<Response<Void>> {

        brushings.filter { brushing ->
            brushing.profileId == profileId && body.brushings.contains(
                brushing.kolibreeId
            )
        }
            .map { brushings.remove(it) }
        return delegate.returningResponse(Response.success(Any()))
            .deleteBrushing(accountId, profileId, body)
    }

    override fun getBrushings(
        accountId: Long,
        profileId: Long,
        pd: Boolean,
        beginDate: String?,
        endDate: String?,
        beforeBrushingId: Long?,
        afterBrushingId: Long?,
        limit: Int?
    ): Single<Response<BrushingsResponse>> {
        val beginLocalDate = beginDate?.let { LocalDate.from(DATE_FORMATTER.parse(it)) }
        val endLocalDate = endDate?.let { LocalDate.from(DATE_FORMATTER.parse(it)) }
        return delegate.returningResponse(
            BrushingsResponse(
                brushings = brushings
                    .asSequence()
                    .filter { it.profileId == profileId }
                    .filter {
                        endLocalDate == null ||
                            it.datetime.toLocalDate().isEqual(endLocalDate) ||
                            it.datetime.toLocalDate().isBefore(endLocalDate)
                    }
                    .filter {
                        beginDate == null ||
                            it.datetime.toLocalDate().isEqual(beginLocalDate) ||
                            it.datetime.toLocalDate().isAfter(beginLocalDate)
                    }
                    .filter {
                        beforeBrushingId == null || it.kolibreeId < beforeBrushingId
                    }
                    .filter {
                        afterBrushingId == null || it.kolibreeId > afterBrushingId
                    }
                    .take(limit ?: 100)
                    .toList(),
                totalCoins = 100
            )
        ).getBrushings(
            accountId,
            profileId,
            pd,
            beginDate,
            endDate,
            beforeBrushingId,
            afterBrushingId,
            limit
        )
    }

    override fun createBrushings(
        accountId: Long,
        profileId: Long,
        body: CreateMultipleBrushingSessionsBody
    ): Single<Response<BrushingsResponse>> {
        body.brushingsToCreate
            .map { brushingsToCreate ->
                this.brushings.add(
                    createBrushingResponse(
                        profileId = profileId,
                        game = brushingsToCreate.game,
                        kolibreeId = TrustedClock.getNowInstant().toEpochMilli(),
                        duration = brushingsToCreate.duration,
                        coins = brushingsToCreate.coins,
                        goalDuration = brushingsToCreate.goalDuration,
                        dateTime = OffsetDateTime.parse(
                            brushingsToCreate.dateTime,
                            DATETIME_FORMATTER
                        )
                    )
                )
            }
        return delegate.returningResponse(
            BrushingsResponse(
                brushings = brushings.filter { it.profileId == profileId },
                totalCoins = 100
            )
        ).createBrushings(accountId, profileId, body)
    }

    override fun assignBrushings(
        accountId: Long,
        profileId: Long,
        brushingsKolibreeIds: List<BrushingApiModel>
    ): Single<Response<Void>> {

        val brushingForUser = brushings.filter { it.profileId == profileId }

        val parser = JsonParser()
        brushingsKolibreeIds.map { obj ->
            brushingForUser
                .filter { brushing -> brushing.kolibreeId == obj.id }
                .forEach {
                    val newBrushing = it.copy(
                        goalDuration = obj.goalDuration,
                        processedData = parser.parse(obj.processedData).asJsonObject
                    )
                    brushings.remove(it)
                    brushings.add(newBrushing)
                }
        }
        return delegate.returningResponse(Response.success(Any()))
            .assignBrushings(accountId, profileId, brushingsKolibreeIds)
    }

    companion object {

        fun createBrushingResponse(
            profileId: Long,
            minusDay: Long = 0,
            game: String = "",
            kolibreeId: Long = 0,
            duration: Long = DURATION,
            coins: Int = COINS,
            goalDuration: Int = GOAL_DURATION,
            dateTime: OffsetDateTime = TrustedClock.getNowOffsetDateTime(),
            idempotencyKey: UUID = UUID.randomUUID()
        ) = BrushingResponse(
            profileId = profileId,
            duration = duration,
            game = game,
            datetime = dateTime.minusDays(minusDay),
            kolibreeId = kolibreeId,
            processedData = JsonObject(),
            coins = coins,
            idempotencyKey = idempotencyKey.toString(),
            goalDuration = goalDuration
        )
    }
}
