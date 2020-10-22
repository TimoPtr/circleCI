package com.kolibree.android.pirate.tuto.persistence.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tutorial")
internal data class Tutorial(
    @ColumnInfo(name = FIELD_PROFILE_ID) @PrimaryKey val profileId: Long,
    @ColumnInfo(name = FIELD_HAS_SEEN_PIRATE_TUTO) val hasSeenPirateTuto: Boolean = false,
    @ColumnInfo(name = FIELD_HAS_SEEN_PIRATE_TRAILER) val hasSeenPirateTrailer: Boolean = false,
    @ColumnInfo(name = FIELD_HAS_SEEN_PIRATE_COMPLETE_TRAILER) val hasSeenPirateCompleteTrailer: Boolean = false,
    @ColumnInfo(name = FIELD_HAS_SEEN_BREE_FIRST_MESSAGE) val hasSeenBreeFirstMessage: Boolean = false,
    @ColumnInfo(name = FIELD_GOT_A_BADGE_WITH_LAST_BRUSHING) val gotABadgeWithLastBrushing: Boolean = false
) {

    // Get the value of the boolean given by it's name (string format)
    fun getValue(which: String): Boolean {
        return when (which) {
            FIELD_HAS_SEEN_PIRATE_TUTO -> hasSeenPirateTuto
            FIELD_HAS_SEEN_PIRATE_TRAILER -> hasSeenPirateTrailer
            FIELD_HAS_SEEN_PIRATE_COMPLETE_TRAILER -> hasSeenPirateCompleteTrailer
            FIELD_HAS_SEEN_BREE_FIRST_MESSAGE -> hasSeenBreeFirstMessage
            FIELD_GOT_A_BADGE_WITH_LAST_BRUSHING -> gotABadgeWithLastBrushing
            else -> false
        }
    }

    // Create a new object giving the current one and the new boolean that needs to be updated
    fun seen(which: String): Tutorial {
        return when (which) {
            FIELD_HAS_SEEN_PIRATE_TUTO -> this.copy(hasSeenPirateTuto = true)
            FIELD_HAS_SEEN_PIRATE_TRAILER -> this.copy(hasSeenPirateTrailer = true)
            FIELD_HAS_SEEN_PIRATE_COMPLETE_TRAILER -> this.copy(hasSeenPirateCompleteTrailer = true)
            FIELD_HAS_SEEN_BREE_FIRST_MESSAGE -> this.copy(hasSeenBreeFirstMessage = true)
            FIELD_GOT_A_BADGE_WITH_LAST_BRUSHING -> this.copy(gotABadgeWithLastBrushing = true)
            else -> this
        }
    }

    companion object {

        // create a new Tutorial Object
        fun create(profileId: Long, which: String): Tutorial {
            return when (which) {
                FIELD_HAS_SEEN_PIRATE_TUTO -> Tutorial(
                    profileId = profileId,
                    hasSeenPirateTuto = true
                )
                FIELD_HAS_SEEN_PIRATE_TRAILER -> Tutorial(
                    profileId = profileId,
                    hasSeenPirateTrailer = true
                )
                FIELD_HAS_SEEN_PIRATE_COMPLETE_TRAILER -> Tutorial(
                    profileId = profileId,
                    hasSeenPirateCompleteTrailer = true
                )
                FIELD_HAS_SEEN_BREE_FIRST_MESSAGE -> Tutorial(
                    profileId = profileId,
                    hasSeenBreeFirstMessage = true
                )
                FIELD_GOT_A_BADGE_WITH_LAST_BRUSHING -> Tutorial(
                    profileId = profileId,
                    gotABadgeWithLastBrushing = true
                )
                else -> Tutorial(profileId = profileId)
            }
        }

        const val FIELD_PROFILE_ID = "profileid"
        const val FIELD_HAS_SEEN_PIRATE_TUTO = "hasseenpiratetuto"
        const val FIELD_HAS_SEEN_PIRATE_TRAILER = "hasseenpiratetrailer"
        const val FIELD_HAS_SEEN_PIRATE_COMPLETE_TRAILER = "hasseenpiratecompletetrailer"
        const val FIELD_HAS_SEEN_BREE_FIRST_MESSAGE = "hasseenbreefirstmessage"
        const val FIELD_GOT_A_BADGE_WITH_LAST_BRUSHING = "gotabadgewithlastbrushing"
    }
}
