/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons.profile

import com.kolibree.android.commons.profile.SourceApplication.COLGATE_CONNECT
import com.kolibree.android.commons.profile.SourceApplication.Companion.findBySerializedName
import com.kolibree.android.commons.profile.SourceApplication.DATAPP
import com.kolibree.android.commons.profile.SourceApplication.HUM
import com.kolibree.android.commons.profile.SourceApplication.KOLIBREE
import com.kolibree.android.commons.profile.SourceApplication.LEGACY
import com.kolibree.android.commons.profile.SourceApplication.MAGIK
import com.kolibree.android.commons.profile.SourceApplication.UNKNOWN
import junit.framework.TestCase.assertEquals
import org.junit.Test

class SourceApplicationTest {

    @Test
    fun `getSerializedName returns correct string for Application enums`() {
        assertEquals(COLGATE_CONNECT.serializedName, SOURCE_APPLICATION_COLGATE_CONNECT)
        assertEquals(DATAPP.serializedName, SOURCE_APPLICATION_DATAPP)
        assertEquals(KOLIBREE.serializedName, SOURCE_APPLICATION_KOLIBREE)
        assertEquals(LEGACY.serializedName, SOURCE_APPLICATION_LEGACY)
        assertEquals(MAGIK.serializedName, SOURCE_APPLICATION_MAGIK)
        assertEquals(HUM.serializedName, SOURCE_APPLICATION_HUM)
        assertEquals(UNKNOWN.serializedName, SOURCE_APPLICATION_UNKNOWN)
    }

    @Test
    fun `findBySerializedName with correct serialized names returns correct enums`() {
        assertEquals(findBySerializedName(SOURCE_APPLICATION_COLGATE_CONNECT), COLGATE_CONNECT)
        assertEquals(findBySerializedName(SOURCE_APPLICATION_DATAPP), DATAPP)
        assertEquals(findBySerializedName(SOURCE_APPLICATION_KOLIBREE), KOLIBREE)
        assertEquals(findBySerializedName(SOURCE_APPLICATION_LEGACY), LEGACY)
        assertEquals(findBySerializedName(SOURCE_APPLICATION_MAGIK), MAGIK)
        assertEquals(findBySerializedName(SOURCE_APPLICATION_HUM), HUM)
        assertEquals(findBySerializedName(SOURCE_APPLICATION_UNKNOWN), UNKNOWN)
    }

    @Test
    fun `findBySerializedName with null, empty or invalid serialized names returns UNKNOWN`() {
        assertEquals(findBySerializedName(null), UNKNOWN)
        assertEquals(findBySerializedName(""), UNKNOWN)
        assertEquals(findBySerializedName("invalid name"), UNKNOWN)
    }
}
