package com.kolibree.android.sdk.core.driver.ble.nordic

import android.bluetooth.BluetoothDevice

/** This class provides methods for the hack that adds 1 to the mac when an M1 is in bootloader  */
object DfuUtils {

    private const val MAX_MAC_DIGIT_VALUE = 0x100

    private const val HEX_RADIX = 16

    private const val LAST_BYTE_STRING_INDEX = 15

    @JvmStatic
    fun getDFUMac(device: BluetoothDevice): String {
        return changeMac(device.address, true)
    }

    @JvmStatic
    fun getDFUMac(mac: String): String {
        return changeMac(mac, true)
    }

    @JvmStatic
    private fun changeMac(mac: String, toBootloaderMac: Boolean): String {
        val parts = mac.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val lastPart = parts[parts.size - 1]
        var value = Integer.parseInt(lastPart, HEX_RADIX)
        if (toBootloaderMac) {
            value++
        } else {
            value--
        }

        @Suppress("MagicNumber")
        return mac.replaceRange(LAST_BYTE_STRING_INDEX, LAST_BYTE_STRING_INDEX + 2, safeFormatRange(value))
    }

    @JvmStatic
    fun getMainAppMac(mac: String): String {
        return changeMac(mac, false)
    }

    // Safely handle cases like < 0x10 or hex¹ overflow
    @Suppress("MagicNumber")
    private fun safeFormatRange(range: Int): String {
        return String.format("%02X", if (range == MAX_MAC_DIGIT_VALUE) 0x00 else range and 0xFF)
    }
}
