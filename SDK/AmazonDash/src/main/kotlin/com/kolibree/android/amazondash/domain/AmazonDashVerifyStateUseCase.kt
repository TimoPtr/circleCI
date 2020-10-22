/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.domain

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.kolibree.android.extensions.edit
import io.reactivex.Single
import java.util.UUID
import javax.inject.Inject
import timber.log.Timber

internal interface AmazonDashVerifyStateUseCase {

    /**
     * Creates a new state and saves it for later verification.
     * Make sure that the state that you generate doesn't contain &, =, ‘, /, \, <, >, “,
     */
    fun createNewState(): Single<String>

    /**
     * Compares given [state] with current one and then deletes it.
     */
    fun verifyAndClear(state: String?): Single<Boolean>
}

/**
 * This implementation uses [SharedPreferences] as a cache
 * to persist state when app get's killed.
 *
 * Please be aware that this prefs are not connected to user session,
 * therefore they will survive logout or account deletion.
 */
internal class AmazonDashVerifyStateUseCaseImpl constructor(
    private val prefs: SharedPreferences
) : AmazonDashVerifyStateUseCase {

    @Inject
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    override fun createNewState(): Single<String> {
        return Single.fromCallable {
            val newState = UUID.randomUUID().toString()
            prefs.edit { putString(PREFS_STATE_KEY, newState) }

            Timber.d("New state generated: $newState")
            return@fromCallable newState
        }
    }

    override fun verifyAndClear(state: String?): Single<Boolean> {
        return Single.fromCallable {
            val currentState = prefs.getString(PREFS_STATE_KEY, null)
            val isValid = !state.isNullOrBlank() && state == currentState
            Timber.d("Verifying state: $state - valid: $isValid")

            prefs.edit { remove(PREFS_STATE_KEY) }
            Timber.d("State cleared")

            return@fromCallable isValid
        }
    }

    @VisibleForTesting
    internal companion object {
        const val PREFS_STATE_KEY = "state"
        private const val PREFS_NAME = "amazon_dash_state"
    }
}
