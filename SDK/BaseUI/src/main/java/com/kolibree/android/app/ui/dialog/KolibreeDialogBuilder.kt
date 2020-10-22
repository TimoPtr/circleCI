/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

internal interface KolibreeDialogBuilder<T> {

    fun build(): T

    companion object {
        const val UNDEFINED_RESOURCE_ID = 0
    }
}
