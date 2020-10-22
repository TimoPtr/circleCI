/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui

import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.card.DynamicCardPosition

@VisibleForApp
interface DynamicCardListConfiguration {

    fun <T : ViewModel> getInitialCardPosition(cardViewModelClass: Class<T>): DynamicCardPosition
}
