/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection

import androidx.annotation.Keep
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.sdk.util.IBluetoothUtils
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.Duration

/** Check whether all conditions are met for a successful pairing attempt */
@Keep
interface CheckConnectionPrerequisitesUseCase {

    /**
     * Checks whether the device is ready to start a connection
     *
     * @return [ConnectionPrerequisitesState]
     */
    fun checkConnectionPrerequisites(): ConnectionPrerequisitesState

    /**
     * Stream that will emit [ConnectionPrerequisitesState]
     *
     * The first is emitted immediately after subscription; following emissions represent change
     * of state.
     *
     * It will emit consecutive duplicates
     *
     * While a subscription is active, it checks periodically the state of Location
     *
     * @return [Observable]<[ConnectionPrerequisitesState]> that emits [1-N] [ConnectionPrerequisitesState]
     */
    fun checkOnceAndStream(): Observable<ConnectionPrerequisitesState>
}

/** [CheckConnectionPrerequisitesUseCase] implementation */
internal class CheckConnectionPrerequisitesUseCaseImpl @Inject constructor(
    private val bluetoothUtils: IBluetoothUtils,
    private val locationStatus: LocationStatus,
    @SingleThreadScheduler private val timeScheduler: Scheduler
) : CheckConnectionPrerequisitesUseCase {

    override fun checkOnceAndStream(): Observable<ConnectionPrerequisitesState> {
        return Observable.interval(
            0,
            CHECK_PREREQUISITES_INTERVAL.seconds,
            TimeUnit.SECONDS,
            timeScheduler
        )
            .map { checkConnectionPrerequisites() }
    }

    override fun checkConnectionPrerequisites() =
        when {
            bluetoothUtils.isBluetoothEnabled.not() ->
                ConnectionPrerequisitesState.BluetoothDisabled
            locationStatus.shouldAskPermission() ->
                ConnectionPrerequisitesState.LocationPermissionNotGranted
            locationStatus.shouldEnableLocation() ->
                ConnectionPrerequisitesState.LocationServiceDisabled
            else ->
                ConnectionPrerequisitesState.ConnectionAllowed
        }
}

@Keep
enum class ConnectionPrerequisitesState {
    BluetoothDisabled,
    LocationServiceDisabled,
    LocationPermissionNotGranted,
    ConnectionAllowed
}

private val CHECK_PREREQUISITES_INTERVAL = Duration.ofSeconds(1)
