package com.kolibree.android.rewards.synchronization.challengeprogress

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.models.CategoryEntity
import com.kolibree.android.rewards.models.ChallengeEntity
import com.kolibree.android.rewards.persistence.CategoriesDao
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.rewards.synchronization.challenges.ChallengesCatalogApi
import com.kolibree.android.rewards.synchronization.challenges.ChallengesSynchronizableCatalogDatastore
import com.kolibree.android.synchronizator.models.SynchronizableCatalog
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.mockito.Mock

internal class ChallengesSynchronizableCatalogDatastoreTest : BaseUnitTest() {

    @Mock
    lateinit var categoriesDao: CategoriesDao

    @Mock
    lateinit var rewardsSynchronizedVersions: RewardsSynchronizedVersions

    private lateinit var datastore: ChallengesSynchronizableCatalogDatastore

    override fun setup() {
        super.setup()

        datastore = ChallengesSynchronizableCatalogDatastore(categoriesDao, rewardsSynchronizedVersions)
    }

    /*
    UPDATE VERSION
     */

    @Test
    fun `update version invokes setChallengesCatalogVersion`() {
        val expectedVersion = 432
        datastore.updateVersion(expectedVersion)

        verify(rewardsSynchronizedVersions).setChallengesCatalogVersion(expectedVersion)
    }

    /*
    REPLACE
     */

    @Test
    fun `replace does nothing when parameter is not ChallengeProgressProfileCatalogInternal`() {
        datastore.replace(object : SynchronizableCatalog {})

        verifyNoMoreInteractions(categoriesDao)
    }

    @Test
    fun `replace invokes categoryDao replace with categories`() {
        val challenge1 = ChallengeEntity(
            description = "Synchronize 1 offline brushing",
            smilesReward = 3,
            id = 5,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Badge_FirstOfflineBrushing.png",
            name = "First offline brushing",
            greetingMessage = "Congratulations! You've synchronized 1 offline brushing.",
            internalCategory = null,
            action = null
        )

        val challenge2 = ChallengeEntity(
            description = "Try the Test Brushing",
            smilesReward = 3,
            id = 6,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Badge_FirstSmartBrushingAnalyzer.png",
            name = "First Test Brushing",
            greetingMessage = "Congratulations! You've done your first Smart Brushing Analysis.",
            internalCategory = null,
            action = null
        )

        val category1Name = "Category 1"
        val category1 = CategoryEntity(name = category1Name, challenges = listOf(challenge1, challenge2))

        val challenge3 = ChallengeEntity(
            description = "Try to brush your teeth with Coach",
            smilesReward = 5,
            id = 1,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Badge_Coach_1st.png",
            name = "First Coach",
            greetingMessage = "Congratulations! You've done your first brushing session with Coach",
            internalCategory = null,
            action = null
        )

        val category2Name = "Category 2"
        val category2 = CategoryEntity(name = category2Name, challenges = listOf(challenge3))

        val catalog = ChallengesCatalogApi(language = "Ignored", categories = listOf(category1, category2))

        datastore.replace(catalog)

        verify(categoriesDao).replace(listOf(category1, category2))
    }
}
