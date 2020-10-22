/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons

import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.Companion.getModelByInternalName
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.HUM_BATTERY
import com.kolibree.android.commons.ToothbrushModel.HUM_ELECTRIC
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.commons.ToothbrushModel.values
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Created by miguelaragues on 15/9/17.  */
class ToothbrushModelTest {

    /*
    isGlint
     */

    @Test
    fun `isGlint returns true for GLINT model`() {
        assertTrue(GLINT.isGlint)
    }

    @Test
    fun `isGLint returns false for every other models`() {
        values()
            .filterNot { it == GLINT }
            .forEach { assertFalse(it.isGlint) }
    }

    /*
    isMultiUser
     */

    @Test
    fun m1_isNotMultiUser() {
        assertFalse(CONNECT_M1.isMultiUser)
    }

    @Test
    fun allOtherModelsAreMultiUser() {
        values()
            .filterNot { it.isConnectM1 }
            .forEach { model ->
                assertTrue("Failed for $model", model.isMultiUser)
            }
    }

    /*
  SUPPORTS GRU DATA UPDATE
   */

    @Test
    fun m1_doesNotNeedGRUUpdates() {
        assertFalse(CONNECT_M1.supportsGRUDataUpdate())
    }

    @Test
    fun pql_doesNotNeedGRUUpdates() {
        assertFalse(PLAQLESS.supportsGRUDataUpdate())
    }

    @Test
    fun allOtherModels_supportsGRUDataUpdate() {
        values()
            .filterNot { it.isConnectM1 || it.isPlaqless }
            .forEach { model ->
                assertTrue("Failed for $model", model.supportsGRUDataUpdate())
            }
    }

    /*
  SUPPORTS DFU
   */

    @Test
    fun supportsDfuUpdate_araDoesNot() {
        assertFalse(ARA.supportsDfuUpdate())
    }

    @Test
    fun supportsDfuUpdate_e1DoesNot() {
        assertFalse(CONNECT_E1.supportsDfuUpdate())
    }

    @Test
    fun allOtherModels_supportDfuUpdate() {
        values()
            .filterNot { it.isAra || it.isConnectE1 }
            .forEach { model ->
                assertTrue("Failed for $model", model.supportsDfuUpdate())
            }
    }

    /*
  IS MANUAL
   */

    @Test
    fun isManual_CONNECT_M1_returnsTrue() {
        assertTrue(CONNECT_M1.isManual)
    }

    @Test
    fun allOtherModels_isManualReturnsFalse() {
        values()
            .filterNot { it.isConnectM1 }
            .forEach { model ->
                assertFalse("Failed for $model", model.isManual)
            }
    }

    /*
  GET MODEL BY INTERNAL NAME
   */

    @Test
    fun getModelByInternalName_isCaseInsensitive() {
        for (model in values()) {
            val internalName = model.internalName
            assertEquals(model, getModelByInternalName(internalName))
            assertEquals(model, getModelByInternalName(internalName.toLowerCase()))
        }
    }

    /*
  supportsVibrationSpeedUpdate
   */

    @Test
    fun ara_doesNotSupportVibrationSpeedUpdate() {
        assertFalse(ARA.supportsVibrationSpeedUpdate())
    }

    @Test
    fun e1_doesNotSupportVibrationSpeedUpdate() {
        assertFalse(CONNECT_E1.supportsVibrationSpeedUpdate())
    }

    @Test
    fun m1_doesNotSupportVibrationSpeedUpdate() {
        assertFalse(CONNECT_M1.supportsVibrationSpeedUpdate())
    }

    @Test
    fun plaqless_doesNotSupportVibrationSpeedUpdate() {
        assertFalse(PLAQLESS.supportsVibrationSpeedUpdate())
    }

    @Test
    fun allOtherModels_supportVibrationSpeedUpdate() {
        values()
            .filterNot { it.isAra || it.isConnectM1 || it.isConnectE1 || it.isPlaqless }
            .forEach { model ->
                assertTrue("Failed for $model", model.supportsVibrationSpeedUpdate())
            }
    }

    /*
  hasDsp
   */

    @Test
    fun plaqless_hasDsp() {
        assertTrue(PLAQLESS.hasDsp)
    }

    @Test
    fun allOtherModels_dontHaveDSP() {
        values()
            .filterNot { it.isPlaqless }
            .forEach { model ->
                assertFalse("Failed for $model", model.hasDsp)
            }
    }

    /*
  canBeKeptAwake
   */
    @Test
    fun `E1, E2, HiLink, Hum Electric and Glint return true to canBeKeptAwake`() {
        values()
            .filter { it.isE1Branded || it.isE2Branded || it.isGlint }
            .forEach { assertTrue("Failed for $it", it.canBeKeptAwake) }
    }

    @Test
    fun `All models except E1, E2 and HiLink and Hum Electric can't be kept awake`() {
        values()
            .filterNot { it.isE1Branded || it.isE2Branded || it.isGlint }
            .forEach { assertFalse("Failed for $it", it.canBeKeptAwake) }
    }

