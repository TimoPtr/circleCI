package com.kolibree.android.sdk.persistence.room

import androidx.room.TypeConverter
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion

/**
 * Created by guillaumeagis on 21/05/2018.
 */

class AccountToothbrushConverters {

    /**
     * Convert a ToothbrushModel to it's internal name
     * */
    @TypeConverter
    fun fromToothbrushModel(toothbrushModel: ToothbrushModel): String = toothbrushModel.internalName

    /**
     * Convert the ToothbrushModel internal name to a ToothbrushModel object
     */
    @TypeConverter
    fun toToothbrushModel(internalName: String): ToothbrushModel? =
        ToothbrushModel.getModelByInternalName(internalName)

    /**
     * Convert a HardwareVersion to its binary
     * */
    @TypeConverter
    fun fromHardwareVersion(version: HardwareVersion): Long = version.toBinary()

    /**
     * Convert the HardwareVersion from its binary
     */
    @TypeConverter
    fun toHardwareVersion(binary: Long): HardwareVersion = HardwareVersion(binary)

    /**
     * Convert a SoftwareVersion to its binary
     * */
    @TypeConverter
    fun fromSoftwareVersion(version: SoftwareVersion): Long = version.toBinary()

    /**
     * Convert the SoftwareVersion from its binary
     */
    @TypeConverter
    fun toSoftwareVersion(binary: Long): SoftwareVersion = SoftwareVersion(binary)

    /**
     * Convert a DspVersion to its binary
     * */
    @TypeConverter
    fun fromDspVersion(version: DspVersion): Long = version.toBinary()

    /**
     * Convert the DspVersion from its binary
     */
    @TypeConverter
    fun toDspVersion(binary: Long): DspVersion = DspVersion(binary)
}
