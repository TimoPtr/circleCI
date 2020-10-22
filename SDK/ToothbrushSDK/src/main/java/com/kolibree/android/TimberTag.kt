package com.kolibree.android

import androidx.annotation.Keep
import kotlin.reflect.KClass

internal const val SDK_TAG = "\uD83D\uDD8C️" // 🖌️ emoji, see https://emojipedia.org/lower-left-paintbrush/
internal const val BLUETOOTH_TAG = "BT"
internal const val OTA_UPDATE_TAG = "OTA"
internal const val OFFLINE_BRUSHINGS_TAG = "OFFL"

internal fun bluetoothTagFor(clazz: KClass<*>): String = "$SDK_TAG|$BLUETOOTH_TAG|${clazz.java.simpleName}" // 🖌️|BT|

internal fun bluetoothTagFor(clazz: Class<*>): String = "$SDK_TAG|$BLUETOOTH_TAG|${clazz.simpleName}" // 🖌️|BT|

@Keep
fun otaTagFor(clazz: KClass<*>): String = "$SDK_TAG|$OTA_UPDATE_TAG|${clazz.java.simpleName}" // 🖌️|OTA|

@Keep
fun otaTagFor(clazz: Class<*>): String = "$SDK_TAG|$OTA_UPDATE_TAG|${clazz.simpleName}" // 🖌️|OTA|

@Keep
fun offlineBrushingsTagFor(clazz: KClass<*>): String =
    "$SDK_TAG|$OFFLINE_BRUSHINGS_TAG|${clazz.java.simpleName}" // 🖌️|OFF|

@Keep
fun offlineBrushingsTagFor(clazz: Class<*>): String =
    "$SDK_TAG|$OFFLINE_BRUSHINGS_TAG|${clazz.simpleName}" // 🖌️|OFF|
