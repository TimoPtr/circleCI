/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.toothbrush

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.KLTBConnection
import java.util.Objects

/**
 * Events a [KLTBConnection] can emit during a Brushing Session
 *
 * Two brushes will never take part of the same brushing session. Thus, [connection] instance is
 * guaranteed to be the same for all events in the same session.
 */
@Keep
sealed class GameToothbrushEvent(val connection: KLTBConnection) {

    /**
     * [connection] is no longer [ACTIVE]
     *
     * This can be followed by either a [ConnectionActive] or [ConnectionEstablished] event
     */
    class ConnectionLost(connection: KLTBConnection) : GameToothbrushEvent(connection) {
        override fun equals(other: Any?): Boolean {
            return innerEquals(other)
        }

        override fun hashCode(): Int {
            return innerHashcode()
        }
    }

    /**
     * A connection has been established for the first time
     *
     * If the [connection] is [ACTIVE], this event will be followed by [ConnectionActive] event
     *
     * If the [connection] state is no longer [ACTIVE], this event will be followed by a [ConnectionLost] event
     */
    class ConnectionEstablished(connection: KLTBConnection) : GameToothbrushEvent(connection) {
        override fun equals(other: Any?): Boolean {
            return innerEquals(other)
        }

        override fun hashCode(): Int {
            return innerHashcode()
        }
    }

    /**
     * [connection] is in [ACTIVE] state
     *
     * If the [connection] is vibrating, this event will be immediately followed by [VibratorOn] event
     *
     * A [VibratorOn] event will follow if the toothbrush starts vibrating at any time in the future
     *
     * If the [connection] state is no longer [ACTIVE], this event will be followed by a [ConnectionLost] event
     */
    class ConnectionActive(connection: KLTBConnection) : GameToothbrushEvent(connection) {
        override fun equals(other: Any?): Boolean {
            return innerEquals(other)
        }

        override fun hashCode(): Int {
            return innerHashcode()
        }
    }

    /**
     * [connection] is vibrating
     *
     * A [VibratorOff] event will follow if the toothbrush stops vibrating
     *
     * If the [connection] state is no longer [ACTIVE], this event will be followed by a [ConnectionLost] event
     */
    class VibratorOn(connection: KLTBConnection) : GameToothbrushEvent(connection) {
        override fun equals(other: Any?): Boolean {
            return innerEquals(other)
        }

        override fun hashCode(): Int {
            return innerHashcode()
        }
    }

    /**
     * [connection] is not vibrating
     *
     * A [VibratorOn] event will follow if the toothbrush starts vibrating
     *
     * If the [connection] state is no longer [ACTIVE], this event will be followed by a [ConnectionLost] event
     */
    class VibratorOff(connection: KLTBConnection) : GameToothbrushEvent(connection) {
        override fun equals(other: Any?): Boolean {
            return innerEquals(other)
        }

        override fun hashCode(): Int {
            return innerHashcode()
        }
    }
}

private fun GameToothbrushEvent.equalsConnection(connection: KLTBConnection): Boolean {
    return this.connection.toothbrush().mac == connection.toothbrush().mac
}

private inline fun <reified T : GameToothbrushEvent> T.innerHashcode(): Int {
    return Objects.hash(javaClass, connection.toothbrush().mac)
}

private inline fun <reified T : GameToothbrushEvent> T.innerEquals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is T) return false

    return equalsConnection(other.connection)
}