    /*
    isRechargeable
     */
    @Test
    fun `M1 return false to isRechargeable`() {
        assertFalse(CONNECT_M1.isRechargeable())
    }

    @Test
    fun `Hum Battery return false to isRechargeable`() {
        assertFalse(HUM_BATTERY.isRechargeable())
    }

    @Test
    fun `B1 return false to isRechargeable`() {
        assertFalse(ToothbrushModel.CONNECT_B1.isRechargeable())
    }

    @Test
    fun `All models except M1, B1 and Hum Battery return true to isRechargeable`() {
        values()
            .filterNot { it == CONNECT_M1 || it.isB1Branded }
            .forEach {
                assertTrue("Failed for $it", it.isRechargeable())
            }
    }

    /*
    useNickName
     */
    @Test
    fun `HiLink return true to useNickName`() {
        assertTrue(HILINK.useNickName())
    }

    @Test
    fun `All models except HiLink return false to useNickName`() {
        values()
            .filterNot { it == HILINK }
            .forEach {
                assertFalse("Failed for $it", it.useNickName())
            }
    }

    /*
    hasOverPressure
     */
    @Test
    fun `Glint return true to hasOverPressure`() {
        assertTrue(GLINT.hasOverPressure())
    }

    @Test
    fun `All models except Glint return false to hasOverPressure`() {
        values()
            .filterNot { it == GLINT }
            .forEach {
                assertFalse("Failed for $it", it.hasOverPressure())
            }
    }

    /*
    HiLink
     */

    @Test
    fun `hiLink's commercial name is HiLink`() {
        assertEquals("HiLink", HILINK.commercialName)
    }

    @Test
    fun `hiLink's internal name is HILINK`() {
        assertEquals("HILINK", HILINK.internalName)
    }

    @Test
    fun `isHiLink returns true for HILINK model`() {
        assertTrue(HILINK.isHiLink)
    }

    @Test
    fun `isHiLink returns false for every other models`() {
        values()
            .filterNot { it == HILINK }
            .forEach { assertFalse(it.isHiLink) }
    }

    @Test
    fun `hiLink is not manual`() {
        assertFalse(HILINK.isManual)
    }

    @Test
    fun `hiLink is multi-user`() {
        assertTrue(HILINK.isMultiUser)
    }

    @Test
    fun `hiLink does not have a DSP`() {
        assertFalse(HILINK.hasDsp)
    }

    @Test
    fun `hiLink can be kept awake`() {
        assertTrue(HILINK.canBeKeptAwake)
    }

    @Test
    fun `hiLink supports DFU updates`() {
        assertTrue(HILINK.supportsDfuUpdate())
    }

    @Test
    fun `hiLink supports vibration speed feature`() {
        assertTrue(HILINK.supportsVibrationSpeedUpdate())
    }

    @Test
    fun `hiLink is rechargeable`() {
        assertTrue(HILINK.isRechargeable())
    }

    /*
  isE1Branded
   */

    @Test
    fun `E1 and Ara return true to isE1Branded`() {
        values()
            .filter { it == CONNECT_E1 || it == ARA }
            .forEach {
                assertTrue("Failed for $it", it.isE1Branded)
            }
    }

    @Test
    fun `All models except E1 and Ara return false to isE1Branded`() {
        values()
            .filterNot { it == CONNECT_E1 || it == ARA }
            .forEach {
                assertFalse("Failed for $it", it.isE1Branded)
            }
    }

    /*
  isE2Branded
   */

    @Test
    fun `E2, HiLink and HumElectric return true to isE2Branded`() {
        values()
            .filter { it == CONNECT_E2 || it == HILINK || it == HUM_ELECTRIC }
            .forEach {
                assertTrue("Failed for $it", it.isE2Branded)
            }
    }

    @Test
    fun `All models except E2, HiLink and HumElectric return false to isE2Branded`() {
        values()
            .filterNot { it == CONNECT_E2 || it == HILINK || it == HUM_ELECTRIC }
            .forEach {
                assertFalse("Failed for $it", it.isE2Branded)
            }
    }

    /*
  isHumToothbrush
   */

    @Test
    fun `HUM_ELECTRIC and HUM_BATTERY return true to isHumToothbrush`() {
        values()
            .filter { it == HUM_BATTERY || it == HUM_ELECTRIC }
            .forEach {
                assertTrue("Failed for $it", it.isHumToothbrush)
            }
    }

    @Test
    fun `All models except E2, HiLink and HumElectric return false to isHumToothbrush`() {
        values()
            .filterNot { it == HUM_BATTERY || it == HUM_ELECTRIC }
            .forEach {
                assertFalse("Failed for $it", it.isHumToothbrush)
            }
    }

    /*
    hasModeLed
     */

    @Test
    fun `hasModeLed is true for Glint`() {
        assertTrue(GLINT.hasModeLed)
    }

    @Test
    fun `hasModeLed is false for every model but Glint`() {
        values()
            .filterNot { it == GLINT }
            .forEach { assertFalse(it.hasModeLed) }
    }
}
