/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.activity

import android.app.Activity
import android.content.Intent
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import kotlin.reflect.KClass

@Keep
fun <T : Activity> FragmentActivity.startActivity(activityClass: KClass<T>) =
    startActivity(Intent(this, activityClass.java))
