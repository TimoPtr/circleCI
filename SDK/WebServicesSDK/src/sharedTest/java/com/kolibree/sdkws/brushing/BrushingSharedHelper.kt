/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.sdkws.brushing

import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.GameApiConstants.GAME_COACH
import com.kolibree.sdkws.brushing.models.BrushingResponse
import com.kolibree.sdkws.brushing.models.BrushingsResponse
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.CreateBrushingData
import java.util.UUID
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit

internal fun createBrushingInternal(
    profileId: Long = PROFILE_ID_USER1,
    minusDay: Long = 0L,
    game: String = GAME_COACH,
    kolibreeId: Long = 0,
    isDeletedLocally: Boolean = false,
    idempotencyKey: UUID = UUID.randomUUID()
) = BrushingInternal(
    profileId = profileId,
    duration = DURATION,
    game = game,
    kolibreeId = kolibreeId,
    points = POINTS,
    isDeletedLocally = isDeletedLocally,
    datetime = currentTime().minusDays(minusDay).truncatedTo(ChronoUnit.SECONDS),
    goalDuration = GOAL_DURATION,
    idempotencyKey = idempotencyKey,
    isSynchronized = IS_SYNC
)

internal fun createBrushing(
    profileId: Long = PROFILE_ID_USER1,
    minusDay: Long = 0L,
    game: String = "",
    processedData: String = "",
    kolibreeId: Long = KOLIBREE_ID
) = Brushing(
    profileId = profileId,
    duration = DURATION,
    goalDuration = GOAL_DURATION,
    game = game,
    kolibreeId = kolibreeId,
    coins = COINS,
    points = POINTS,
    dateTime = currentTime().minusDays(minusDay).truncatedTo(ChronoUnit.SECONDS),
    processedData = processedData
)

internal fun currentTime(): OffsetDateTime {
    return TrustedClock.getNowOffsetDateTime().truncatedTo(ChronoUnit.SECONDS)
}

internal fun generateBrushingResponse(
    profileId: Long = PROFILE_ID_USER1,
    brushings: List<BrushingResponse> = arrayListOf(createBrushingResponseItem(profileId))
) = BrushingsResponse(
    brushings = brushings,
    totalCoins = 100
)

internal fun generateBrushingResponseBetweenDates(
    startDate: LocalDate,
    endDate: LocalDate,
    profileId: Long = PROFILE_ID_USER1
) = BrushingsResponse(
    brushings = createBrushingResponseItems(profileId, startDate, endDate),
    totalCoins = 100
)

internal fun createBrushingResponseItem(profileId: Long = PROFILE_ID_USER1) = BrushingResponse(
    kolibreeId = NEW_KOLIBREE_ID,
    coins = 101,
    game = GAME2,
    duration = 10001,
    datetime = TrustedClock.getNowOffsetDateTime(),
    goalDuration = GOAL_DURATION,
    profileId = profileId,
    idempotencyKey = UUID.randomUUID().toString()
)

internal fun createBrushingResponseItems(
    profileId: Long = PROFILE_ID_USER1,
    startDate: LocalDate,
    endDate: LocalDate
): List<BrushingResponse> = 0.rangeTo(ChronoUnit.DAYS.between(startDate, endDate))
    .toList()
    .map {
        BrushingResponse(
            kolibreeId = NEW_KOLIBREE_ID,
            coins = 101,
            game = GAME2,
            duration = 10001,
            datetime = startDate.plusDays(it).atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime(),
            goalDuration = GOAL_DURATION,
            profileId = profileId,
            idempotencyKey = UUID.randomUUID().toString()
        )
    }

fun createBrushingData(): CreateBrushingData {
    return CreateBrushingData(
        GAME1,
        DURATION,
        GOAL_DURATION,
        TrustedClock.getNowOffsetDateTime(),
        COINS
    )
}

@Suppress("MagicNumber")
fun createProfile() = ProfileInternal(
    points = 10,
    id = PROFILE_ID_USER1,
    gender = "m",
    firstName = "my_user",
    accountId = DEFAULT_ACCOUNT_ID.toInt(),
    isOwnerProfile = true,
    creationDate = "1990-05-05T12:00:00+0000",
    brushingTime = 120,
    birthday = LocalDate.of(1999, 5, 4)
)

internal const val DEFAULT_ACCOUNT_ID = 667L
internal const val PROFILE_ID_NO_DATA = 10L
internal const val PROFILE_ID_USER1 = 53L
internal const val PROFILE_ID_USER2 = 101L
internal const val DURATION = 200L
internal const val GAME1 = "game1"
internal const val GAME2 = "game2"
internal const val NEW_GAME = "new_game"
internal const val POINTS = 42
internal const val COINS = 142
internal const val GOAL_DURATION = 120
internal const val IS_SYNC = true
internal const val TOOTHBRUSH_MAC = "AA:BB:CC:DD:EE"
internal const val KOLIBREE_ID = 0L

const val NEW_KOLIBREE_ID = 52L
const val NEW_POINTS = 1001
