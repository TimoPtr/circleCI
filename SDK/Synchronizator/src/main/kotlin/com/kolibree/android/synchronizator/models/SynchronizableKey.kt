/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.models

import com.kolibree.android.annotation.VisibleForApp

/**
 * Represents keys supported by synchronization endpoint
 *
 * Don't remove or update a value without a migration or crashes will happen on production
 *
 * @param priority higher goes first. In case of the same priority
 * items can be processed in parallel.
 *
 * @see <a href="https://kolibree.atlassian.net/l/c/yZ1MHVfW">keys</a>
 *
 * @suppress MagicNumber because we want to keep [priority] simple.
 */
@SuppressWarnings("MagicNumber")
@VisibleForApp
enum class SynchronizableKey(
    internal val value: String,
    internal val priority: Int
) {

    // Priority 4
    ACCOUNT("account", 4), // not implemented yet
    PROFILES("profiles", 4), // not implemented yet
    BRUSHINGS("brushings", 4), // not implemented yet
    PROFILE_SMILES("profile_smiles", 4),

    // Priority 3
    CHALLENGE_CATALOG("challenges_catalog", 3),
    PRIZES_CATALOG("prizes_catalog", 3),
    TIERS_CATALOG("tiers_catalog", 3),

    // Priority 2
    PROFILE_SMILES_HISTORY("rewards_history", 2),
    CHALLENGE_PROGRESS("challenges_progress", 2),
    LIFETIME_SMILES("lifetime_smiles", 2),
    IN_OFF_BRUSHINGS_COUNT("brushing_in_vs_off", 2),
    PERSONAL_CHALLENGE("personal_challenge", 2),

    // Priority 1
    GAME_PROGRESS("game_progress", 1),
    BRUSH_SYNC_REMINDER("brush_sync_reminder", 1),
    AMAZON_DRS_STATUS("amazon_drs_status", 1),
    BRUSH_HEAD_STATUS("brush_head_status", 1),
    SHORT_TASK("short_task", 1), // does not exist on backend side
    PROFILE_TIER("profile_tier", 1); // not used in CC 3.0

    @VisibleForApp
    companion object {
        fun from(from: String): SynchronizableKey? = values().firstOrNull { it.value == from }

        fun findBy(priority: Int): List<SynchronizableKey> {
            return values().filter { it.priority == priority }
        }
    }
}
