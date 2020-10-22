/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.logic.userproperties

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import junit.framework.TestCase.assertEquals
import org.junit.Test

class UserPropertiesFactoryMapperTest : BaseUnitTest() {

    @Test
    fun map_ARA_equalsAra() {
        assertEquals("Ara", UserPropertiesFactory.getToothbrushModel(ToothbrushModel.ARA))
    }

    @Test
    fun map_CONNECT_E1_equalsE1() {
        assertEquals("E1", UserPropertiesFactory.getToothbrushModel(ToothbrushModel.CONNECT_E1))
    }

    @Test
    fun map_CONNECT_M1_equalsM1() {
        assertEquals("M1", UserPropertiesFactory.getToothbrushModel(ToothbrushModel.CONNECT_M1))
    }

    @Test
    fun map_CONNECT_E2_equalsE2() {
        assertEquals("E2", UserPropertiesFactory.getToothbrushModel(ToothbrushModel.CONNECT_E2))
    }

    @Test
    fun map_CONNECT_B1_equalsB1() {
        assertEquals("B1", UserPropertiesFactory.getToothbrushModel(ToothbrushModel.CONNECT_B1))
    }

    @Test
    fun map_PLAQLESS_equalsPQL() {
        assertEquals("PQL", UserPropertiesFactory.getToothbrushModel(ToothbrushModel.PLAQLESS))
    }
}
