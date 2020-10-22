/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

internal interface KolibreeActionDelegate<T> {
    /**
     * Sets an action to be performed whenever this control is clicked
     */
    var action: (T) -> Unit

    /**
     * Sets an action to be performed whenever this control is clicked
     */
    fun action(lambda: ((T) -> Unit)?)
}

internal class KolibreeActionDelegateImpl<T> : KolibreeActionDelegate<T> {

    override var action: (T) -> Unit = { /* NO-OP */ }

    override fun action(lambda: ((T) -> Unit)?) {
        this.action = lambda ?: { /* NO-OP */ }
    }
}
