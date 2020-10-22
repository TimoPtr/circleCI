package com.kolibree.sdkws.data.model

import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AvailableUpdateTest {
    @Test(expected = IllegalStateException::class)
    fun validate_fileDoesNotExist_throwsIlegalStateException() {
        val availableUpdate = availableUpdateSpy()

        val file = mockUpdateFile(availableUpdate)
        whenever(file.exists()).thenReturn(false)

        availableUpdate.validate()
    }

    @Test(expected = IllegalStateException::class)
    fun validate_fileExists_fileIsDirectory_throwsIlegalStateException() {
        val availableUpdate = availableUpdateSpy()

        val file = mockUpdateFile(availableUpdate)
        whenever(file.exists()).thenReturn(true)
        whenever(file.isDirectory).thenReturn(true)

        availableUpdate.validate()
    }

    @Test(expected = IllegalStateException::class)
    fun validate_fileExists_fileIsNotDirectory_fileIsZeroLength_throwsIlegalStateException() {
        val availableUpdate = availableUpdateSpy()

        val file = mockUpdateFile(availableUpdate)
        whenever(file.exists()).thenReturn(true)
        whenever(file.isDirectory).thenReturn(false)
        whenever(file.length()).thenReturn(0L)

        availableUpdate.validate()
    }

    @Test
    fun validate_fileExists_fileIsNotDirectory_fileIsNotZeroLength_doesNothing() {
        val availableUpdate = availableUpdateSpy()

        val file = mockUpdateFile(availableUpdate)
        whenever(file.exists()).thenReturn(true)
        whenever(file.isDirectory).thenReturn(false)
        whenever(file.length()).thenReturn(40L)

        availableUpdate.validate()
    }

    @Test
    fun validate_emptyGRU_returnsTrue() {
        AvailableUpdate.empty(UpdateType.TYPE_GRU).validate()
    }

    @Test
    fun validate_emptyFw_returnsTrue() {
        AvailableUpdate.empty(UpdateType.TYPE_FIRMWARE).validate()
    }

    @Test
    fun empty_returnsIsEmptyTrue() {
        assertTrue(AvailableUpdate.empty(UpdateType.TYPE_GRU).isEmpty())
        assertTrue(AvailableUpdate.empty(UpdateType.TYPE_FIRMWARE).isEmpty())
    }

    private fun mockUpdateFile(availableUpdate: AvailableUpdate): File {
        val file: File = mock()
        doReturn(file).whenever(availableUpdate).updateFile()

        return file
    }

    private fun availableUpdateSpy() = spy(
        AvailableUpdate(
            "",
            "",
            UpdateType.TYPE_FIRMWARE,
            0L
        )
    )
}
