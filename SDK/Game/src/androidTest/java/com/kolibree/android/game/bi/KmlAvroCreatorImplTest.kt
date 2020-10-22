/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.bi

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.kml.CharVector
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.avro.AVRO_CACHE_DIRECTORY
import com.kolibree.sdkws.core.avro.AvroFileUploader
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [KmlAvroCreator] instrumentation tests */
class KmlAvroCreatorImplTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val connector: IKolibreeConnector = mock()

    private val appVersions = KolibreeAppVersions("", "")

    private val avroFileUploader: AvroFileUploader = mock()

    private lateinit var avroCreator: KmlAvroCreatorImpl

    override fun setUp() {
        super.setUp()

        avroCreator = KmlAvroCreatorImpl(
            connector = connector,
            appVersions = appVersions,
            avroFileUploader = avroFileUploader,
            context = context()
        )
    }

    override fun tearDown() {
        super.tearDown()

        File(context().cacheDir, AVRO_CACHE_DIRECTORY).deleteRecursively()
    }

    /*
    submitAvroData
     */

    @Test
    fun submitAvroData_correctlyWritesAvroDataToTheRightFile() {
        val expectedData = "I'm some piece of AVRO data"
        val charVector = mock<CharVector>()
        whenever(charVector.iterator())
            .thenReturn(expectedData.toCharArray().toMutableList().iterator())

        avroCreator.submitAvroData(charVector)
            .test()
            .assertNoErrors()
            .assertComplete()

        val avroDirectory = File(context().cacheDir, AVRO_CACHE_DIRECTORY)
        assertTrue(avroDirectory.exists())
        assertTrue(avroDirectory.isDirectory)

        val avroFile = avroDirectory.listFiles()!!
            .first { it.name.startsWith("raw_data_avro_") }

        assertTrue(avroFile.exists())
        assertEquals(expectedData, avroFile.readText())
    }

    @Test
    fun submitAvroData_invokes_uploadPendingFiles() {
        val charVector = mock<CharVector>()
        whenever(charVector.iterator()).thenReturn(mutableListOf<Char>().iterator())

        avroCreator.submitAvroData(charVector)
            .test()
            .assertNoErrors()
            .assertComplete()

        verify(avroFileUploader).uploadPendingFiles()
    }

    @Test
    fun submitAvroData_doesNotOverwritePreviouslyRecordedFiles() {
        val charVector = mock<CharVector>()
        whenever(charVector.iterator()).thenReturn(mutableListOf<Char>().iterator())

        avroCreator.submitAvroData(charVector).test()
        avroCreator.submitAvroData(charVector).test()
        avroCreator.submitAvroData(charVector).test()
        avroCreator.submitAvroData(charVector).test()

        val avroFileCount = File(context().cacheDir, AVRO_CACHE_DIRECTORY)
            .listFiles()!!
            .filter { it.name.startsWith("raw_data_avro_") }
            .count()

        assertEquals(4, avroFileCount)
    }

    /*
    toothbrushProfileCompletable
     */

    @Test
    fun toothbrushProfileCompletable_getsProfileIdFromTb_thenProfileFromConnector() {
        val profileId = 1986L
        val expectedProfile = ProfileBuilder.create().withId(profileId).build()
        whenever(connector.getProfileWithIdSingle(profileId))
            .thenReturn(Single.just(expectedProfile))

        val connection = KLTBConnectionBuilder
            .createWithDefaultState(true)
            .withOwnerId(profileId)
            .build()

        avroCreator.toothbrushProfileCompletable(connection)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(expectedProfile)
    }

    @Test
    fun toothbrushProfileCompletable_emitsOwnerProfileOnSharedBrush() {
        val profileId = 1986L
        val expectedProfile = ProfileBuilder.create().withId(profileId).build()
        whenever(connector.currentProfile).thenReturn(expectedProfile)

        val connection = KLTBConnectionBuilder
            .createWithDefaultState(true)
            .withSharedMode()
            .build()

        avroCreator.toothbrushProfileCompletable(connection)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(expectedProfile)
    }

    @Test
    fun getHandedness_mapsToCorrespondingContractHandedness() {
        assertEquals(
            avroCreator.getHandedness(Handedness.RIGHT_HANDED),
            Contract.Handedness.RIGHT_HANDED
        )
        assertEquals(
            avroCreator.getHandedness(Handedness.LEFT_HANDED),
            Contract.Handedness.LEFT_HANDED
        )
        assertEquals(
            avroCreator.getHandedness(Handedness.UNKNOWN),
            Contract.Handedness.UNKNOWN
        )
    }
}
