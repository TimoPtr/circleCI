/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.fragment

import androidx.annotation.Keep
import com.kolibree.android.defensive.Preconditions

/**
 * @return non-null [BaseFragment] arguments
 * @throws [NullPointerException] if arguments is null
 */
@Keep
fun BaseFragment.sanitizedArguments() = Preconditions.checkNotNull(arguments)
