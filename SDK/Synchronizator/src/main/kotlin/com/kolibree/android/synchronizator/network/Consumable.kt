package com.kolibree.android.synchronizator.network

/**
 * Descendants of Consumable hold the deltas between the version specified in a SynchronizeAccountRequest for each
 * SynchronizableKey and the latest version the backend had at the instant of the request
 *
 * This information is only valid in the context of one SynchronizeAccountResponse
 */

internal data class Consumable(
    val version: Int,
    val updatedIds: List<Long> = listOf(),
    val deletedIds: List<Long> = listOf()
)
