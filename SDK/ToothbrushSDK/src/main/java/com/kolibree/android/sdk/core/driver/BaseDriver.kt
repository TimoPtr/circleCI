package com.kolibree.android.sdk.core.driver

import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion

/** Base driver for all Kolibree-produced toothbrushes  */
internal interface BaseDriver : BaseInternalDriver {

    /**
     * Get toothbrush hardware version
     *
     * @return non null [HardwareVersion]
     */
    fun getHardwareVersion(): HardwareVersion

    /**
     * Get firmware version
     *
     * @return non null firmware [SoftwareVersion] (Major.minor.revision)
     */
    fun getFirmwareVersion(): SoftwareVersion

    /**
     * Get bootloader version
     *
     * @return non null bootloader [SoftwareVersion] (Major.minor.revision)
     */
    fun getBootloaderVersion(): SoftwareVersion

    /**
     * Get dsp version
     *
     * @return non null dsp [DspVersion] (Major.minor.revision)
     */
    fun getDspVersion(): DspVersion
}
