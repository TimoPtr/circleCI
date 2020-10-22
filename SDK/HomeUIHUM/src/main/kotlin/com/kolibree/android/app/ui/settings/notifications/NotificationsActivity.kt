/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateFormat.is24HourFormat
import android.text.style.StyleSpan
import androidx.annotation.Keep
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.ui.dialog.alertDialog
import com.kolibree.android.app.utils.setSpan
import com.kolibree.android.brushreminder.model.BrushingReminderType
import com.kolibree.android.commons.extensions.toCalendar
import com.kolibree.android.commons.extensions.toLocalTime
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityNotificationsBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import org.threeten.bp.LocalTime

internal class NotificationsActivity :
    BaseMVIActivity<
        NotificationsViewState,
        NotificationsActions,
        NotificationsViewModel.Factory,
        NotificationsViewModel,
        ActivityNotificationsBinding>(),
    TrackableScreen {

    override fun getViewModelClass(): Class<NotificationsViewModel> =
        NotificationsViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_notifications

    override fun execute(action: NotificationsActions) {
        when (action) {
            is NotificationsActions.ShowTimePicker ->
                showTimePicker(action.type, action.currentReminderTime)
        }
    }

    private fun showTimePicker(
        reminderType: BrushingReminderType,
        currentTime: LocalTime
    ) {
        MaterialDialog(this).show {
            lifecycleOwner(this@NotificationsActivity)
            timePicker(
                currentTime = currentTime.toCalendar(),
                show24HoursView = is24HourFormat(this@NotificationsActivity)
            ) { _, time ->
                viewModel.userSelectedReminderTime(time.toLocalTime(), reminderType)
            }
        }
    }

    override fun onBackPressed() {
        viewModel.onCloseScreen()
    }

    override fun getScreenName(): AnalyticsEvent = NotificationsAnalytics.main()

    @Suppress("LongMethod")
    fun showNotificationsDisabledDialog() {
        alertDialog(this) {
            lifecycleOwner(this@NotificationsActivity)
            title(R.string.notifications_disabled_dialog_title)
            body(buildBody(this@NotificationsActivity))
            cancellable(false) // it should stay not cancellable to restore previous env on cancel
            containedButton {
                title(R.string.notifications_disabled_go_to_settings)
                action {
                    viewModel.onGoToSettingsClick()
                    dismiss()
                }
            }
            textButton {
                title(R.string.cancel)
                action {
                    dismiss()
                }
            }
        }.show()
    }

    private fun buildBody(context: Context): Spannable {
        val body = context.getString(R.string.notifications_disabled_dialog_body)
        val point1 = context.getString(R.string.notifications_disabled_dialog_point1)
        val point2 = context.getString(R.string.notifications_disabled_dialog_point2)

        val string = buildString {
            appendln(body)
            appendln()
            appendln(point1)
            appendln(point2)
        }

        return SpannableStringBuilder(string).apply {
            setSpan(point1, StyleSpan(Typeface.BOLD))
            setSpan(point2, StyleSpan(Typeface.BOLD))
        }
    }
}

@Keep
fun startNotificationsActivity(context: Context) {
    context.startActivity(Intent(context, NotificationsActivity::class.java))
}
