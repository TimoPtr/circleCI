package com.kolibree.android.synchronizator.network

import com.kolibree.android.synchronizator.models.SynchronizeAccountKey

/**
 *
 * See https://confluence.kolibree.com/display/SOF/Synchronization+support
 */
internal data class SynchronizeAccountRequestBody(private val synchronizeAccountKeys: Set<SynchronizeAccountKey>) {
    fun toMap(): Map<String, Int> {
        return synchronizeAccountKeys.associate { it.key.value to it.version }
    }
}
