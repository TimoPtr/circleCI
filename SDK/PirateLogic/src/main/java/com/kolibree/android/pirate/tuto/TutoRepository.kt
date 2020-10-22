package com.kolibree.android.pirate.tuto

import androidx.annotation.Keep
import com.kolibree.android.commons.interfaces.Truncable

@Keep
interface TutoRepository : Truncable {
    fun hasSeenPirateTuto(profileId: Long): Boolean
    fun setHasSeenPirateTuto(profileId: Long)
    fun hasSeenPirateTrailer(profileId: Long): Boolean
    fun setHasSeenPirateTrailer(profileId: Long)
    fun hasSeenPirateCompleteTrailer(profileId: Long): Boolean
    fun setHasSeenPirateCompleteTrailer(profileId: Long)
    fun hasSeenBreeFirstMessage(profileId: Long): Boolean
    fun setHasSeenBreeFirstMessage(profileId: Long)
    fun gotABadgeWithLastBrushing(profileId: Long): Boolean
    fun setGotABadgeWithLastBrushing(profileId: Long, gotABadge: Boolean)
}
