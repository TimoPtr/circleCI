package com.kolibree.sdkws.brushing.wrapper

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.sdkws.exception.NoExistingBrushingException
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.OffsetDateTime

/**
 * Interface to communicate with the brushing module.
 * Your custom brushing object needs to implement the IBrushing interface.
 * The most important method in this module is the synchronizeBrushing
 * that needs to be called every time you load a profile in order to sync the local and
 * remote brushings.
 */
@Keep
@Suppress("TooManyFunctions")
interface BrushingFacade {

    /**
     * Add a new brushing to a profile
     *
     * @param brushing brushing to add
     * @param profile profile to associated the brushing to
     * @return non null [IBrushing] [Single] brushing added
     */
    fun addBrushing(brushing: IBrushing, profile: IProfile): Single<IBrushing>

    /**
     * Get all the  brushings stored locally for a given profile, associated by its profile
     * Id. Do not forget to call the sync method of this module when you load a profile in order to
     * sync the local and remote brushings.
     *
     * @param profileId profileId of the profile to get the brushings from
     * @return non null [IBrushing] [List] [Single] list of brushings stored locally associated to the profile
     */
    fun getBrushings(profileId: Long): Single<List<IBrushing>>

    /**
     * Emits all the  brushings stored locally for a given profile, associated by its profile
     * Id. Do not forget to call the sync method of this module when you load a profile in order to
     * sync the local and remote brushings.
     *
     * If the brushings database is updated, the [Flowable] will emit a new [List]
     *
     * @param profileId profileId of the profile to get the brushings from
     * @return non null [IBrushing] [List] [Flowable] list of brushings stored locally associated to the profile
     */
    fun brushingsFlowable(profileId: Long): Flowable<List<IBrushing>>

    /**
     * Get all the brushing sessions for a given profile since a given date
     *
     * @param startTime date to get the brushings from
     * @param profileId profileId of the profile to get the brushings from
     * @return non null [IBrushing] [List] [Single] list of brushings stored for that user since this date
     */
    @Deprecated("This method only returns local data, please use getBrushingSessions instead")
    fun getBrushingsSince(startTime: OffsetDateTime, profileId: Long): Single<List<IBrushing>>

    /**
     * Get all the brushing sessions for a given profile between two given dates
     *
     * @param begin date to get the brushings from
     * @param end date to get the brushings to
     * @param profileId profileId of the profile to get the brushings from
     * @return non null [IBrushing] [List] [Single] list of brushings stored for that user since this date
     */
    @Deprecated("This method only returns local data, please use getBrushingSessions instead")
    fun getBrushingsBetween(
        begin: OffsetDateTime,
        end: OffsetDateTime,
        profileId: Long
    ): Single<List<IBrushing>>

    /**
     * Get all brushing sessions in a [[beginDateTime], [endDateTime]] range for a given profile ID.
     *
     * The returned [Observable] will first emit the local (database-provided) data.
     * In a second time it may emit an up-to-date (backend-synchronized) list of [IBrushing] before
     * completing.
     *
     * In case of connectivity lack, the returned [Observable] will only emit the local data, it
     * will then complete without emitting any network-related error.
     *
     * The [endDateTime] parameter can be set to TrustedClock.getNowOffsetDateTime(), in this case all brushing
     * sessions from [beginDateTime] will be emitted.
     *
     * @param profileId [Long] profile ID
     * @param beginDateTime [OffsetDateTime] beginning of the time period
     * @param endDateTime [OffsetDateTime] end of the time period
     * @return [IBrushing] [List] [Observable]
     */
    fun getBrushingSessions(
        beginDateTime: OffsetDateTime,
        endDateTime: OffsetDateTime,
        profileId: Long
    ): Observable<List<IBrushing>>

    /**
     * Get the latest brushing sessions for a profile
     * Throw a [NoExistingBrushingException] exception if there is no existing brushing for this user
     * @param profileId profileId of the profile to get the latest brushing from
     * @return non null [IBrushing] [Single] last brushing if exist
     */
    fun getLastBrushingSession(profileId: Long): Single<IBrushing>

    /**
     * Get the latest brushing sessions for a profile
     *
     * Whenever there are changes to brushing database, it emits the new last brushing session
     * if it is different from the previous one
     *
     * If there is no existing brushing for this user, it won't emit anything
     *
     * @param profileId profileId of the profile to get the latest brushing from
     * @return non null [IBrushing] [Single] last brushing if exist
     */
    fun getLastBrushingSessionFlowable(profileId: Long): Flowable<IBrushing>

    /**
     * delete a  brushing for a profile
     *
     * @param brushing brushing to delete
     * @return non null [Completable]
     */
    fun deleteBrushing(brushing: IBrushing): Completable

    /**
     * Synchronized the local brushing with the remote brushing on the server to make sure the local
     * and remote database contains the latest updated version.
     * If there are any brushings, It always stores it locally
     * It's important to call this method every time your load a profile.
     *
     * @param profileId profileId of the profile to get the latest brushing from
     * @return non null [Boolean] [Single] true is succeed, otherwise false
     */
    fun synchronizeBrushing(profileId: Long): Single<Boolean>

    /**
     * Compute the quality of a brushing.
     * @param brushing brushing to get the quality from
     * @return [Int]  quality of the brushing, value between 0 and 100
     */
    fun getQualityBrushing(brushing: IBrushing): Int
}
