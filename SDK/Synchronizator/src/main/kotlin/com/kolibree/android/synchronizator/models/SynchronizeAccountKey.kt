/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.models

import android.annotation.SuppressLint

/**
 * Holds info to be added to a a SynchronizeAccountRequest
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
data class SynchronizeAccountKey(val key: SynchronizableKey, val version: Int)
