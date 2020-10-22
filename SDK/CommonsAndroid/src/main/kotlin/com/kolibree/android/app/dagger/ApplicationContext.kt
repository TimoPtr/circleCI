/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.Keep

/**
 * [ContextWrapper] holding the applicationContext of context passed as parameter
 */
@Keep
class ApplicationContext(context: Context) : ContextWrapper(context.applicationContext)
