package com.kolibree.android.synchronizator.models

import android.annotation.SuppressLint

/**
 * Builder to dynamically construct SynchronizeAccountKey
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
abstract class SynchronizeAccountKeyBuilder(val key: SynchronizableKey) {
    fun build() = SynchronizeAccountKey(key, version())

    abstract fun version(): Int
}
