/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.data.model

import androidx.annotation.StringRes
import com.kolibree.android.app.Error

internal data class AmazonDashException(val error: Error) : Exception() {
    constructor(errorMessage: String) : this(Error.from(errorMessage))
    constructor(@StringRes errorRes: Int) : this(Error.from(errorRes))
}
