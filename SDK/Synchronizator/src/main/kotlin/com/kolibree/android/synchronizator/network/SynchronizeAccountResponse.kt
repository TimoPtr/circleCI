package com.kolibree.android.synchronizator.network

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.synchronizator.models.SynchronizableKey

/**
 * Holds a Map of SynchronizableKey and Consumable
 *
 * The size of this Map is at most the size of the Set<SynchronizeAccountKey> in the SynchronizeAccountRequest, but
 * could be smaller. If the version held by the backend is the same as our local version, the response won't include
 * that key
 *
 * See https://confluence.kolibree.com/pages/viewpage.action?spaceKey=SOF&title=Synchronization+support
 *
 */
@JsonAdapter(SynchronizeAccountResponseTypeAdapter::class)
internal data class SynchronizeAccountResponse(val typeConsumables: Map<SynchronizableKey, Consumable>)

/**
 * Maps the json response from the server to a SynchronzeAccountResponse
 */
internal class SynchronizeAccountResponseTypeAdapter : TypeAdapter<SynchronizeAccountResponse>() {
    override fun write(out: JsonWriter, value: SynchronizeAccountResponse) {
        // no need to implement it
    }

    override fun read(reader: JsonReader): SynchronizeAccountResponse {
        val typeConsumables: MutableMap<SynchronizableKey, Consumable> = mutableMapOf()

        reader.beginObject()

        while (reader.peek() == JsonToken.NAME) {
            val keyName = reader.nextName()
            val key = SynchronizableKey.from(keyName)

            reader.beginObject()

            val consumable = createConsumable(reader)

            reader.endObject()

            if (key != null) {
                typeConsumables[key] = consumable
            } else {
                FailEarly.fail("Unknown key $keyName with body $consumable")
            }
        }

        reader.endObject()

        return SynchronizeAccountResponse(typeConsumables.toMap())
    }

    private fun createConsumable(reader: JsonReader): Consumable {
        var version = 0
        val updatedIds = mutableListOf<Long>()
        val deletedIds = mutableListOf<Long>()

        var nextName = reader.nextName()
        do {
            when (nextName) {
                UPDATED_IDS_KEY, UPDATED_PROFILE_IDS_KEY -> {
                    updatedIds += createListIds(reader)
                }
                DELETED_IDS_KEY, DELETED_PROFILE_IDS_KEY -> {
                    deletedIds += createListIds(reader)
                }
                VERSION_KEY -> {
                    version = reader.nextInt()
                }
            }

            nextName = tryNextName(reader)
        } while (nextName.isNotEmpty())

        return Consumable(version, updatedIds, deletedIds)
    }

    private fun tryNextName(reader: JsonReader): String = try {
        reader.nextName()
    } catch (_: Exception) {
        ""
    }

    private fun createListIds(reader: JsonReader): List<Long> = try {
        extractListOfLong(reader)
    } catch (_: Exception) {
        emptyList()
    }

    private fun extractListOfLong(reader: JsonReader): MutableList<Long> {
        reader.beginArray()
        val longList = mutableListOf<Long>()
        while (reader.hasNext()) {
            longList.add(reader.nextLong())
        }
        reader.endArray()

        return longList
    }
}

private const val UPDATED_IDS_KEY = "updated_ids"
private const val UPDATED_PROFILE_IDS_KEY = "updated_profile_ids"
private const val DELETED_IDS_KEY = "deleted_ids"
private const val DELETED_PROFILE_IDS_KEY = "deleted_profile_ids"
private const val VERSION_KEY = "version"
