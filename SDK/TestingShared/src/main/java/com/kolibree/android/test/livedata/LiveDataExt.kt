/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.livedata

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun <T> LiveData<T>.setTestValue(value: T) {
    (this as MutableLiveData<T>).value = value
}
