/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbar

import android.os.Parcelable
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

@VisibleForApp
sealed class ToolbarIcon(
    @DrawableRes val mainIcon: Int,
    @AttrRes val mainIconAlphaAttr: Int = R.attr.disconnectedIconAlpha,
    @DrawableRes val badgeIcon: Int,
    @DrawableRes val startTransition: Int = 0,
    @DrawableRes val endTransition: Int = 0
) : Parcelable {

    @Parcelize
    @VisibleForApp
    object NoToothbrush :
        ToolbarIcon(
            mainIcon = R.drawable.ic_toothbrush,
            badgeIcon = R.drawable.ic_badge_add
        )

    @Parcelize
    @VisibleForApp
    object ToothbrushDisconnected :
        ToolbarIcon(
            mainIcon = R.drawable.ic_toothbrush,
            badgeIcon = R.drawable.ic_badge_disconnected
        )

    @Parcelize
    @VisibleForApp
    object ToothbrushConnected :
        ToolbarIcon(
            mainIcon = R.drawable.ic_toothbrush,
            mainIconAlphaAttr = R.attr.connectedIconAlpha,
            badgeIcon = R.drawable.ic_badge_connected
        )

    @Parcelize
    @VisibleForApp
    object ToothbrushConnectedOta :
        ToolbarIcon(
            mainIcon = R.drawable.ic_toothbrush_ota,
            mainIconAlphaAttr = R.attr.connectedIconAlpha,
            badgeIcon = R.drawable.ic_badge_connected
        )

    @Parcelize
    @VisibleForApp
    object ToothbrushConnecting :
        ToolbarIcon(
            mainIcon = R.drawable.ic_toothbrush_connecting_animated,
            mainIconAlphaAttr = R.attr.disconnectedIconAlpha,
            badgeIcon = R.drawable.ic_badge_disconnected,
            startTransition = R.drawable.ic_toothbrush_shrink,
            endTransition = R.drawable.ic_toothbrush_grow
        )
}
