/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.android.app.ui.dialog.KolibreeDialogBuilder.Companion.UNDEFINED_RESOURCE_ID

@Keep
abstract class KolibreeDialogCommon<T, V>(
    initialValue: V,
    valueDelegate: KolibreeValueDelegate<T, V> = KolibreeValueDelegateImpl(initialValue)
) : KolibreeDialogBuilder<T>,
    KolibreeValueDelegate<T, V> by valueDelegate {

    var title: String? = null
        private set

    @StringRes
    var titleId: Int = UNDEFINED_RESOURCE_ID
        private set

    /**
     * Sets the title of the control to the specified string
     */
    fun title(title: String) {
        this.title = title
    }

    /**
     * Sets the title of the control to the specified string resource
     */
    fun title(@StringRes titleId: Int) {
        this.titleId = titleId
    }

    fun buildWithListener(lambda: (view: T, valueListener: ValueListener<T, V>?) -> Unit): T {
        return build().also { view ->
            lambda.invoke(view, valueListener)
        }
    }
}
