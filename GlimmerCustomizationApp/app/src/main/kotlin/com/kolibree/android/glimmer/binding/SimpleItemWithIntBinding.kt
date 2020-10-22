/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.binding

import com.kolibree.android.glimmer.BR
import com.kolibree.android.glimmer.R
import me.tatarka.bindingcollectionadapter2.ItemBinding

internal object SimpleItemWithIntBinding {

    const val simpleLayout = R.layout.simple_item_with_int_binding

    @JvmField
    val simpleItemBinding = ItemBinding.of<Int>(BR.item, simpleLayout)
}
