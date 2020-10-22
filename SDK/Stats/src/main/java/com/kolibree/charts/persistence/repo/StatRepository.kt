package com.kolibree.charts.persistence.repo

import androidx.annotation.Keep
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.charts.models.Stat
import io.reactivex.Flowable
import org.threeten.bp.ZonedDateTime

/**
 * Created by guillaume agis on 18/5/18.
 * Interface for the Stat repository. Get all the stats for a given user from a date, for a week
 */
@Keep
interface StatRepository : Truncable {

    /**
     * Get the list of stats from a given date for a given profileId
     *
     * @param startTime the date to get the stat from , a ZonedDateTime object
     * @param profileId the id of the profile to update the stat
     * @return a Flowable with a list of Stat object
     */
    fun getStatsSince(startTime: ZonedDateTime, profileId: Long): Flowable<List<Stat>>
}
