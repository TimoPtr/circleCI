package com.kolibree.android.sdk.core

import android.content.Context
import android.net.MacAddress
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.bluetoothTagFor
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.runOnMainThread
import com.kolibree.android.sdk.KolibreeAndroidSdk
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.driver.KLTBDriverFactory
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.sdk.scan.BluetoothSessionResetterRegisterer
import com.kolibree.android.sdk.scan.ConnectionScannedTracker
import com.kolibree.android.sdk.scan.EstablishConnectionFilter
import com.kolibree.android.sdk.scan.ToothbrushScanner
import com.kolibree.android.sdk.util.IBluetoothUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import timber.log.Timber

/**
 * Responsible for creating KLTBConnection instances.
 */
@Keep
interface KLTBConnectionPool : AutoCloseable {
    /**
     * Initializes the pool instance. It will automatically attempt to establish a connection to
     * all known Toothbrushes.
     *
     * Caller is responsible to invoke close() when the KLTBPool should be released
     *
     * Future subscriptions on a non-closed instance won't have any effect
     * Future subscriptions on a closed instance will behave as if it were the first invocation
     *
     * @return [Completable] that creates all [KLTBConnection] known by the active account and
     * attempts to establish a connection to each one.
     */
    fun init(): Completable

    /**
     * Initializes the pool instance. It will automatically attempt to establish a connection to
     * all known Toothbrushes that were previously scanned since the phone rebooted.
     *
     * See https://kolibree.atlassian.net/browse/KLTB002-9867 description & comments for context
     *
     * Caller is responsible to invoke close() when the KLTBPool should be released
     *
     * Future subscriptions on a non-closed instance won't have any effect
     * Future subscriptions on a closed instance will behave as if it were the first invocation
     *
     * @return [Completable] that creates all [KLTBConnection] known by the active account and
     * attempts to establish a connection to each one that we have scanned since the user
     * rebooted the phone
     */
    fun initOnlyPreviouslyScanned(): Completable

    /**
     * @return [List]<[KLTBConnection]> with all known connections, independently of the [KLTBConnectionState]
     */
    fun getKnownConnections(): List<KLTBConnection>

    /**
     * @return Flowable that will emit [List]<[InternalKLTBConnection]> with the known connections
     * each time a connection is added or removed from the pool. The list can be empty.
     *
     * It won't emit a new list on connection state change.
     *
     * It won't complete unless there's an error, even if this instance is closed.
     */
    fun getKnownConnectionsOnceAndStream(): Flowable<out List<KLTBConnection>>

    /**
     * Closes the resources held by the [KLTBConnectionPool] instance.
     *
     * Invocations on an already closed instance won't have any effect
     *
     * Once close has been invoked, future subscriptions to [init] will behave as if it were the first invocation
     */
    override fun close()
}

internal interface InternalKLTBConnectionPoolManager : KLTBConnectionPool {
    /**
     * Creates a InternalKLTBConnection from the given parameters.
     *
     * Future calls to createConnection for the same mac will return the same instance, unless
     * forget has been invoked. In that case, a new instance will be returned
     */
    fun create(mac: String, name: String, model: ToothbrushModel): InternalKLTBConnection

    /**
     * Creates a InternalKLTBConnection from the given parameters and immediately attempts to
     * establish a connection
     *
     * Future calls to createAndEstablish for the same mac will return the same instance, unless
     * forget has been invoked. In that case, a new instance will be returned.
     *
     * This method will not attempt to establish a connection if an instance for the mac address
     * already existed
     */
    fun createAndEstablish(
        mac: String,
        name: String,
        model: ToothbrushModel
    ): InternalKLTBConnection

    /**
     * Returns an InternalKLTBConnection from the connection list, if present
     */
    fun get(mac: String): InternalKLTBConnection?

    /**
     * The KLTBConnection associated to the specified mac address will no longer be associated to
     * the active account.
     *
     * Future calls to init won't attempt to establish a connection to the specified mac address
     *
     * Future calls to create with the specified mac address will return a new instance
     */
    fun forget(mac: String): Completable

    /**
     * The KLTBConnection will no longer be associated to the active account.
     *
     * Future calls to init won't attempt to establish a connection to the specified KLTBConnection
     *
     * Future calls to create with a mac address equal to the specified KLTBConnection will return
     * a new instance
     */
    fun forget(connection: KLTBConnection): Completable
}

