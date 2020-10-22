/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.database

import androidx.annotation.Keep
import androidx.room.TypeConverter
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.UploadStatus
import java.util.UUID

internal class UploadStatusConverters {

    @TypeConverter
    fun fromUploadStatus(value: String): UploadStatus {
        return UploadStatus.uploadStatusFromStringedValue(value)
    }

    @TypeConverter
    fun toUploadStatus(value: UploadStatus): String {
        return value.stringify()
    }
}

@Keep
class UuidConverters {

    @TypeConverter
    fun fromUUID(value: String?): UUID? {
        if (value.isNullOrBlank()) return null

        return UUID.fromString(value)
    }

    @TypeConverter
    fun toUUIDString(value: UUID?): String? {
        return value?.toString()
    }
}

internal class SynchronizableKeyConverter {
    @TypeConverter
    fun fromSynchronizableKey(value: String): SynchronizableKey {
        return SynchronizableKey.from(value)!!
    }

    @TypeConverter
    fun toSynchronizableKey(synchronizableKey: SynchronizableKey): String {
        return synchronizableKey.value
    }
}
