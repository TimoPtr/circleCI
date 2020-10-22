/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder

import android.content.Context
import com.kolibree.account.ProfileFacade
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.hum.brushsyncreminder.R
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

internal class BrushReminderTitleProviderTest : BaseUnitTest() {

    lateinit var provider: BrushSyncReminderTitleProvider

    private val profileFacade: ProfileFacade = mock()

    private val context: Context = mock()

    override fun setup() {
        super.setup()

        provider =
            BrushSyncReminderTitleProvider(
                profileFacade
            )
    }

    @Test
    fun `method returns title together with user name`() {
        val profileName = "Testovsky"
        val profileId = 123L
        val profile = ProfileBuilder.create()
            .withName(profileName)
            .withId(profileId)
            .build()

        whenever(profileFacade.getProfile(profileId)).thenReturn(Single.just(profile))
        val title = "title test 001"
        whenever(context.getString(R.string.brush_reminder_notification_body2))
            .thenReturn(title)
        whenever(context.getString(R.string.reminder_notification_body, profileName, title))
            .thenReturn("name & title")

        val randomIndex = 1
        provider.title(context, profileId, randomIndex)

        verify(context).getString(R.string.reminder_notification_body, profileName, title)
    }
}
