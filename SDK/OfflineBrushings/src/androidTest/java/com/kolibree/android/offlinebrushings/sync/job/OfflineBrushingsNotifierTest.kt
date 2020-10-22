/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.base.Optional
import com.kolibree.android.feature.OfflineBrushingsNotificationsFeature
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.android.test.BaseInstrumentationTest
import com.nhaarman.mockitokotlin2.mock
import javax.inject.Provider
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.OffsetDateTime

@RunWith(AndroidJUnit4::class)
class OfflineBrushingsNotifierTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var notifier: OfflineBrushingsNotifier

    @Test
    fun showNotification_does_nothing_if_featureToglle_is_disabled() {
        init(featureEnabled = false)

        notifier.showNotification(createOfflineBrushingNotificationContent())
    }

    @Test
    fun showNotification_does_nothing_if_OfflineBrushingNotificationContent_is_EMPTY() {
        init()

        notifier.showNotification(OfflineBrushingNotificationContent.EMPTY)
    }

    /*
    Utils
     */
    private fun init(featureEnabled: Boolean = true, consentNeeded: Boolean = false) {

        val feature = PersistentFeatureToggle(context(), OfflineBrushingsNotificationsFeature)
        feature.value = featureEnabled

        notifier = OfflineBrushingsNotifier(
            context(),
            Optional.of(Provider { mock<Intent>() }),
            setOf(feature),
            mock()
        )
    }

    private fun createOfflineBrushingNotificationContent(
        title: String = "",
        content: String = "",
        offlineBrushingsDateTimes: List<OffsetDateTime> = listOf(),
        orphanBrushingsDateTimes: List<OffsetDateTime> = listOf()
    ) = OfflineBrushingNotificationContent(
        title,
        content,
        offlineBrushingsDateTimes,
        orphanBrushingsDateTimes
    )
}
