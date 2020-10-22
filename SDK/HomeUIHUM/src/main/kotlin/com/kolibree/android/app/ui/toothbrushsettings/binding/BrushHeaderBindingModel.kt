/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.binding

import android.content.Context
import androidx.annotation.StringRes
import com.kolibree.android.app.ui.toothbrushsettings.ConnectionState
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

@Parcelize
internal data class BrushHeaderBindingModel(
    val name: String,
    val lastSyncDate: LocalDate?,
    val connectionState: ConnectionState?,
    val optionalOtaAvailable: Boolean,
    val mandatoryOtaAvailable: Boolean
) : ToothbrushSettingsItemBindingModel {

    init {
        FailEarly.failInConditionMet(
            optionalOtaAvailable && mandatoryOtaAvailable,
            "Optional and mandatory update at the same time is not possible"
        )
    }

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_tb_settings_brush_header)
    }

    fun toothbrushConnectionStatus(context: Context): String = when (connectionState) {
        ConnectionState.CONNECTED -> context.getString(R.string.tb_settings_connected, name)
        else -> context.getString(R.string.tb_settings_connecting)
    }

    fun lastSyncDate(context: Context, date: LocalDate?): String {
        val lastSyncDate = when {
            date == null -> context.getString(R.string.tb_settings_last_sync_never)
            isToday(date) -> context.getString(R.string.tb_settings_last_sync_today)
            else -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date)
        }
        return context.getString(R.string.tb_settings_last_sync, lastSyncDate)
    }

    fun isConnectingAnimationOn(): Boolean = connectionState != ConnectionState.CONNECTED

    fun isConnectedIconVisible(): Boolean = connectionState == ConnectionState.CONNECTED

    fun isLastSyncDateVisible(): Boolean = connectionState == ConnectionState.CONNECTED

    fun isWaitingVisible(): Boolean = connectionState == ConnectionState.CONNECTING

    fun isNotConnectingVisible(): Boolean = connectionState == ConnectionState.DISCONNECTED

    fun isOtaAvailable(): Boolean = optionalOtaAvailable || mandatoryOtaAvailable

    @StringRes
    fun otaText(): Int =
        if (mandatoryOtaAvailable) {
            R.string.tb_settings_mandatory_ota_title
        } else {
            R.string.tb_settings_optional_ota_title
        }

    @StringRes
    fun otaActionText(): Int =
        if (mandatoryOtaAvailable) {
            R.string.tb_settings_mandatory_ota_proceed
        } else {
            R.string.tb_settings_optional_ota_proceed
        }

    private fun isToday(date: LocalDate) =
        date.isEqual(TrustedClock.getNowLocalDate())
}
