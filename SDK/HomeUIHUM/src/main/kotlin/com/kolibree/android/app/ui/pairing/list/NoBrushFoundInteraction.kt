/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import androidx.lifecycle.LiveData

internal interface NoBrushFoundInteraction {
    val showNoBrushFound: LiveData<Boolean>

    fun getItClick()
    fun closeClick()
}
