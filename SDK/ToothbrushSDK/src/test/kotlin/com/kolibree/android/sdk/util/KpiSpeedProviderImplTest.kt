/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.util

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.R
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class KpiSpeedProviderImplTest : BaseUnitTest() {

    lateinit var kpiSpeedProviderImpl: KpiSpeedProviderImpl

    override fun setup() {
        super.setup()
        kpiSpeedProviderImpl = KpiSpeedProviderImpl(mock(), mock(), mock())
    }

    @Test
    fun `get kpiSpeed and iv for all returns kpi_speed_manual_json_enc and kpi_speed_ranges_manual_json_iv`() {
        ToothbrushModel.values().forEach {
            assertEquals(
                Pair(
                    R.raw.kpi_speed_ranges_json_enc,
                    R.raw.kpi_speed_ranges_json_iv
                ), kpiSpeedProviderImpl.getEncryptedKpiRes(it)
            )
        }
    }
}
