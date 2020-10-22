package com.kolibree.android.sdk.core

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import io.reactivex.Single

@Keep
interface KLTBConnectionProvider {

    /**
     * This method is a best effort at returning an active KLTBConnection
     *
     * <p>1. If there's no service or the service doesn't return any KLTBConnection for the
     * macAddress, returns a Single.error
     *
     * <p>2. If there's an ACTIVE connection, the Single emits that connection
     *
     * <p>3. If there's a connection but it's not ACTIVE, Single emits the connection when it becomes ACTIVE, or errors
     * if it's in TERMINATING or TERMINATED
     *
     * <p>Timeout is not managed by this class, it should be handled by the client
     *
     * @return a Single that will emit on MainThread a KLTBConnection with state ACTIVE, or an error
     */
    fun existingActiveConnection(macAddress: String): Single<KLTBConnection>

    fun existingConnectionWithStates(
        macAddress: String,
        acceptedStates: List<KLTBConnectionState>
    ): Single<KLTBConnection>

    fun getKLTBConnectionSingle(macAddress: String): Single<KLTBConnection>
}
