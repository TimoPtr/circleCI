package com.kolibree.android.sdk.core

import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ESTABLISHING
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.NEW
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.OTA
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATED
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATING
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.sdk.connection.vibrator.VibratorListener
import com.kolibree.android.sdk.core.driver.VibratorDriver
import com.kolibree.android.sdk.core.driver.VibratorMode
import com.kolibree.android.sdk.core.notification.ListenerNotifier
import com.kolibree.android.sdk.core.notification.ListenerPool
import com.kolibree.android.sdk.core.notification.UniqueListenerPool
import com.kolibree.android.sdk.e1.ToothbrushAwaker
import com.kolibree.android.sdk.e1.ToothbrushAwakerImpl
import com.kolibree.android.sdk.error.FailureReason
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by aurelien on 08/03/17.
 *
 *
 * [Vibrator] implementation
 */
@AnyThread
internal class VibratorImpl @VisibleForTesting
constructor(
    eventsSource: KLTBConnection,
    /** Vibrator driver implementation  */
    private val driver: VibratorDriver,
    /** Vibrator state changes listener pool  */
    private val listenerPool: ListenerPool<VibratorListener>,
    private val toothbrushAwaker: ToothbrushAwaker?,
    private val mainHandler: Handler
) : Vibrator, DataCache {

    /** Toothbrush vibrator current state  */
    private val vibratorOn = AtomicBoolean(false)

    /** Event source  */
    private val source: WeakReference<KLTBConnection> = WeakReference(eventsSource)

    private val runWhenActiveListeners = NotifyWhenActive()
    private val runWhenActiveStream = NotifyWhenActive()

    override val isOn: Boolean
        get() = vibratorOn.get()

    private val stateRelay = PublishRelay.create<Boolean>()

    override val vibratorStream: Flowable<Boolean>
        get() {
            val stream = stateRelay.hide().toFlowable(BackpressureStrategy.LATEST)

            // avoid invoking alien method inside synchronized block
            synchronized(source) { source.get() }?.run {
                if (state().current == ACTIVE) {
                    return stream.startWith(isOn)
                }
            }

            return stream
        }

    /**
     * [Vibrator] implementation constructor
     *
     * @param eventsSource non null KLTBConnection events source
     * @param vibratorDriver non null VibratorDriver
     */
    constructor(
        eventsSource: KLTBConnection,
        vibratorDriver: VibratorDriver,
        mainHandler: Handler = Handler(Looper.getMainLooper())
    ) : this(
        eventsSource,
        vibratorDriver,
        UniqueListenerPool<VibratorListener>(
            "vibrator",
            true
        ),
        maybeCreateAwaker(eventsSource),
        mainHandler = mainHandler
    )

    override fun register(l: VibratorListener) {
        listenerPool.add(l)

        toothbrushAwaker?.keepAlive()
    }

    override fun unregister(l: VibratorListener) {
        val totalListeners = listenerPool.remove(l)

        if (totalListeners == 0) toothbrushAwaker?.allowShutdown()
    }

    override fun on(): Completable {
        return driver.setVibratorMode(VibratorMode.START)
    }

    override fun off(): Completable {
        return driver.setVibratorMode(VibratorMode.STOP)
    }

    override fun offAndStopRecording(): Completable {
        return driver.setVibratorMode(VibratorMode.STOP_AND_HALT_RECORDING)
    }

    override fun setLevel(percents: Int): Completable {
        return Completable.create { emitter ->
            try {
                if (percents < 0 || percents > 100) {
                    throw FailureReason("$percents is out of bounds [0, 100]")
                }
                driver.setVibrationLevel(percents)
                emitter.onComplete()
            } catch (e: Exception) {
                val throwable: Throwable = e.cause ?: e

                emitter.tryOnError(throwable)
            }
        }
    }

    fun onVibratorStateChanged(data: Boolean) {
        vibratorOn.set(data)

        onActiveConnection(runWhenActiveStream) {
            mainHandler.post {
                stateRelay.accept(data)
            }
        }

        onActiveConnection(runWhenActiveListeners) {
            listenerPool.notifyListeners(ListenerNotifier { listener ->
                listener.onVibratorStateChanged(it, data)
            })
        }
    }

    private inline fun onActiveConnection(
        runWhenActive: NotifyWhenActive,
        crossinline block: (KLTBConnection) -> Unit
    ) {
        // avoid invoking alien method inside synchronized block
        synchronized(source) { source.get() }?.let { connection ->
            runWhenActive.notify(connection) {
                block(connection)
            }
        }
    }

    override fun clearCache() {
        vibratorOn.set(false)

        toothbrushAwaker?.allowShutdown()
    }

    companion object {

        @VisibleForTesting
        fun maybeCreateAwaker(connection: KLTBConnection): ToothbrushAwaker? {
            return if (connection.toothbrush().model === ToothbrushModel.CONNECT_E1) {
                ToothbrushAwakerImpl(connection)
            } else null
        }
    }
}

/**
 * Utility class that holds the invocation of a given block until the [KLTBConnection] is either
 * established or terminating
 *
 * If the connection is establishing, it'll register itself as listener and run the block once the
 * connection is [ACTIVE]
 */
private class NotifyWhenActive {
    private val blockMap = HashMap<String, WeakReference<() -> Unit>>()

    private val listener = object : ConnectionStateListener {
        override fun onConnectionStateChanged(
            connection: KLTBConnection,
            newState: KLTBConnectionState
        ) {
            if (newState == ACTIVE || newState == TERMINATING || newState == TERMINATED) {
                connection.state().unregister(this)
            }

            if (newState == ACTIVE) {
                blockMap[connection.toothbrush().mac]?.get()?.invoke()
            }
        }
    }

    /**
     * Invokes [block] immediately if [KLTBConnectionState] of [connection] is established, tearing
     * down or terminated ([ACTIVE], [TERMINATING], [TERMINATED] or [OTA])
     *
     * If connection is being established ([NEW], [ESTABLISHING]], it registers this instance as
     * listener and will run [block] as soon as connection is [ACTIVE]
     *
     * Warning: On multiple invocations on the same [connection]
     * - If the connection is being established, only the last [block] will be invoked
     * - If the connection is already established, [block] will be immediately invoked
     */
    fun notify(connection: KLTBConnection, block: () -> Unit) {
        when (connection.state().current) {
            NEW, ESTABLISHING -> {
                blockMap[connection.toothbrush().mac] = WeakReference(block)

                connection.state().register(listener)
            }
            ACTIVE, TERMINATING, TERMINATED, OTA -> block()
        }
    }
}
