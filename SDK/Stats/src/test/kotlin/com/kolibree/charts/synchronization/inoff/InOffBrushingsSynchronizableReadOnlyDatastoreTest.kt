/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.synchronization.inoff

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.charts.inoff.data.persistence.InOffBrushingsCountDao
import com.kolibree.charts.inoff.data.persistence.model.InOffBrushingsCountEntity
import com.kolibree.charts.synchronization.StatsSynchronizedVersions
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class InOffBrushingsSynchronizableReadOnlyDatastoreTest : BaseUnitTest() {
    private val dao: InOffBrushingsCountDao = mock()
    private val versionsPersistence: StatsSynchronizedVersions = mock()

    private lateinit var datastore: InOffBrushingsSynchronizableReadOnlyDatastore

    override fun setup() {
        super.setup()

        datastore = InOffBrushingsSynchronizableReadOnlyDatastore(
            dao,
            versionsPersistence
        )
    }

    @Test
    fun `replace does nothing if parameter is not InOffBrushingsCountEntity `() {
        datastore.replace(mock())

        verify(dao, never()).insertOrReplace(any())
    }

    @Test
    fun `replace invokes replace with InOffBrushingsCountEntity`() {
        val entity =
            InOffBrushingsCountEntity(
                profileId = 12,
                onlineBrushingCount = 1,
                offlineBrushingCount = 2
            )

        datastore.replace(entity)

        verify(dao).insertOrReplace(entity)
    }

    /*
   UPDATE VERSION
    */

    @Test
    fun `updateVersion invokes StatsSynchronizedVersions with expected value`() {
        val expectedVersion = 543
        datastore.updateVersion(expectedVersion)

        verify(versionsPersistence).setInOffBrushingsCountVersion(expectedVersion)
    }
}
