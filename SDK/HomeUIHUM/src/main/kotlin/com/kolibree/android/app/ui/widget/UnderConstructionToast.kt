/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget

import android.content.Context
import android.view.Gravity
import android.widget.Toast

internal fun Context.showUnderConstructionToast() {
    showToast("\uD83D\uDC77 Under construction")
}

internal fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).also {
        it.setGravity(Gravity.CENTER, 0, 0)
        it.show()
    }
}
