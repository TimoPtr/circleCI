/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.test.idlingresources

import com.squareup.picasso.Picasso
import com.squareup.picasso.hasActions

internal class PicassoIdlingResource : EagerIdlingResource(
    name = "PicassoIdlingResource"
) {
    override fun isIdle(): Boolean {
        return !Picasso.get().hasActions()
    }
}
