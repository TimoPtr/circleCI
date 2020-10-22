/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.battery

import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [BatteryImplFactory] tests */
class BatteryImplFactoryTest {

    private val factory = BatteryImplFactory()

    @Test
    fun `createBatteryImplementation() returns a RechargeableBatteryImpl for CE1`() {
        assertTrue(factory.createBatteryImplementation(CONNECT_E1, mock())
            is RechargeableBatteryImpl)
    }

    @Test
    fun `createBatteryImplementation() returns a RechargeableBatteryImpl for CE2`() {
        assertTrue(factory.createBatteryImplementation(CONNECT_E2, mock())
            is RechargeableBatteryImpl)
    }

    @Test
    fun `createBatteryImplementation() returns a RechargeableBatteryImpl for HiLink`() {
        assertTrue(factory.createBatteryImplementation(HILINK, mock())
            is RechargeableBatteryImpl)
    }

    @Test
    fun `createBatteryImplementation() returns a RechargeableBatteryImpl for Ara`() {
        assertTrue(factory.createBatteryImplementation(ARA, mock())
            is RechargeableBatteryImpl)
    }

    @Test
    fun `createBatteryImplementation() returns a RechargeableBatteryImpl for Glint`() {
        assertTrue(factory.createBatteryImplementation(GLINT, mock())
            is RechargeableBatteryImpl)
    }

    @Test
    fun `createBatteryImplementation() returns a ReplaceableBatteryImpl for CM1`() {
        assertTrue(factory.createBatteryImplementation(CONNECT_M1, mock())
            is ReplaceableBatteryImpl)
    }

    @Test
    fun `createBatteryImplementation() returns a ReplaceableBatteryImpl for CB1`() {
        assertTrue(factory.createBatteryImplementation(CONNECT_B1, mock())
            is ReplaceableBatteryImpl)
    }

    @Test
    fun `createBatteryImplementation() returns a RechargeableBatteryImpl for PQL`() {
        assertTrue(factory.createBatteryImplementation(PLAQLESS, mock())
            is RechargeableBatteryImpl)
    }

    @Test
    fun `usesCompatPayload() returns true for CE1`() {
        assertTrue(factory.usesCompatPayload(CONNECT_E1))
    }

    @Test
    fun `usesCompatPayload() returns true for Ara`() {
        assertTrue(factory.usesCompatPayload(ARA))
    }

    @Test
    fun `usesCompatPayload() returns false for CM1`() {
        assertFalse(factory.usesCompatPayload(CONNECT_M1))
    }

    @Test
    fun `usesCompatPayload() returns false for CE2`() {
        assertFalse(factory.usesCompatPayload(CONNECT_E2))
    }

    @Test
    fun `usesCompatPayload() returns false for CB1`() {
        assertFalse(factory.usesCompatPayload(CONNECT_B1))
    }

    @Test
    fun `usesCompatPayload() returns false for PQL`() {
        assertFalse(factory.usesCompatPayload(PLAQLESS))
    }
}
