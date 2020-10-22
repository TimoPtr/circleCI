/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.squareup.picasso

internal fun Picasso.hasActions(): Boolean {
    return targetToAction.isNotEmpty()
}
