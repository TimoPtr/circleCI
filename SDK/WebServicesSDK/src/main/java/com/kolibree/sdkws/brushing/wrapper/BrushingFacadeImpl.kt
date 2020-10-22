package com.kolibree.sdkws.brushing.wrapper

import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.exception.NoAccountException
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.GameApiConstants.GAME_OFFLINE
import com.kolibree.android.commons.profile.HANDEDNESS_LEFT
import com.kolibree.android.commons.profile.HANDEDNESS_RIGHT
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.kolibree.sdkws.exception.NoExistingBrushingException
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime

@SuppressWarnings("TooManyFunctions")
@VisibleForApp
class BrushingFacadeImpl
@Inject constructor(
    private val brushingsRepository: BrushingsRepository,
    private val kolibreeConnector: InternalKolibreeConnector,
    private val checkupCalculator: CheckupCalculator
) : BrushingFacade {

    override fun synchronizeBrushing(profileId: Long): Single<Boolean> =
        currentAccountId()?.let { accountId ->
            brushingsRepository.synchronizeBrushing(accountId, profileId)
        } ?: Single.error(NoAccountException)

    /**
     * Add a brushing to a profile
     *
     * @return non null [brushing] [Single] brushing
     */
    override fun addBrushing(brushing: IBrushing, profile: IProfile): Single<IBrushing> {
        return currentAccountId()?.let { accountId ->
            val brushingData = CreateBrushingData(
                GAME_OFFLINE,
                brushing.duration,
                brushing.goalDuration,
                brushing.dateTime,
                0
            )

            return brushingsRepository.addBrushing(
                brushingData,
                ProfileInternal(
                    firstName = profile.firstName,
                    gender = profile.gender.serializedName,
                    handedness = if (profile.isRightHanded()) HANDEDNESS_RIGHT else HANDEDNESS_LEFT,
                    birthday = profile.birthday,
                    brushingTime = profile.brushingGoalTime,
                    id = profile.id,
                    creationDate = profile.createdDate,
                    accountId = accountId.toInt(),
                    points = 0,
                    brushingNumber = 0
                ),
                accountId
            ).map { it }
        } ?: Single.error(NoAccountException)
    }

    /**
     * Get all the brushings for a profile
     *
     * @return non null [IBrushing] [List] [Single] list of brushings stored
     */
    override fun getBrushings(profileId: Long): Single<List<IBrushing>> =
        brushingsRepository.getBrushings(profileId)
            .map { it }

    override fun brushingsFlowable(profileId: Long): Flowable<List<IBrushing>> =
        brushingsRepository.brushingsFlowable(profileId)
            .map { it }

    /**
     * Get all the brushings for a profile since a given date
     *
     * @return non null [IBrushing] [List] [Single] list of brushings stored since this date
     */
    override fun getBrushingsSince(
        startTime: OffsetDateTime,
        profileId: Long
    ): Single<List<IBrushing>> =
        brushingsRepository.getBrushingsSince(startTime, profileId).map { it }

    override fun getBrushingSessions(
        beginDateTime: OffsetDateTime,
        endDateTime: OffsetDateTime,
        profileId: Long
    ): Observable<List<IBrushing>> =
        Single.concat(
            brushingsRepository.getBrushingsBetween(beginDateTime, endDateTime, profileId),
            synchronizedBrushingSessions(profileId, beginDateTime, endDateTime)
        )
            .toObservable()
            .filter { it.isNotEmpty() }
            .map { it }

    @VisibleForTesting
    fun synchronizedBrushingSessions(
        profileId: Long,
        beginDateTime: OffsetDateTime,
        endDateTime: OffsetDateTime
    ) = brushingsRepository.fetchRemoteBrushings(
        accountId = kolibreeConnector.accountId,
        profileId = profileId,
        fromDate = beginDateTime.toLocalDate(),
        toDate = endDateTime.toLocalDate()
    )
        .flatMap { brushingsRepository.getBrushingsBetween(beginDateTime, endDateTime, profileId) }
        .onErrorReturnItem(emptyList())

    /**
     * Get all the brushings for a profile between two given date
     *
     * @return non null [IBrushing] [List] [Single] list of brushings stored since this date
     */
    override fun getBrushingsBetween(
        begin: OffsetDateTime,
        end: OffsetDateTime,
        profileId: Long
    ): Single<List<IBrushing>> =
        brushingsRepository.getBrushingsBetween(begin, end, profileId).map { it }

    /**
     * Get the latest brushing sessions for a profile
     *
     * @return non null [IBrushing] [Maybe] last brushing if exist
     */
    override fun getLastBrushingSession(profileId: Long): Single<IBrushing> =
        brushingsRepository.getLastBrushingSession(profileId)?.let {
            Single.just(it as IBrushing)
        } ?: Single.error(NoExistingBrushingException(profileId))

    /**
     * Get the latest brushing session for a profile.
     *
     * Whenever there are changes to brushing database, it emits the new last brushing session
     * if it is different from the previous one
     *
     * @return non null [Flowable] [IBrushing] [Maybe] that will emit the last brushing session
     */
    override fun getLastBrushingSessionFlowable(profileId: Long): Flowable<IBrushing> =
        brushingsRepository.getLastBrushingSessionFlowable(profileId)
            .map { it }

    /**
     * delete a  brushing for a profile
     *
     * @return non null [Completable]
     */
    override fun deleteBrushing(brushing: IBrushing): Completable =
        currentAccountId()?.let { accountId ->
            brushingsRepository.deleteBrushing(
                accountId, brushing.profileId, Brushing(
                    duration = brushing.duration,
                    goalDuration = brushing.goalDuration,
                    dateTime = brushing.dateTime,
                    processedData = brushing.processedData,
                    game = brushing.game,
                    kolibreeId = brushing.kolibreeId,
                    profileId = brushing.profileId,
                    points = 0,
                    coins = 0
                )
            )
        } ?: Completable.error(NoAccountException)

    /**
     * Compute the quality of a brushing.
     * @param brushing brushing to get the quality from
     * @return [Int]  quality of the brushing, value between 0 and 100
     */
    override fun getQualityBrushing(brushing: IBrushing) =
        checkupCalculator.calculateCheckup(
            brushing
        ).surfacePercentage

    @VisibleForTesting
    fun currentAccountId(): Long? = kolibreeConnector.accountId.takeIf { it != -1L }
}
