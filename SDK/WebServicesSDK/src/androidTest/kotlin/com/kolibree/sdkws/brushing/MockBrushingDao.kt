package com.kolibree.sdkws.brushing

import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import org.threeten.bp.OffsetDateTime

open class MockBrushingDao {

    internal val brushings = ArrayList<BrushingInternal>()

    /**
     * Count number of brushing inserted in the DB and not deleted locally for a given profileId
     */
    internal fun getBrushings(profileId: Long) =
        brushings.filter { brushing ->
            brushing.profileId == profileId && !brushing.isDeletedLocally
        }

    // take last brushing session
    internal fun getLastBrushingSession(profileId: Long) =
        brushings.filter { it.profileId == profileId }
            .sortedByDescending { it.dateTime }
            .map { it.extractBrushing() }
            .first()

    /**
     * Count number of brushing inserted in the DB and not deleted locally for a given game
     */
    internal fun getBrushingsByGame(game: String) =
        brushings.filter { brushing ->
            brushing.game == game && !brushing.isDeletedLocally
        }

    /**
     * Count number of brushing inserted in the DB and not deleted locally for a given game
     */
    internal fun getBrushingsByGame(game: String, profileId: Long) =
        brushings.filter { brushing ->
            brushing.profileId == profileId && brushing.game == game && !brushing.isDeletedLocally
        }

    /**
     * Count number of brushing inserted in the DB for a given profileId between 2 dates
     */
    internal fun getBrushingBetween(begin: OffsetDateTime, end: OffsetDateTime, profileId: Long) =
        brushings.filter { brushings ->
            brushings.profileId == profileId &&
                brushings.dateTime.isAfter(begin) &&
                brushings.dateTime.isBefore(end)
        }

    /**
     * Get  non  sync brushing inserted in the DB for a given profileId
     */
    internal fun getNonSynchronizedBrushing(profileId: Long) =
        brushings.filter { brushing ->
            brushing.profileId == profileId &&
                !brushing.isSynchronized
        }

    /**
     * Get brushings inserted in the DB for a given profileId between 2 dates
     */
    internal fun getBrushingsSince(startingTimestamp: OffsetDateTime, profileId: Long) =
        brushings.filter { brushings ->
            brushings.profileId == profileId &&
                brushings.dateTime.isAfter(startingTimestamp)
        }
            .map { it.extractBrushing() }

    internal fun clearNonSynchronized(profileId: Long) {
        brushings.filter {
            !it.isSynchronized &&
                !it.isDeletedLocally &&
                it.profileId == profileId
        }.map { brushings.remove(it) }
    }

    internal fun deleteBrushing(brushing: BrushingInternal) {
        brushings.remove(brushing)
    }
}
