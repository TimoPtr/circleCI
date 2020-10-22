/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

import android.content.Context
import androidx.annotation.StringRes

internal class StoreAvatarException(context: Context, @StringRes messageResId: Int) :
    Exception(context.getString(messageResId))
