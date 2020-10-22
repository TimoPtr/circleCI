/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.database

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.models.SynchronizableKey.ACCOUNT
import com.kolibree.android.synchronizator.models.SynchronizableKey.BRUSHINGS
import com.kolibree.android.synchronizator.models.SynchronizableKey.CHALLENGE_CATALOG
import com.kolibree.android.synchronizator.models.SynchronizableKey.CHALLENGE_PROGRESS
import com.kolibree.android.synchronizator.models.SynchronizableKey.GAME_PROGRESS
import com.kolibree.android.synchronizator.models.SynchronizableKey.IN_OFF_BRUSHINGS_COUNT
import com.kolibree.android.synchronizator.models.SynchronizableKey.PERSONAL_CHALLENGE
import com.kolibree.android.synchronizator.models.SynchronizableKey.PRIZES_CATALOG
import com.kolibree.android.synchronizator.models.SynchronizableKey.PROFILES
import com.kolibree.android.synchronizator.models.SynchronizableKey.PROFILE_SMILES
import com.kolibree.android.synchronizator.models.SynchronizableKey.PROFILE_SMILES_HISTORY
import com.kolibree.android.synchronizator.models.SynchronizableKey.PROFILE_TIER
import com.kolibree.android.synchronizator.models.SynchronizableKey.SHORT_TASK
import com.kolibree.android.synchronizator.models.SynchronizableKey.TIERS_CATALOG
import com.kolibree.android.synchronizator.models.SynchronizableKey.values
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("TestFunctionName")
class SynchronizableKeyConverterTest : BaseUnitTest() {
    private val converter = SynchronizableKeyConverter()

    @Test
    fun conversion_is_reversible() {
        values().forEach { key ->
            val keyAsString = converter.toSynchronizableKey(key)

            assertEquals(key, converter.fromSynchronizableKey(keyAsString))
        }
    }

    /*
    Ensure that no key is removed by accident from the enum

    By hardcoding the key value, the test is independent of the enum changes

    If one of the tests below fails in the future, it means an NPE can happen for a user in
    production
     */

    @Test
    fun ACCOUNT_key_doesNotReturnNull() {
        assertEquals(ACCOUNT, converter.fromSynchronizableKey("account"))
    }

    @Test
    fun PROFILES_key_doesNotReturnNull() {
        assertEquals(PROFILES, converter.fromSynchronizableKey("profiles"))
    }

    @Test
    fun BRUSHINGS_key_doesNotReturnNull() {
        assertEquals(BRUSHINGS, converter.fromSynchronizableKey("brushings"))
    }

    @Test
    fun GAME_PROGRESS_key_doesNotReturnNull() {
        assertEquals(GAME_PROGRESS, converter.fromSynchronizableKey("game_progress"))
    }

    @Test
    fun CHALLENGE_CATALOG_key_doesNotReturnNull() {
        assertEquals(CHALLENGE_CATALOG, converter.fromSynchronizableKey("challenges_catalog"))
    }

    @Test
    fun PRIZES_CATALOG_key_doesNotReturnNull() {
        assertEquals(PRIZES_CATALOG, converter.fromSynchronizableKey("prizes_catalog"))
    }

    @Test
    fun TIERS_CATALOG_key_doesNotReturnNull() {
        assertEquals(TIERS_CATALOG, converter.fromSynchronizableKey("tiers_catalog"))
    }

    @Test
    fun CHALLENGE_PROGRESS_key_doesNotReturnNull() {
        assertEquals(CHALLENGE_PROGRESS, converter.fromSynchronizableKey("challenges_progress"))
    }

    @Test
    fun PROFILE_TIER_key_doesNotReturnNull() {
        assertEquals(PROFILE_TIER, converter.fromSynchronizableKey("profile_tier"))
    }

    @Test
    fun PROFILE_SMILES_key_doesNotReturnNull() {
        assertEquals(PROFILE_SMILES, converter.fromSynchronizableKey("profile_smiles"))
    }

    @Test
    fun PROFILE_SMILES_HISTORY_key_doesNotReturnNull() {
        assertEquals(PROFILE_SMILES_HISTORY, converter.fromSynchronizableKey("rewards_history"))
    }

    @Test
    fun PERSONAL_CHALLENGE_key_doesNotReturnNull() {
        assertEquals(PERSONAL_CHALLENGE, converter.fromSynchronizableKey("personal_challenge"))
    }

    @Test
    fun SHORT_TASK_key_doesNotReturnNull() {
        assertEquals(SHORT_TASK, converter.fromSynchronizableKey("short_task"))
    }

    @Test
    fun IN_OFF_BRUSHINGS_COUNT_key_doesNotReturnNull() {
        assertEquals(IN_OFF_BRUSHINGS_COUNT, converter.fromSynchronizableKey("brushing_in_vs_off"))
    }
}
