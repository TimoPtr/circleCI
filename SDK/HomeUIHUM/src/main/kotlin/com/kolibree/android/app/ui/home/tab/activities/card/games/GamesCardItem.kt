/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities.card.games

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import com.kolibree.android.app.ui.home.tab.activities.card.games.GamesCardItem.Status.DownloadAvailable
import com.kolibree.android.app.ui.home.tab.activities.card.games.GamesCardItem.Status.DownloadDone
import com.kolibree.android.app.ui.home.tab.activities.card.games.GamesCardItem.Status.DownloadInProgress
import com.kolibree.android.app.ui.home.tab.activities.card.games.GamesCardItem.Status.UpdateAvailable
import com.kolibree.android.dynamiccards.BR
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.ItemBindingModel

@Parcelize
internal data class GamesCardItem(
    @DrawableRes
    val logoRes: Int,
    val points: Int,
    val title: String,
    val body: String,
    val status: Status,
    val size: String
) : Parcelable, ItemBindingModel {

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_games_card)
    }

    fun getPoints(context: Context): String {
        return context.resources.getQuantityString(
            R.plurals.games_card_item_points,
            points,
            points
        )
    }

    fun getStatusIconRes(): Int {
        return when (status) {
            is DownloadAvailable -> R.drawable.ic_download_start
            is DownloadInProgress -> R.drawable.ic_download_stop
            is DownloadDone -> R.drawable.ic_download_success
            is UpdateAvailable -> R.drawable.ic_update_available
        }
    }

    fun getStatusIconColor(context: Context): Int {
        return context.getStatusColor()
    }

    fun getProgress(): Int {
        return when (status) {
            is DownloadAvailable, UpdateAvailable -> 0
            is DownloadInProgress -> status.progress
            is DownloadDone -> 100
        }
    }

    fun getProgressColor(context: Context): Int {
        return context.getStatusColor()
    }

    private fun Context.getStatusColor(): Int {
        return when (status) {
            is DownloadDone -> getColorFromAttr(R.attr.colorSecondaryDark)
            else -> getColorFromAttr(R.attr.colorPrimary)
        }
    }

    internal sealed class Status : Parcelable {

        @Parcelize
        object DownloadAvailable : Status()

        @Parcelize
        class DownloadInProgress(
            @IntRange(from = 0, to = 100)
            val progress: Int
        ) : Status()

        @Parcelize
        object DownloadDone : Status()

        @Parcelize
        object UpdateAvailable : Status()
    }
}
