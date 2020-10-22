package com.kolibree.android.rewards.persistence

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.persistence.BasePreferencesImpl
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Stores information about the rewards model versions this app holds
 */
internal class RewardsSynchronizedVersions @Inject constructor(context: Context) :
    BasePreferencesImpl(context), Truncable {
    @VisibleForTesting
    companion object {
        const val KEY_CHALLENGES = "challenges_catalog"
        const val KEY_CHALLENGE_PROGRESS = "challenges_progress"
        const val KEY_TIERS = "tiers_catalog"
        const val KEY_PROFILE_TIERS = "profile_tiers"
        const val KEY_PROFILE_SMILES = "profile_smiles"
        const val KEY_PRIZES = "prizes_catalog"
        const val KEY_SMILES_HISTORY = "smiles_history"
        const val KEY_PERSONAL_CHALLENGE = "personal_challenge"
        const val KEY_LIFETIME_SMILES = "lifetime_smiles"
    }

    fun challengesCatalogVersion() = prefs.getInt(KEY_CHALLENGES, 0)
    fun challengeProgressVersion() = prefs.getInt(KEY_CHALLENGE_PROGRESS, 0)
    fun tiersCatalogVersion() = prefs.getInt(KEY_TIERS, 0)
    fun profileTiersVersion() = prefs.getInt(KEY_PROFILE_TIERS, 0)
    fun profileSmilesVersion() = prefs.getInt(KEY_PROFILE_SMILES, 0)
    fun prizesVersion() = prefs.getInt(KEY_PRIZES, 0)
    fun smilesHistoryVersion() = prefs.getInt(KEY_SMILES_HISTORY, 0)
    fun personalChallengeVersion() = prefs.getInt(KEY_PERSONAL_CHALLENGE, 0)
    fun lifetimeSmilesVersion() = prefs.getInt(KEY_LIFETIME_SMILES, 0)

    fun setChallengesCatalogVersion(newVersion: Int) =
        prefsEditor.putInt(KEY_CHALLENGES, newVersion).apply()

    fun setChallengeProgressVersion(newVersion: Int) =
        prefsEditor.putInt(KEY_CHALLENGE_PROGRESS, newVersion).apply()

    fun setTiersCatalogVersion(newVersion: Int) = prefsEditor.putInt(KEY_TIERS, newVersion).apply()
    fun setProfileTiersVersion(newVersion: Int) =
        prefsEditor.putInt(KEY_PROFILE_TIERS, newVersion).apply()

    fun setProfileSmilesVersion(newVersion: Int) =
        prefsEditor.putInt(KEY_PROFILE_SMILES, newVersion).apply()

    fun setPrizesVersion(newVersion: Int) = prefsEditor.putInt(KEY_PRIZES, newVersion).apply()
    fun setSmilesHistoryVersion(newVersion: Int) = prefsEditor.putInt(KEY_SMILES_HISTORY, newVersion).apply()
    fun setPersonalChallengeVersion(newVersion: Int) = prefsEditor.putInt(KEY_PERSONAL_CHALLENGE, newVersion).apply()
    fun setLifetimeSmilesVersion(newVersion: Int) = prefsEditor.putInt(KEY_LIFETIME_SMILES, newVersion).apply()

    override fun truncate(): Completable = Completable.fromAction { clear() }
}
