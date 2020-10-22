/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.extensions

import android.content.SharedPreferences
import androidx.annotation.Keep
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable

@Keep
inline fun SharedPreferences.edit(func: SharedPreferences.Editor.() -> Unit) {
    val editor = edit()
    editor.func()
    editor.apply()
}

@Keep
fun <T> SharedPreferences.observeForChanges(
    key: String,
    getValue: SharedPreferences.(key: String) -> T
): Observable<T> {
    val changeRelay: Relay<T> = PublishRelay.create()
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
        if (changedKey == key) {
            val value = getValue(key)
            changeRelay.accept(value)
        }
    }

    return changeRelay
        .doOnSubscribe { registerOnSharedPreferenceChangeListener(listener) }
        .doOnDispose { unregisterOnSharedPreferenceChangeListener(listener) }
}
