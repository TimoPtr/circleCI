/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.bi

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import junit.framework.TestCase
import org.junit.Test

class KmlAvroCreatorMapperTest : BaseUnitTest() {
    @Test
    fun `mapToothbrushModelToAvroToothbrushModel() returns Avro Ara for ARA`() {
        TestCase.assertEquals(
            Contract.ToothbrushModelName.ARA,
            mapToothbrushModelToAvroToothbrushModel(ToothbrushModel.ARA)
        )
    }

    @Test
    fun `mapToothbrushModelToAvroToothbrushModel() returns Avro E1 for CONNECT_E1`() {
        TestCase.assertEquals(
            Contract.ToothbrushModelName.E1,
            mapToothbrushModelToAvroToothbrushModel(ToothbrushModel.CONNECT_E1)
        )
    }

    @Test
    fun `mapToothbrushModelToAvroToothbrushModel() returns Avro E2 for CONNECT_E2`() {
        TestCase.assertEquals(
            Contract.ToothbrushModelName.E2,
            mapToothbrushModelToAvroToothbrushModel(ToothbrushModel.CONNECT_E2)
        )
    }

    @Test
    fun `mapToothbrushModelToAvroToothbrushModel() returns Avro E2 for HILINK`() {
        TestCase.assertEquals(
            Contract.ToothbrushModelName.E2,
            mapToothbrushModelToAvroToothbrushModel(ToothbrushModel.HILINK)
        )
    }

    @Test
    fun `mapToothbrushModelToAvroToothbrushModel() returns Avro M1 for CONNECT_M1`() {
        TestCase.assertEquals(
            Contract.ToothbrushModelName.M1,
            mapToothbrushModelToAvroToothbrushModel(ToothbrushModel.CONNECT_M1)
        )
    }

    @Test
    fun `mapToothbrushModelToAvroToothbrushModel() returns Avro B1 for CONNECT_B1`() {
        TestCase.assertEquals(
            Contract.ToothbrushModelName.B1,
            mapToothbrushModelToAvroToothbrushModel(ToothbrushModel.CONNECT_B1)
        )
    }

    @Test
    fun `mapToothbrushModelToAvroToothbrushModel() returns Avro PQL for Plaqless`() {
        TestCase.assertEquals(
            Contract.ToothbrushModelName.PQL,
            mapToothbrushModelToAvroToothbrushModel(ToothbrushModel.PLAQLESS)
        )
    }

    @Test
    fun `mapToothbrushModelToAvroToothbrushModel() returns Avro GLT for GLINT`() {
        TestCase.assertEquals(
            Contract.ToothbrushModelName.GLI,
            mapToothbrushModelToAvroToothbrushModel(ToothbrushModel.GLINT)
        )
    }

    @Test
    fun `mapToothbrushModelToAvroToothbrushModel() returns Avro E2 for HUM_ELECTRIC`() {
        TestCase.assertEquals(
            Contract.ToothbrushModelName.E2,
            mapToothbrushModelToAvroToothbrushModel(ToothbrushModel.HUM_ELECTRIC)
        )
    }

    @Test
    fun `mapToothbrushModelToAvroToothbrushModel() returns Avro B1 for HUM_BATTERY`() {
        TestCase.assertEquals(
            Contract.ToothbrushModelName.B1,
            mapToothbrushModelToAvroToothbrushModel(ToothbrushModel.HUM_BATTERY)
        )
    }
}
