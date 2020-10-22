/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature.impl

import android.content.Context
import androidx.annotation.Keep
import com.kolibree.android.extensions.edit
import com.kolibree.android.feature.Feature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.checkIfFeatureTypeIsSupported
import com.kolibree.android.persistence.BasePreferencesImpl
import javax.inject.Inject

/**
 * Persistent feature toggle. Returns initial feature value before user makes any changes to it.
 * After that, it returns value set by the user. Keeps the value between app launches.
 */
@Keep
class PersistentFeatureToggle<T : Any> @Inject constructor(
    context: Context,
    override val feature: Feature<T>
) : FeatureToggle<T>, BasePreferencesImpl(context) {

    private val storageKey = feature::class.java.canonicalName

    init {
        checkIfFeatureTypeIsSupported()
    }

    override var value: T
        @Suppress("UNCHECKED_CAST")
        get() = with(prefs) {
            when (feature.type()) {
                Boolean::class -> getBoolean(storageKey, feature.initialValue as Boolean) as T
                String::class -> getString(storageKey, feature.initialValue as String) as T
                Long::class -> getLong(storageKey, feature.initialValue as Long) as T
                // TODO add support for single-choice enums https://kolibree.atlassian.net/browse/KLTB002-9495
                // TODO add support for multi-choice enums https://kolibree.atlassian.net/browse/KLTB002-9496
                else -> throw UnsupportedOperationException("${feature.type()} is not currently supported!")
            }
        }
        set(value) {
            prefs.edit {
                when (feature.type()) {
                    Boolean::class -> putBoolean(storageKey, value as Boolean)
                    String::class -> putString(storageKey, value as String)
                    Long::class -> putLong(storageKey, value as Long)
                    // TODO add support for single-choice enums https://kolibree.atlassian.net/browse/KLTB002-9495
                    // TODO add support for multi-choice enums https://kolibree.atlassian.net/browse/KLTB002-9496
                    else -> throw UnsupportedOperationException("${feature.type()} is not currently supported!")
                }
            }
        }

    override fun getPreferencesName(): String {
        return "secret_feature_toggles"
    }
}
