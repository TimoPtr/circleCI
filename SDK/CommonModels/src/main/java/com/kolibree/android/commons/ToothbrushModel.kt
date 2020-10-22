/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons

import androidx.annotation.Keep

/** Kolibree and Colgate toothbrush models enumeration  */
@Keep
enum class ToothbrushModel constructor(
    /** Kolibree device commercial name  */
    val commercialName: String,
    /** Internal name for server communication  */
    val internalName: String
) {

    /**
     * Kolibree Ara toothbrush
     *
     *
     * Bluetooth Low Energy
     */
    ARA("Ara", ARA_INTERNAL_NAME),

    /**
     * Kolibree third generation device
     *
     *
     * Bluetooth Low Energy
     */
    CONNECT_M1("Connect M1", CONNECT_M1_INTERNAL_NAME),

    /**
     * Colgate E1 toothbrush
     *
     *
     * Bluetooth Low Energy
     */
    CONNECT_E1("Connect E1", CONNECT_E1_INTERNAL_NAME),

    /**
     * Colgate E2 toothbrush
     *
     *
     * Bluetooth Low Energy
     */
    CONNECT_E2("Connect E2", CONNECT_E2_INTERNAL_NAME),

    /**
     * Colgate B1 toothbrush
     *
     *
     * Bluetooth Low Energy
     */
    CONNECT_B1("Connect B1", CONNECT_B1_INTERNAL_NAME),

    /**
     * Plaqless toothbrush
     *
     * Bluetooth Low Energy
     */
    PLAQLESS("Plaqless", PLAQLESS_INTERNAL_NAME),

    /**
     * HiLink toothbrush (BLE, Brushing Modes feature) based on CE2's hardware
     */
    HILINK("HiLink", HILINK_INTERNAL_NAME),

    /**
     * Hum branded E2
     *
     * Bluetooth Low Energy
     */
    HUM_ELECTRIC("Hum Electric", HUM_ELECTRIC_INTERNAL_NAME),

    /**
     * Hum branded B1
     *
     * Bluetooth Low Energy
     */
    HUM_BATTERY("Hum Battery", HUM_BATTERY_INTERNAL_NAME),

    /**
     * Glint toothbrush (BLE, OverPressure, Brushing Modes, Strength Levels)
     */
    GLINT("Glint", GLINT_INTERNAL_NAME);

    val isAra = internalName == ARA_INTERNAL_NAME

    val isConnectM1 = internalName == CONNECT_M1_INTERNAL_NAME

    /**
     * Check if this toothbrush is Colgate's Connect E1
     *
     * @return true if this [ToothbrushModel] is a Connect E1, false otherwise
     */
    val isConnectE1 = internalName == CONNECT_E1_INTERNAL_NAME

    /**
     * Check if this toothbrush is Colgate's Connect E2
     *
     * @return true if this [ToothbrushModel] is a Connect E2, false otherwise
     */
    val isConnectE2 = internalName == CONNECT_E2_INTERNAL_NAME

    /**
     * Check if this toothbrush is Colgate's Connect B1
     *
     * @return true if this [ToothbrushModel] is a Connect B1, false otherwise
     */
    val isConnectB1 = internalName == CONNECT_B1_INTERNAL_NAME

    /**
     * Check if this ToothbrushModel is a HiLink toothbrush
     */
    val isHiLink = internalName == HILINK_INTERNAL_NAME

    /**
     * Check if this toothbrush is Plaqless
     *
     * @return true if this [ToothbrushModel] is a Plaqless, false otherwise
     */
    val isPlaqless = internalName == PLAQLESS_INTERNAL_NAME

    /**
     * Check if this toothbrush is Hum Electric
     *
     * @return true if this [ToothbrushModel] is a HUM_ELECTRIC, false otherwise
     */
    val isHumElectric = internalName == HUM_ELECTRIC_INTERNAL_NAME

    /**
     * Check if this toothbrush is Hum Battery
     *
     * @return true if this [ToothbrushModel] is a HUM_BATTERY, false otherwise
     */
    val isHumBattery = internalName == HUM_BATTERY_INTERNAL_NAME

    /**
     * Check if this ToothbrushModel is a Glint toothbrush
     */
    val isGlint = internalName == GLINT_INTERNAL_NAME

    val isE1Branded = isConnectE1 || isAra

    val isE2Branded = isConnectE2 || isHiLink || isHumElectric

    val isB1Branded = isConnectB1 || isHumBattery

    val isHumToothbrush = isHumBattery || isHumElectric

    /**
     * Check if this toothbrush is manual
     *
     * @return true if this [ToothbrushModel] is a manual, false otherwise
     */
    val isManual = isConnectM1

    /** Returns true if toothbrush model supports multiple users, false otherwise  */
    val isMultiUser = !isConnectM1

    val hasDsp: Boolean = isPlaqless

    /**
     * @return true if this model supports being kept awake artificially
     */
    // See https://kolibree.atlassian.net/browse/KLTB002-8787
    val canBeKeptAwake = isE1Branded || isE2Branded || isGlint

    /**
     * Check whether the device has Mode LEDs
     */
    val hasModeLed = isGlint

    fun supportsGRUDataUpdate() = when (this) {
        CONNECT_M1,
        PLAQLESS
            -> false
        ARA,
        CONNECT_E1,
        CONNECT_E2,
        CONNECT_B1,
        HILINK,
        HUM_ELECTRIC,
        HUM_BATTERY,
        GLINT
            -> true
    }

    fun supportsDfuUpdate() = when (this) {
        ARA,
        CONNECT_E1
            -> false
        CONNECT_M1,
        CONNECT_E2,
        CONNECT_B1,
        PLAQLESS,
        HILINK,
        HUM_ELECTRIC,
        HUM_BATTERY,
        GLINT
            -> true
    }

    fun supportsVibrationSpeedUpdate() = when (this) {
        ARA,
        CONNECT_E1,
        CONNECT_M1,
        PLAQLESS
            -> false
        CONNECT_E2,
        CONNECT_B1,
        HILINK,
        HUM_ELECTRIC,
        HUM_BATTERY,
        GLINT
            -> true
    }

    fun isRechargeable(): Boolean = !isConnectM1 && !isB1Branded

    fun useNickName(): Boolean = isHiLink

    fun hasOverPressure(): Boolean = isGlint

    @Keep
    companion object {

        @JvmStatic
        fun getModelByInternalName(internalName: String?): ToothbrushModel? {
            internalName?.let {
                for (model in values()) {
                    if (internalName.equals(model.internalName, ignoreCase = true)) {
                        return model
                    }
                }
            }

            return null
        }

        @JvmStatic
        fun getModelByCommercialName(commercialName: String): ToothbrushModel? {
            for (model in values()) {
                if (commercialName == model.commercialName) {
                    return model
                }
            }

            return null
        }

        fun e2Branded(): List<ToothbrushModel> = listOf(CONNECT_E2, HILINK, HUM_ELECTRIC)

        fun b1Branded(): List<ToothbrushModel> = listOf(CONNECT_B1, HUM_BATTERY)
    }
}

private const val ARA_INTERNAL_NAME = "KLTB002"
private const val CONNECT_M1_INTERNAL_NAME = "KLTB003"
private const val CONNECT_E1_INTERNAL_NAME = "CE1"
private const val CONNECT_E2_INTERNAL_NAME = "CE2"
private const val CONNECT_B1_INTERNAL_NAME = "CB1"
private const val PLAQLESS_INTERNAL_NAME = "PLAQLESS"
private const val HILINK_INTERNAL_NAME = "HILINK"
private const val HUM_ELECTRIC_INTERNAL_NAME = "HE1"
private const val HUM_BATTERY_INTERNAL_NAME = "HB1"
private const val GLINT_INTERNAL_NAME = "GLINT"
