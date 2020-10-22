/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card

import android.annotation.SuppressLint

/**
 * Interaction is a public interface of the view - like listener, but
 * can hold more methods. Bound to the view, its methods can be called
 * directly from binding.
 *
 * Because the way user interacts with each card can be different, it's
 * up to each feature to implement methods it needs.
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
interface DynamicCardInteraction {

    /**
     * Associative method between [DynamicCardBindingModel] and [DynamicCardInteraction],
     * which compose a unique pair.
     *
     * @return true if [bindingModel] comes from the same feature as
     * this interaction.
     */
    fun interactsWith(bindingModel: DynamicCardBindingModel): Boolean
}
