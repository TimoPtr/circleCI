/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.fakebrushings

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.annotation.Keep
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.kolibree.R
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.widget.snackbar.snackbar
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.extensions.toCalendar
import com.kolibree.android.commons.extensions.toLocalDateTime
import com.kolibree.android.tracker.NonTrackableScreen
import com.kolibree.databinding.ActivityCreateFakeBrushingBinding

internal class CreateFakeBrushingActivity :
    BaseMVIActivity<
        CreateFakeBrushingViewState,
        CreateFakeBrushingActions,
        CreateFakeBrushingViewModel.Factory,
        CreateFakeBrushingViewModel,
        ActivityCreateFakeBrushingBinding>(),
    NonTrackableScreen {

    override fun getViewModelClass(): Class<CreateFakeBrushingViewModel> =
        CreateFakeBrushingViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_create_fake_brushing

    override fun execute(action: CreateFakeBrushingActions) {
        when (action) {
            is CreateFakeBrushingActions.DateClick -> showDateTimePicker()
            is CreateFakeBrushingActions.FutureDateSelected -> showFutureDateNotSupported()
            is CreateFakeBrushingActions.InvalidAccount -> showInvalidAccount()
        }
    }

    private fun showDateTimePicker() {
        MaterialDialog(this).show {
            lifecycleOwner(this@CreateFakeBrushingActivity)
            dateTimePicker(
                currentDateTime = TrustedClock.getNowLocalDateTime()
                    .toCalendar(TrustedClock.systemZone),
                show24HoursView = DateFormat.is24HourFormat(this@CreateFakeBrushingActivity)
            ) { _, time ->
                viewModel.onUserSelectedBrushingDate(time.toLocalDateTime())
            }
        }
    }

    private fun showFutureDateNotSupported() {
        snackbar(binding.rootContentLayout) {
            message("Dates in the future aren't supported")
            duration(Snackbar.LENGTH_LONG)
            anchor(binding.fakeBrushingCreate)
        }.show()
    }

    private fun showInvalidAccount() {
        snackbar(binding.rootContentLayout) {
            message("Only accounts with emails ending in $supportedDomains can create fake brushing")
            duration(Snackbar.LENGTH_LONG)
            anchor(binding.fakeBrushingCreate)
        }.show()
    }
}

@Keep
fun startCreateFakeBrushingIntent(context: Context) {
    context.startActivity(Intent(context, CreateFakeBrushingActivity::class.java))
}
