package com.kolibree.android.sdk.persistence.model

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.kolibree.android.SHARED_MODE_PROFILE_ID
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.persistence.model.AccountToothbrush.Companion.TABLE_NAME
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion

@Keep
@Entity(tableName = TABLE_NAME)
data class AccountToothbrush(
    @PrimaryKey
    @ColumnInfo(name = "mac") val mac: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "model") val model: ToothbrushModel,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "profile_id") val profileId: Long = 0,
    @ColumnInfo(name = "serial") val serial: String = "",
    @ColumnInfo(name = "hardware_version") val hardwareVersion: HardwareVersion = HardwareVersion.NULL,
    @ColumnInfo(name = "firmware_version") val firmwareVersion: SoftwareVersion = SoftwareVersion.NULL,
    @ColumnInfo(
        name = "bootloader_version",
        defaultValue = "0"
    ) val bootloaderVersion: SoftwareVersion = SoftwareVersion.NULL,
    @ColumnInfo(
        name = "dsp_version",
        defaultValue = "0"
    ) val dspVersion: DspVersion = DspVersion.NULL,
    /**
     * true if local data must be sync with backend
     */
    @ColumnInfo(
        name = "dirty",
        defaultValue = "0"
    ) val dirty: Boolean = false
) {
    @Ignore
    constructor(mac: String, name: String, model: ToothbrushModel, accountId: Long) : this(
        mac,
        name,
        model,
        accountId,
        0
    )

    @Ignore
    val isSharedToothbrush = profileId == SHARED_MODE_PROFILE_ID

    internal companion object {
        @VisibleForTesting
        internal const val TABLE_NAME = "account_tootbrushes"
    }
}