internal class KLTBConnectionPoolManagerImpl
constructor(
    context: Context,
    private val kltbDriverFactory: KLTBDriverFactory,
    private val toothbrushRepository: ToothbrushRepository,
    private val doctorFactory: KLTBConnectionDoctorFactory,
    private val establishConnectionFilter: EstablishConnectionFilter,
    private val connectionScannedTracker: ConnectionScannedTracker,
    private val bluetoothSessionResetterRegisterer: BluetoothSessionResetterRegisterer
) : InternalKLTBConnectionPoolManager {

    @Inject
    constructor(
        context: Context,
        kltbDriverFactory: KLTBDriverFactory,
        toothbrushRepository: ToothbrushRepository,
        doctorFactory: KLTBConnectionDoctorFactory,
        establishConnectionFilter: EstablishConnectionFilter,
        connectionScannedTracker: ConnectionScannedTracker
    ) : this(
        context = context,
        kltbDriverFactory = kltbDriverFactory,
        toothbrushRepository = toothbrushRepository,
        doctorFactory = doctorFactory,
        establishConnectionFilter = establishConnectionFilter,
        connectionScannedTracker = connectionScannedTracker,
        bluetoothSessionResetterRegisterer = BluetoothSessionResetterRegisterer
    )

    init {
        /*
         * If we have never scanned for a TB during the Bluetooth session, establishing a connection
         * will fail
         *
         * BroadcastReceiver registered in Manifest won't work to track if BT was turned off while
         * our app was in background (see
         * https://developer.android.com/guide/components/broadcasts#changes-system-broadcasts)
         *
         * So, we need to register it here
         */
        bluetoothSessionResetterRegisterer.register(context)
    }

    companion object {
        private val TAG = bluetoothTagFor(KLTBConnectionPoolManagerImpl::class)
    }

    private val appContext = context.applicationContext

    @VisibleForTesting
    val initialized = AtomicBoolean()

    @VisibleForTesting
    val connectionList = CopyOnWriteArrayList<KLTBConnectionDoctor>()

    private val connectionsBehaviorSubject: BehaviorProcessor<List<InternalKLTBConnection>> =
        BehaviorProcessor.create()

    override fun initOnlyPreviouslyScanned(): Completable {
        return internalInit(
            onInitSuccessCompletable = Completable.fromAction { establishConnectionFilter.disableScanBeforeConnect() }
        )
    }

    override fun init(): Completable {
        return internalInit(
            onInitSuccessCompletable = Completable.fromAction {
                forceScanOnFirstConnection()

                establishConnectionFilter.enableScanBeforeConnect()
            }
        )
    }

    /**
     * If we have never scanned for a TB during the Bluetooth session, establishing a connection
     * will fail
     *
     * A bluetooth session is restarted under two scenarios
     *
     * - Phone is rebooted
     * - Bluetooth is turned off
     *
     * So, the only way to ensure that establishing connections succeed is to force to
     * scan on the first attempt. To achieve that, we need to clear ConnectionScannedTracker
     */
    private fun forceScanOnFirstConnection() {
        connectionScannedTracker.clear()
    }

    @VisibleForTesting
    internal fun internalInit(onInitSuccessCompletable: Completable): Completable {
        return Single.defer { Single.just(maybeInitialize()) }
            .flatMapCompletable { initializeSuccess ->
                if (initializeSuccess) {
                    onInitSuccessCompletable
                        .andThen(establishConnectionsCompletable())
                } else {
                    Completable.complete()
                }
            }
    }

    @VisibleForTesting
    internal fun establishConnectionsCompletable(): Completable {
        return toothbrushRepository
            .listAll()
            .flatMapCompletable { connections ->
                val connectionsToCreate = connections
                    .filter { accountToothbrush ->
                        establishConnectionFilter.canAttemptConnection(accountToothbrush.mac)
                    }

                onConnectionsRead(connectionsToCreate)
            }
    }

    /**
     * @return true if the instance was initialized, false if it was already initialized
     */
    private fun maybeInitialize(): Boolean {
        synchronized(initialized) {
            return initialized.compareAndSet(
                false,
                true
            )
        }
    }

    @VisibleForTesting
    fun onConnectionsRead(accountToothbrushes: List<AccountToothbrush>): Completable {
        return Completable.fromAction {
            for ((mac, name, model) in accountToothbrushes) {
                Timber.tag(TAG).v("onConnectionsRead create %s in %s", name, this)
                create(mac, name, model)
            }

            connectionList.iterator().forEach {
                Timber.tag(TAG).v(
                    "onConnectionsRead establish %s in %s",
                    it.connection.toothbrush().getName(),
                    this
                )
                it.init()
            }
        }
    }

    override fun create(mac: String, name: String, model: ToothbrushModel): InternalKLTBConnection {
        validateMac(mac) // this'll throw an IllegalArgumentException if it's not valid

        get(mac)?.let { existingConnection -> return existingConnection }

        val connection = newKLTBConnectionInstance(mac, name, model)
        val connectionDoctor = newConnectionDoctorInstance(connection)

        connectionList.addIfAbsent(connectionDoctor)
            .also { isAdded ->
                if (isAdded)
                    refreshKnownConnectionsStream()
            }

        return connection
    }

    override fun createAndEstablish(
        mac: String,
        name: String,
        model: ToothbrushModel
    ): InternalKLTBConnection {
        Timber.tag(TAG)
            .i("Invokes createAndEstablish to %s, previous connection is %s", mac, get(mac))
        val connection = get(mac) ?: create(mac, name, model)

        // thjs should never be null
        getDoctor(mac)?.init() ?: Timber.tag(TAG).e("Doctor was null for %s", mac)

        return connection
    }

    @VisibleForTesting
    fun newConnectionDoctorInstance(connection: InternalKLTBConnection): KLTBConnectionDoctor {
        return doctorFactory.createDoctor(connection)
    }

    @VisibleForTesting
    fun bluetoothUtils(): IBluetoothUtils = KolibreeAndroidSdk.getSdkComponent().bluetoothUtils()

    @VisibleForTesting
    fun toothbrushScanner(connection: KLTBConnection): ToothbrushScanner {
        return KolibreeAndroidSdk.getSdkComponent().toothbrushScannerFactory()
            .getScanner(appContext, connection.toothbrush().model)!!
    }

    @VisibleForTesting
    fun validateMac(mac: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            MacAddress.fromString(mac)
        } else {
            check(!mac.isBlank()) { "Mac can't be blank" }
        }
    }

    @VisibleForTesting
    fun newKLTBConnectionInstance(
        mac: String,
        name: String,
        model: ToothbrushModel
    ): InternalKLTBConnection =
        KLTBConnectionImpl(appContext, mac, name, model, kltbDriverFactory)

    @VisibleForTesting
    override fun get(mac: String): InternalKLTBConnection? {
        return getDoctor(mac)?.connection
    }

    private fun getDoctor(mac: String): KLTBConnectionDoctor? {
        synchronized(connectionList) {
            connectionList.forEach {
                if (it.connection.toothbrush().mac == mac) {
                    return it
                }
            }
        }

        return null
    }

    override fun getKnownConnections(): List<InternalKLTBConnection> =
        connectionList.map { it.connection }

    /**
     * @return Flowable that will emit a [List]<[InternalKLTBConnection]> with the known connections
     * each time a connection is added or removed from the pool. The list can be empty.
     *
     * It won't emit a new list on connection state change.
     *
     * It won't complete unless there's an error, even if this instance is closed.
     */
    override fun getKnownConnectionsOnceAndStream(): Flowable<out List<KLTBConnection>> {
        return connectionsBehaviorSubject
            .startWith { publisher ->
                publisher.onNext(getKnownConnections())
                publisher.onComplete()
            }
            .hide()
    }

    @VisibleForTesting
    fun refreshKnownConnectionsStream() {
        /*
        Post on mainThread to avoid blocking if invoked within a RxStream
         */
        { connectionsBehaviorSubject.onNext(getKnownConnections()) }.runOnMainThread()
    }

    override fun forget(connection: KLTBConnection): Completable {
        return forget(connection.toothbrush().mac)
    }

    override fun forget(mac: String): Completable {
        return Completable.defer {
            Timber.i("Migration : forget $mac")
            getDoctor(mac)?.let { doctor -> disconnectAndDelete(doctor) }
                ?: Completable.error(UnknownToothbrushException(mac))
        }
    }

    private fun disconnectAndDelete(connectionDoctor: KLTBConnectionDoctor): Completable {
        return toothbrushRepository
            .remove(connectionDoctor.mac())
            .doOnSubscribe { connectionDoctor.close() }
            .onErrorComplete()
            .andThen(Completable
                .fromAction { connectionList.remove(connectionDoctor) }
                .doOnComplete { refreshKnownConnectionsStream() }
            )
    }

    override fun close() {
        synchronized(initialized) {
            Timber.tag(TAG).d("Closing %s", this)
            connectionList.iterator().forEach { it.close() }

            /*
            Since we don't clear connectionList after closing and KLTBConnectionPool has
            @ToothbrushSdkScope, it's guaranteed that as long as our
            process is alive, we'll always receive the same KLTBConnectionImpl instance. This is
            probably not good because those objects are huge and they have a lot of state.

            On the other hand, if we ever change that behaviour, many components might stop working.
            For example, GameToothbrushInteractorFacade won't reinject its fields if it was
            already initialized. If a connection to a given mac was represented by a new instance
            of KLTBConnection, games will stop working
             */

            initialized.compareAndSet(true, false)
        }
    }
}

@Keep
class UnknownToothbrushException(mac: String) : Exception("Unknown connection with mac $mac")
