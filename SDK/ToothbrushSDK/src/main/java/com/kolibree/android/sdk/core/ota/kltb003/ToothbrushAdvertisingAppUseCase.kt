/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb003

import android.bluetooth.BluetoothDevice
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.commons.UpdateType
import com.kolibree.android.otaTagFor
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.driver.ble.nordic.DfuUtils
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.DFU_BOOTLOADER
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.MAIN
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushAdvertisingApp.NOT_FOUND
import com.kolibree.android.sdk.scan.AnyToothbrushScanCallback
import com.kolibree.android.sdk.scan.ToothbrushApp
import com.kolibree.android.sdk.scan.ToothbrushApp.UNKNOWN
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.scan.ToothbrushScannerFactory
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import timber.log.Timber

/**
 * Detects if a disconnected toothbrush is running [MAIN] or [DFU_BOOTLOADER], or [NOT_FOUND]
 *
 * After doing an OTA, if we installed a new Bootloader version, depending on the flash available
 * space, the toothbrush might have had to erase the main app and launch in bootloader mode. Or,
 * if there is enough flash space, it might have been able to keep application alive.
 *
 * If Application is running, toothbrush will advertise with MAC
 * If Bootloader mode is running, toothbrush will advertise with MAC +1
 *
 * I imagine there can be a short period of time were both advertise
 *
 * Since we can't know beforehand, we need to scan for both possible mac address (MAC and MAC +1) to
 * detect which of the 2 scenarios we are facing.
 *
 * Quoting a conversation with Emmanuel https://kolibree.slack.com/archives/G7J9NDA72/p1599730847028100
 *
 * Miguel
After updating Bootloader, Toothbrushes stayed in bootloader mode. Thus, I started a connection
to MAC + 1

I noticed that Plaqless does not stay in bootloader mode. So, I have to start a connection to MAC

Is this wanted? Can I hardcode that condition to the code?

 * Emmanuel Clerc
Well, it is safe to consider that both behavior are possible, with all brushes.

In fact the rule is simple :
- The bootloader does not directly overwrite itself , but makes a temporary copy of itself
in flash so that if power is lost while downloading it doesn't brisk the brush.
- if there is space enough for this in flash without erasing the app. then it does not erase
the app. so there is still an app to boot into and it boots there
- If there is no space enough. it erases the app so it is forced to boot in bootloader at
the end and you are forced to update the app. that's why in general we suggest to update BL
before the app.

Until now :
- on E2, B1, there is less flash than on Plaqless so app is always erased
- on plaqless there is more flash, so unless the app grows widely it will not erase the app
when uploading the BL

But for G2, we had to optimize code size. it had a side effect of reducing code size of E2, B1,
M1. too . In the latest version, This seems to liberate just enough space in flash so that a
bootloader can be copied.

So you might see in the future some E2 or B1 brush that do not reboot in BL but keep their app
instead.
 *
 * Glossary
 * * Application: Main application running the Toothbrush
 * * Bootloader: Bootloader mode, the toothbrush has minimal capabilities (user can't brush)
 * * MAC: Original MAC of the toothbrush
 * * MAC + 1: When Toothbrush is in bootloader mode, it advertises with MAC + 1
 * * OTA: Over The Air update. Apply a software patch on the toothbrush
 */
internal class ToothbrushAdvertisingAppUseCase
@Inject constructor(
    @SingleThreadScheduler private val timeoutScheduler: Scheduler,
    private val scannerFactory: ToothbrushScannerFactory,
    connection: InternalKLTBConnection
) {
    private val toothbrushMac by lazy { connection.toothbrush().mac }
    private val dfuMac by lazy { DfuUtils.getDFUMac(connection.toothbrush().mac) }

    /**
     * Detects the [ToothbrushAdvertisingApp] of the [InternalKLTBConnection] specified as
     * constructor parameter
     *
     * * If [toothbrushMac] is scanned, it will emit [MAIN]
     * * After [SCAN_TIMEOUT] seconds without detecting [toothbrushMac]
     * ** If [dfuMac] is scanned, it will emit [DFU_BOOTLOADER]
     * ** If neither of them is scanned, it will emit [NOT_FOUND]
     *
     * Multiple subscribers might result in unexpected behavior, since they may interfere with the
     * state of scanning for devices
     *
     * @return [Single]<[ToothbrushAdvertisingApp]> that will emit the
     * [ToothbrushAdvertisingApp] of the [InternalKLTBConnection] specified as constructor
     * parameter
     */
    fun advertisingStateSingle(updateType: UpdateType): Single<ToothbrushAdvertisingApp> {
        if (updateType != UpdateType.TYPE_BOOTLOADER) {
            return Single.just(MAIN)
        }

        return Single.fromCallable { scannerFactory.getCompatibleBleScanner() }
            .flatMap { scanner ->
                val foundMacs = mutableSetOf<String>()

                Single.create<ToothbrushAdvertisingApp> { emitter ->
                    val callback = object : AnyToothbrushScanCallback {
                        override fun onToothbrushFound(result: ToothbrushScanResult) {
                            foundMacs.add(result.mac)

                            /*
                            If we found toothbrushMac, we wouldn't need to store foundMacs. But to play on the safe
                            side, I prefer to store it first in case a TimeoutException kicks in at the wrong time
                             */
                            if (result.mac == toothbrushMac) {
                                emitter.onSuccess(MAIN)
                            }
                        }

                        override fun onError(errorCode: Int) {
                            emitter.tryOnError(RuntimeException("Error scanning: $errorCode"))
                        }

                        override fun bluetoothDevice(): BluetoothDevice? = null
                    }

                    scanner.startScan(callback = callback, includeBondedDevices = false)

                    emitter.setCancellable { scanner.stopScan(callback) }
                }
                    .timeout(SCAN_TIMEOUT, TimeUnit.SECONDS, timeoutScheduler)
                    .onErrorResumeNext { throwable ->
                        if (throwable is TimeoutException) {
                            Single.just(foundMacs.toToothbrushAdvertisingApp())
                        } else {
                            Single.error(throwable)
                        }
                    }
            }
    }

    private fun Set<String>.toToothbrushAdvertisingApp(): ToothbrushAdvertisingApp =
        when {
            contains(toothbrushMac) -> MAIN
            contains(dfuMac) -> DFU_BOOTLOADER
            else -> {
                Timber.tag(TAG)
                    .w("Toothbrush not found. Expecting $toothbrushMac or $dfuMac, found $this")

                NOT_FOUND
            }
        }
}

/**
 * Seconds after which we consider Toothbrush is not advertising Main App
 *
 * Android reports scan results every ~5 seconds. Thus, 16 seconds is equivalent to 3 batch scan
 * results before timing out
 */
private const val SCAN_TIMEOUT = 16L

/**
 * Similar to [ToothbrushApp], but replaces [UNKNOWN] for [NOT_FOUND] state
 */
internal enum class ToothbrushAdvertisingApp {
    /**
     * Toothbrush is running [ToothbrushApp.MAIN]
     */
    MAIN,

    /**
     * Toothbrush is running [ToothbrushApp.DFU_BOOTLOADER]
     */
    DFU_BOOTLOADER,

    /**
     * No Scan result received for specified toothbrush
     */
    NOT_FOUND
}

private val TAG = otaTagFor(ToothbrushAdvertisingAppUseCase::class)
