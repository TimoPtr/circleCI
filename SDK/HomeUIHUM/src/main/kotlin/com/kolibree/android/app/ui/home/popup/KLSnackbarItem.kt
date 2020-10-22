/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup

import com.kolibree.android.utils.KLItem
import com.kolibree.android.utils.Priority

internal sealed class KLSnackbarItem : KLItem {

    internal object SnackbarLocationItem : KLSnackbarItem() {
        override val priority = Priority.LOW
    }

    internal object SnackbarBluetoothItem : KLSnackbarItem() {
        override val priority = Priority.LOW
    }
}
