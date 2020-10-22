package com.kolibree.charts.persistence.repo

import com.jakewharton.rx.ReplayingShare
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.charts.models.Stat
import com.kolibree.charts.persistence.dao.StatDao
import com.kolibree.charts.persistence.models.StatInternal
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.data.model.Brushing
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.Clock
import org.threeten.bp.ZonedDateTime

/**
 * Manage the stat data into and store them locally into Room
 * Every time some data into the BrushingsRepository is updated, it will emit the data
 * in this class and update the stat data stored locally accordingly
 */
internal class StatRepositoryImpl
@Inject internal constructor(
    private val statDao: StatDao,
    private val clock: Clock,
    private val brushingRepository: BrushingsRepository,
    private val checkupCalculator: CheckupCalculator
) : StatRepository {

    /**
     * Emit a list of brushings when the brushings list is updated
     * and convert them into stats and store them locally, after cleaning up the local DB.
     *
     */
    private val brushingObserver by lazy {
        brushingRepository.getNonDeletedBrushings()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .observeOn(Schedulers.io())
            .doOnNext { list -> if (list.isNotEmpty()) addLatestStats(list) }
            .compose(ReplayingShare.instance())
    }

    /**
     * Get the list of stats from a given date for a given profileId
     *
     * @param startTime the date to get the stat from , a ZonedDateTime object
     * @param profileId the id of the profile to update the stat
     * @return a Flowable with a list of Stats object
     */
    override fun getStatsSince(startTime: ZonedDateTime, profileId: Long): Flowable<List<Stat>> {
        return brushingObserver
            .switchMapMaybe {
                statDao.readStatsSince(profileId, startTime.toInstant().toEpochMilli())
            }
            .map {
                it.map { statInternal ->
                    val checkupData = checkupCalculator.calculateCheckup(
                        statInternal.processedData,
                        TimeUnit.MILLISECONDS.toSeconds(statInternal.timestamp),
                        statInternal.durationObject
                    )

                    Stat.fromStatInternal(statInternal, checkupData)
                }
            }
    }

    override fun truncate(): Completable {
        return Completable.fromAction {
            statDao.truncate()
        }
    }

    /**
     * Get the latest brushings and store the stat locally every time the brushingRepo
     * emits a new list of brushings. The duplicates are removed
     */
    private fun addLatestStats(brushings: List<Brushing>) {
        val statsToAdd = brushings
            .map { StatInternal.fromBrushing(it, clock) }
            .distinct()
        brushings.forEach { brushing -> statDao.deleteForProFile(brushing.profileId) }
        statDao.insertAll(statsToAdd)
    }
}
