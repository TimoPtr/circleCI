/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toolbartoothbrush

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@SuppressLint("DeobfuscatedPublicSdkClass")
interface ToothbrushConnectionState : Parcelable

@Parcelize
object Unknown : ToothbrushConnectionState

abstract class NoToothbrushConnected(open val toothbrushes: Int, open val mac: String?) :
    ToothbrushConnectionState

@Parcelize
data class NoService(override val toothbrushes: Int, override val mac: String?) :
    NoToothbrushConnected(toothbrushes, mac)

@Parcelize
data class NoLocation(override val toothbrushes: Int, override val mac: String?) :
    NoToothbrushConnected(toothbrushes, mac)

@Parcelize
data class NoToothbrushes(override val toothbrushes: Int) :
    NoToothbrushConnected(toothbrushes, null)

@Parcelize
data class NoBluetooth(override val toothbrushes: Int, override val mac: String?) :
    NoToothbrushConnected(toothbrushes, mac)

abstract class SingleToothbrush(open val mac: String) : ToothbrushConnectionState

@Parcelize
data class SingleToothbrushDisconnected(override val mac: String) : SingleToothbrush(mac)

@Parcelize
data class SingleToothbrushConnecting(override val mac: String) : SingleToothbrush(mac)

@Parcelize
data class SingleToothbrushConnected(override val mac: String) : SingleToothbrush(mac)

@Parcelize
data class SingleToothbrushOtaAvailable(override val mac: String) : SingleToothbrush(mac)

@Parcelize
data class SingleToothbrushOtaInProgress(override val mac: String) : SingleToothbrush(mac)

@Parcelize
data class SingleSyncingOfflineBrushing(override val mac: String) : SingleToothbrush(mac)

abstract class MultiToothbrush(open val macs: List<String>) : ToothbrushConnectionState

@Parcelize
data class MultiToothbrushDisconnected(override val macs: List<String>) : MultiToothbrush(macs)

@Parcelize
data class MultiToothbrushConnecting(override val macs: List<String>) : MultiToothbrush(macs)

@Parcelize
data class MultiToothbrushConnected(
    override val macs: List<String>
) : MultiToothbrush(macs)

@Parcelize
data class MultiToothbrushOtaAvailable(override val macs: List<String>) : MultiToothbrush(macs)

@Parcelize
data class MultiToothbrushOtaInProgress(override val macs: List<String>) : MultiToothbrush(macs)

@Parcelize
data class MultiSyncingOfflineBrushing(override val macs: List<String>) : MultiToothbrush(macs)
