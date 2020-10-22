package com.kolibree.android.sdk.core.toothbrush

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.error.CommandNotSupportedException
import io.reactivex.Completable
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by aurelien on 11/08/17.
 *
 * [com.kolibree.android.sdk.connection.toothbrush.Toothbrush] implementation constructor
 *
 * @param mac non null toothbrush bluetooth mac address
 * @param model non null toothbrush model
 * @param toothbrushName non null toothbrush bluetooth toothbrushName
 */
internal abstract class ToothbrushBaseImpl(
    override val mac: String,
    override val model: ToothbrushModel,
    toothbrushName: String
) : ToothbrushImplementation {

    /** Toothbrush name cache  */
    private val name: AtomicReference<String> = AtomicReference(toothbrushName)

    /** Serial number cache  */
    private val serial: AtomicReference<String> = AtomicReference("")

    override val serialNumber: String
        get() = serial.get() ?: ""

    override fun getName(): String = name.get()

    override fun setAndCacheName(toothbrushName: String): Completable = Completable.fromAction {
        checkNameLength(toothbrushName)
        setToothbrushName(toothbrushName)
        name.set(toothbrushName)
    }

    override fun cacheName(toothbrushName: String) {
        checkNameLength(toothbrushName)
        name.set(toothbrushName)
    }

    @VisibleForTesting
    fun checkNameLength(toothbrushName: String) =
        require(
            !(toothbrushName.toByteArray().size > MAX_NAME_BYTES_LENGTH ||
                toothbrushName.isEmpty())
        ) { "Invalid toothbrush name bytes length, min 1 max 19" }

    @Throws(Exception::class)
    protected abstract fun setToothbrushName(name: String)

    override fun setSerialNumber(serialNumber: String) {
        serial.set(serialNumber)
    }

    /*
    Only 2nd generation toothbrushes implement this parameter.
    Overridden in the ToothbrushKLTB002Impl class

    https://kolibree.atlassian.net/browse/KLTB002-8463
     */
    override fun setAdvertisingIntervals(fastModeIntervalMs: Long, slowModeIntervalMs: Long) =
        Completable.error(CommandNotSupportedException())

    companion object {

        @VisibleForTesting
        const val MAX_NAME_BYTES_LENGTH = 19
    }
}
