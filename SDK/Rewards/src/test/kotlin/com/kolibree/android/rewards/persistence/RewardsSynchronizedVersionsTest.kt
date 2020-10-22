/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.persistence

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions.Companion.KEY_CHALLENGES
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions.Companion.KEY_CHALLENGE_PROGRESS
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions.Companion.KEY_LIFETIME_SMILES
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions.Companion.KEY_PROFILE_SMILES
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions.Companion.KEY_PROFILE_TIERS
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions.Companion.KEY_TIERS
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.mockito.Mock

class RewardsSynchronizedVersionsTest : BaseUnitTest() {
    @Mock
    lateinit var context: Context

    @Mock
    lateinit var prefs: SharedPreferences

    private lateinit var rewardsSynchronizedVersions: RewardsSynchronizedVersions

    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)

        rewardsSynchronizedVersions = RewardsSynchronizedVersions(context)
    }

    /*
    LIFETIME SMILES
     */

    @Test
    fun `lifetimeSmilesVersion asks for KEY_LIFETIME_SMILES with default value 0`() {
        rewardsSynchronizedVersions.lifetimeSmilesVersion()

        verify(prefs).getInt(KEY_LIFETIME_SMILES, 0)
    }

    @Test
    fun `setLifetimeSmilesVersion invokes putInt for KEY_LIFETIME_SMILES with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        rewardsSynchronizedVersions.setLifetimeSmilesVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(KEY_LIFETIME_SMILES, expectedValue)
            verify(editor).apply()
        }
    }

    /*
    CHALLENGES CATALOG
     */

    @Test
    fun `challengesCatalogVersion asks for KEY_CHALLENGES with default value 0`() {
        rewardsSynchronizedVersions.challengesCatalogVersion()

        verify(prefs).getInt(KEY_CHALLENGES, 0)
    }

    @Test
    fun `setChallengesCatalogVersion invokes putInt for KEY_CHALLENGES with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        rewardsSynchronizedVersions.setChallengesCatalogVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(KEY_CHALLENGES, expectedValue)
            verify(editor).apply()
        }
    }

    /*
    CHALLENGE PROGRESS
     */

    @Test
    fun `challengeProgressVersion asks for KEY_CHALLENGE_PROGRESS with default value 0`() {
        rewardsSynchronizedVersions.challengeProgressVersion()

        verify(prefs).getInt(KEY_CHALLENGE_PROGRESS, 0)
    }

    @Test
    fun `setChallengeProgressVersion invokes putInt for KEY_CHALLENGE_PROGRESS with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        rewardsSynchronizedVersions.setChallengeProgressVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(KEY_CHALLENGE_PROGRESS, expectedValue)
            verify(editor).apply()
        }
    }

    /*
    TIERS CATALOG
     */

    @Test
    fun `tiersCatalogVersion asks for KEY_TIERS with default value 0`() {
        rewardsSynchronizedVersions.tiersCatalogVersion()

        verify(prefs).getInt(KEY_TIERS, 0)
    }

    @Test
    fun `setTiersCatalogVersion invokes putInt for KEY_TIERS with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        rewardsSynchronizedVersions.setTiersCatalogVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(KEY_TIERS, expectedValue)
            verify(editor).apply()
        }
    }

    /*
    PROFILE TIERS
     */

    @Test
    fun `profileTiersVersion asks for KEY_PROFILE_TIERS with default value 0`() {
        rewardsSynchronizedVersions.profileTiersVersion()

        verify(prefs).getInt(KEY_PROFILE_TIERS, 0)
    }

    @Test
    fun `setProfileTiersVersion invokes putInt for KEY_PROFILE_TIERS with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        rewardsSynchronizedVersions.setProfileTiersVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(KEY_PROFILE_TIERS, expectedValue)
            verify(editor).apply()
        }
    }

    /*
    PROFILE SMILES
     */

    @Test
    fun `profileSmilesVersion asks for KEY_PROFILE_SMILES with default value 0`() {
        rewardsSynchronizedVersions.profileSmilesVersion()

        verify(prefs).getInt(KEY_PROFILE_SMILES, 0)
    }

    @Test
    fun `setProfileSmilesVersion invokes putInt for KEY_PROFILE_SMILES with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        rewardsSynchronizedVersions.setProfileSmilesVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(KEY_PROFILE_SMILES, expectedValue)
            verify(editor).apply()
        }
    }

    /*
    PRIZES
     */

    @Test
    fun `prizesVersion asks for KEY_PRIZES with default value 0`() {
        rewardsSynchronizedVersions.prizesVersion()

        verify(prefs).getInt(RewardsSynchronizedVersions.KEY_PRIZES, 0)
    }

    @Test
    fun `setPrizesVersion invokes putInt for KEY_PRIZES with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        rewardsSynchronizedVersions.setPrizesVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(RewardsSynchronizedVersions.KEY_PRIZES, expectedValue)
            verify(editor).apply()
        }
    }

    /*
    SMILES HISTORY
     */

    @Test
    fun `smilesHistoryVersion asks for KEY_SMILES_HISTORY with default value 0`() {
        rewardsSynchronizedVersions.smilesHistoryVersion()

        verify(prefs).getInt(RewardsSynchronizedVersions.KEY_SMILES_HISTORY, 0)
    }

    @Test
    fun `setSmilesHistoryVersion invokes putInt for KEY_SMILES_HISTORY with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        rewardsSynchronizedVersions.setSmilesHistoryVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(RewardsSynchronizedVersions.KEY_SMILES_HISTORY, expectedValue)
            verify(editor).apply()
        }
    }

    /*
    PERSONNAL CHALLENGE
     */

    @Test
    fun `personalChallengeVersion asks for KEY_PERSONAL_CHALLENGE with default value 0`() {
        rewardsSynchronizedVersions.personalChallengeVersion()

        verify(prefs).getInt(RewardsSynchronizedVersions.KEY_PERSONAL_CHALLENGE, 0)
    }

    @Test
    fun `setPersonalChallengeVersion invokes putInt for KEY_PERSONAL_CHALLENGE with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        rewardsSynchronizedVersions.setPersonalChallengeVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(RewardsSynchronizedVersions.KEY_PERSONAL_CHALLENGE, expectedValue)
            verify(editor).apply()
        }
    }

    private fun mockPrefsEditor(): SharedPreferences.Editor {
        val editor: SharedPreferences.Editor = mock()

        whenever(prefs.edit()).thenReturn(editor)
        whenever(editor.putInt(any(), any())).thenReturn(editor)

        return editor
    }
}
