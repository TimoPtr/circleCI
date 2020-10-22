package com.kolibree.android.toothbrushupdate

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.sdkws.data.model.GruwareData
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

class OtaForConnectionTest : BaseUnitTest() {
    @Test(expected = IllegalArgumentException::class)
    fun init_STANDARD_noGruwareData_throwsIllegalArgumentException() {
        OtaForConnection(mock(), OtaUpdateType.STANDARD)
    }

    @Test(expected = IllegalArgumentException::class)
    fun init_MANDATORY_noGruwareData_throwsIllegalArgumentException() {
        OtaForConnection(mock(), OtaUpdateType.MANDATORY)
    }

    @Test
    fun init_MANDATORY_NEEDS_INTERNET_noGruwareData_setsTagNull() {
        val connection: KLTBConnection = mock()
        OtaForConnection(connection, OtaUpdateType.MANDATORY_NEEDS_INTERNET)

        verify(connection).tag = null
    }

    @Test
    fun init_MANDATORY_withGruwareData_setsTag() {
        val connection: KLTBConnection = mock()
        val gruwareData: GruwareData = mock()
        OtaForConnection(connection, OtaUpdateType.MANDATORY, gruwareData)

        verify(connection).tag = gruwareData
    }

    @Test
    fun init_STANDARD_withGruwareData_setsTag() {
        val connection: KLTBConnection = mock()
        val gruwareData: GruwareData = mock()
        OtaForConnection(connection, OtaUpdateType.STANDARD, gruwareData)

        verify(connection).tag = gruwareData
    }
}
