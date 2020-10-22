/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.toothbrush

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.ConnectionActive
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.ConnectionEstablished
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.ConnectionLost
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.VibratorOff
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.VibratorOn
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("ReplaceCallWithBinaryOperator")
internal class GameToothbrushEventTest : BaseUnitTest() {
    /*
    ConnectionActive
     */
    @Test
    fun `ConnectionEstablished equals returns true if same connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertEquals(
            ConnectionEstablished(connection),
            ConnectionEstablished(connection)
        )
    }

    @Test
    fun `ConnectionEstablished equals returns false if same connection but different event`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertFalse(
            ConnectionEstablished(connection).equals(ConnectionLost(connection))
        )
    }

    @Test
    fun `ConnectionEstablished equals returns true if different connection but same mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertTrue(
            ConnectionEstablished(connection1) == ConnectionEstablished(connection2)
        )
    }

    @Test
    fun `ConnectionEstablished equals returns false if different connection and different mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("lala")
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("diff")
            .build()

        assertFalse(
            ConnectionEstablished(connection1) == ConnectionEstablished(connection2)
        )
    }

    @Test
    fun `ConnectionEstablished hashCode returns same value if same connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertEquals(
            ConnectionEstablished(connection).hashCode(),
            ConnectionEstablished(connection).hashCode()
        )
    }

    @Test
    fun `ConnectionEstablished hashCode returns same value if different connection but same mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertTrue(
            ConnectionEstablished(connection1).hashCode() == ConnectionEstablished(connection2).hashCode()
        )
    }

    @Test
    fun `ConnectionEstablished hashCode returns false if different connection and different mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("lala")
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("diff")
            .build()

        assertFalse(
            ConnectionEstablished(connection1).hashCode() == ConnectionEstablished(connection2).hashCode()
        )
    }

    /*
    ConnectionActive
     */
    @Test
    fun `equals returns true if same connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertEquals(
            ConnectionActive(connection),
            ConnectionActive(connection)
        )
    }

    @Test
    fun `equals returns false if same connection but different event`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertFalse(
            ConnectionActive(connection).equals(ConnectionLost(connection))
        )
    }

    @Test
    fun `equals returns true if different connection but same mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertTrue(
            ConnectionActive(connection1) == ConnectionActive(connection2)
        )
    }

    @Test
    fun `equals returns false if different connection and different mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("lala")
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("diff")
            .build()

        assertFalse(
            ConnectionActive(connection1) == ConnectionActive(connection2)
        )
    }

    @Test
    fun `hashCode returns same value if same connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertEquals(
            ConnectionActive(connection).hashCode(),
            ConnectionActive(connection).hashCode()
        )
    }

    @Test
    fun `hashCode returns same value if different connection but same mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertTrue(
            ConnectionActive(connection1).hashCode() == ConnectionActive(connection2).hashCode()
        )
    }

    @Test
    fun `hashCode returns false if different connection and different mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("lala")
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("diff")
            .build()

        assertFalse(
            ConnectionActive(connection1).hashCode() == ConnectionActive(connection2).hashCode()
        )
    }

    /*
    ConnectionLost
     */
    @Test
    fun `ConnectionLost equals returns true if same connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertEquals(
            ConnectionLost(connection),
            ConnectionLost(connection)
        )
    }

    @Test
    fun `ConnectionLost equals returns false if same connection but different event`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertFalse(
            ConnectionLost(connection).equals(ConnectionActive(connection))
        )
    }

    @Test
    fun `ConnectionLost equals returns true if different connection but same mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertTrue(
            ConnectionLost(connection1) == ConnectionLost(connection2)
        )
    }

    @Test
    fun `ConnectionLost equals returns false if different connection and different mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("lala")
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("diff")
            .build()

        assertFalse(
            ConnectionLost(connection1) == ConnectionLost(connection2)
        )
    }

    @Test
    fun `ConnectionLost hashCode returns same value if same connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertEquals(
            ConnectionLost(connection).hashCode(),
            ConnectionLost(connection).hashCode()
        )
    }

    @Test
    fun `ConnectionLost hashCode returns same value if different connection but same mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertTrue(
            ConnectionLost(connection1).hashCode() == ConnectionLost(connection2).hashCode()
        )
    }

    @Test
    fun `ConnectionLost hashCode returns different value if same connection but different event`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertFalse(
            ConnectionLost(connection1).hashCode().equals(VibratorOff(connection2).hashCode())
        )
    }

    @Test
    fun `ConnectionLost hashCode returns different if different connection and different mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("lala")
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("diff")
            .build()

        assertFalse(
            ConnectionLost(connection1).hashCode() == ConnectionLost(connection2).hashCode()
        )
    }

    /*
    VibratorOff
     */
    @Test
    fun `VibratorOff equals returns true if same connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertEquals(
            VibratorOff(connection),
            VibratorOff(connection)
        )
    }

    @Test
    fun `VibratorOff equals returns false if same connection but different event`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertFalse(
            VibratorOff(connection).equals(ConnectionActive(connection))
        )
    }

    @Test
    fun `VibratorOff equals returns true if different connection but same mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertTrue(
            VibratorOff(connection1) == VibratorOff(
                connection2
            )
        )
    }

    @Test
    fun `VibratorOff equals returns false if different connection and different mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("lala")
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("diff")
            .build()

        assertFalse(
            VibratorOff(connection1) == VibratorOff(connection2)
        )
    }

    @Test
    fun `VibratorOff hashCode returns same value if same connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertEquals(
            VibratorOff(connection).hashCode(),
            VibratorOff(connection).hashCode()
        )
    }

    @Test
    fun `VibratorOff hashCode returns same value if different connection but same mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertTrue(
            VibratorOff(connection1).hashCode() == VibratorOff(connection2).hashCode()
        )
    }

    @Test
    fun `VibratorOff hashCode returns different value if same connection but different event`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertFalse(
            VibratorOff(connection1).hashCode().equals(ConnectionLost(connection2).hashCode())
        )
    }

    @Test
    fun `VibratorOff hashCode returns different if different connection and different mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("lala")
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("diff")
            .build()

        assertFalse(
            VibratorOff(connection1).hashCode() == VibratorOff(connection2).hashCode()
        )
    }

    /*
    VibratorOn
     */
    @Test
    fun `VibratorOn equals returns true if same connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertEquals(
            VibratorOn(connection),
            VibratorOn(connection)
        )
    }

    @Test
    fun `VibratorOn equals returns false if same connection but different event`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertFalse(
            VibratorOn(connection).equals(ConnectionActive(connection))
        )
    }

    @Test
    fun `VibratorOn equals returns true if different connection but same mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertTrue(
            VibratorOn(connection1) == VibratorOn(
                connection2
            )
        )
    }

    @Test
    fun `VibratorOn equals returns false if different connection and different mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("lala")
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("diff")
            .build()

        assertFalse(
            VibratorOn(connection1) == VibratorOn(connection2)
        )
    }

    @Test
    fun `VibratorOn hashCode returns same value if same connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertEquals(
            VibratorOn(connection).hashCode(),
            VibratorOn(connection).hashCode()
        )
    }

    @Test
    fun `VibratorOn hashCode returns same value if different connection but same mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertTrue(
            VibratorOn(connection1).hashCode() == VibratorOn(connection2).hashCode()
        )
    }

    @Test
    fun `VibratorOn hashCode returns different value if same connection but different event`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()

        assertFalse(
            VibratorOn(connection1).hashCode().equals(ConnectionLost(connection2).hashCode())
        )
    }

    @Test
    fun `VibratorOn hashCode returns different if different connection and different mac`() {
        val connection1 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("lala")
            .build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("diff")
            .build()

        assertFalse(
            VibratorOn(connection1).hashCode() == VibratorOn(connection2).hashCode()
        )
    }
}
