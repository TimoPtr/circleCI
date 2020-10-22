/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.push

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.messaging.RemoteMessage
import com.kolibree.android.app.notification.MINIMUM_BODY_LENGTH_FOR_BIG_TEXT_STYLE
import com.kolibree.android.app.notification.chooseStyleForTheMessage
import com.kolibree.android.test.BaseInstrumentationTest
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class PushNotificationUtilTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun chooseStyleForTheMessage_returnsNull_forShortBody_andNoImageUrl() {
        val notification = mock<RemoteMessage.Notification>()
        doReturn("Short string").whenever(notification).body
        doReturn(null).whenever(notification).imageUrl

        val style = chooseStyleForTheMessage(notification)

        assertNull(style)
    }

    @Test
    fun chooseStyleForTheMessage_returnsBigPictureStyle_ifImageUrlIsAvailable() {
        val notification = mock<RemoteMessage.Notification>()
        doReturn("Short string").whenever(notification).body
        whenever(notification.imageUrl).thenReturn(Uri.parse("http://example.com"))

        val style = chooseStyleForTheMessage(notification)

        assertTrue(style is NotificationCompat.BigPictureStyle)
    }

    @Test
    fun chooseStyleForTheMessage_returnsBiTextStyle_ifBodyIsLongEnough_andTheresNoImage() {
        val notification = mock<RemoteMessage.Notification>()
        doReturn("1".repeat(MINIMUM_BODY_LENGTH_FOR_BIG_TEXT_STYLE)).whenever(notification).body
        doReturn(null).whenever(notification).imageUrl

        val style = chooseStyleForTheMessage(notification)

        assertTrue(style is NotificationCompat.BigTextStyle)
    }

    @Test
    fun chooseStyleForTheMessage_returnsBigPictureStyle_ifImageUrlIsAvailable_evenIfBodyIsLong() {
        val notification = mock<RemoteMessage.Notification>()
        doReturn("1".repeat(MINIMUM_BODY_LENGTH_FOR_BIG_TEXT_STYLE)).whenever(notification).body
        whenever(notification.imageUrl).thenReturn(Uri.parse("http://example.com"))

        val style = chooseStyleForTheMessage(notification)

        assertTrue(style is NotificationCompat.BigPictureStyle)
    }

    private fun chooseStyleForTheMessage(
        notification: RemoteMessage.Notification
    ): NotificationCompat.Style? {
        return chooseStyleForTheMessage(notification.imageUrl, notification.body)
    }
}
