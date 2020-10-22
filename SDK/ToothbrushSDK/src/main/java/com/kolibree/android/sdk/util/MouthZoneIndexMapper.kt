package com.kolibree.android.sdk.util

import com.kolibree.kml.MouthZone16
import com.kolibree.kml.MouthZone16.LoIncExt
import com.kolibree.kml.MouthZone16.LoIncInt
import com.kolibree.kml.MouthZone16.LoMolLeExt
import com.kolibree.kml.MouthZone16.LoMolLeInt
import com.kolibree.kml.MouthZone16.LoMolLeOcc
import com.kolibree.kml.MouthZone16.LoMolRiExt
import com.kolibree.kml.MouthZone16.LoMolRiInt
import com.kolibree.kml.MouthZone16.LoMolRiOcc
import com.kolibree.kml.MouthZone16.UpIncExt
import com.kolibree.kml.MouthZone16.UpIncInt
import com.kolibree.kml.MouthZone16.UpMolLeExt
import com.kolibree.kml.MouthZone16.UpMolLeInt
import com.kolibree.kml.MouthZone16.UpMolLeOcc
import com.kolibree.kml.MouthZone16.UpMolRiExt
import com.kolibree.kml.MouthZone16.UpMolRiInt
import com.kolibree.kml.MouthZone16.UpMolRiOcc

/**
 * This class maps an offline brushing zone index to a [MouthZone16]
 */
internal object MouthZoneIndexMapper {

    private val mapMouthZone16Index = mapOf(
        0 to LoMolLeOcc,
        1 to LoMolLeExt,
        2 to LoMolLeInt,
        3 to LoMolRiOcc,
        4 to LoMolRiExt,
        5 to LoMolRiInt,
        6 to LoIncExt,
        7 to LoIncInt,
        8 to UpMolLeOcc,
        9 to UpMolLeExt,
        10 to UpMolLeInt,
        11 to UpMolRiOcc,
        12 to UpMolRiExt,
        13 to UpMolRiInt,
        14 to UpIncExt,
        15 to UpIncInt
    )

    private val mapIndexMouthZone16 = mapMouthZone16Index
        .map { entry -> entry.value to entry.key.toByte() }
        .toMap()

    /**
     * Map a hardware zone index to a MouthZone16
     * The map that the brushes are using can be found in the document below
     * https://docs.google.com/document/d/1n5b8xPcIhNvaraIVmipXMUWI_JBFVuFU1TM63zbGphU/edit#heading=h.m698t4fbqg84
     *
     * @param storedMouthZoneId stored brushing hardware zone index
     * @return non null [MouthZone16]
     */
    @JvmStatic
    fun mapZoneIdToMouthZone16(storedMouthZoneId: Int): MouthZone16 =
        mapMouthZone16Index[storedMouthZoneId] ?: throw IllegalArgumentException("Invalid zone id: $storedMouthZoneId")

    /**
     * Map a MouthZone16 to hardware zone index
     * The map that the brushes are using can be found in the document below
     * https://docs.google.com/document/d/1n5b8xPcIhNvaraIVmipXMUWI_JBFVuFU1TM63zbGphU/edit#heading=h.m698t4fbqg84
     *
     * @param zone
     * @return non null [Byte]
     */
    @JvmStatic
    fun mapMouthZone16ToId(zone: MouthZone16): Byte =
        mapIndexMouthZone16[zone] ?: throw IllegalArgumentException("Invalid zone")
}
