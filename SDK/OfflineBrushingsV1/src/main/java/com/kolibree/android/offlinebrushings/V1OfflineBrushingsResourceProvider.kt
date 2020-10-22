/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import androidx.annotation.Keep
import com.kolibree.android.offlinebrushings.v1.R

@Keep
object V1OfflineBrushingsResourceProvider : OfflineBrushingsResourceProvider {

    override val notificationChannelName = R.string.push_notification_channel_night_watch

    override val largeNotificationIcon = R.drawable.ic_offline_brushing_notification

    override val smallNotificationIcon: Int = R.drawable.ic_notification_small

    override val multipleBrushingWithSmilesNotificationTitle: Int =
        R.string.offline_brushing_notification_multiple_with_smiles_title

    override val multipleBrushingNoSmilesNotificationTitle: Int =
        R.string.offline_brushing_notification_multiple_no_smiles_title

    override val singleBrushingWithSmilesNotificationTitle: Int =
        R.string.offline_brushing_notification_single_with_smiles_title

    override val singleBrushingNoSmilesNotificationTitle: Int =
        R.string.offline_brushing_notification_single_no_smiles_title

    override val multipleBrushingWithSmilesNotificationBody: Int =
        R.string.offline_brushing_notification_multiple_with_smiles_content

    override val multipleBrushingNoSmilesNotificationBody: Int =
        R.string.offline_brushing_notification_multiple_no_smiles_content

    override val singleBrushingWithSmilesNotificationBody: Int =
        R.string.offline_brushing_notification_single_with_smiles_content

    override val singleBrushingNoSmilesNotificationBody: Int =
        R.string.offline_brushing_notification_single_no_smiles_content
}
