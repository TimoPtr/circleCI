/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.partnerships.data.api.HEADSPACE_DISCOUNT_CODE
import com.kolibree.android.partnerships.data.api.HEADSPACE_POINTS_THRESHOLD
import com.kolibree.android.partnerships.data.api.HEADSPACE_REDEEM_URL
import com.kolibree.android.partnerships.data.api.PartnershipApiFake
import com.kolibree.android.partnerships.data.api.PartnershipApiMapper
import com.kolibree.android.partnerships.data.api.model.PartnershipData
import com.kolibree.android.partnerships.data.persistence.PartnershipDao
import com.kolibree.android.partnerships.data.persistence.PartnershipPersistenceMapper
import com.kolibree.android.partnerships.data.persistence.model.PartnershipEntity
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.partnerships.domain.model.Partner.HEADSPACE
import com.kolibree.android.partnerships.domain.model.Partner.TEST_ONLY
import com.kolibree.android.partnerships.domain.model.PartnershipStatus
import com.kolibree.android.partnerships.headspace.data.api.HeadspaceApiMapper
import com.kolibree.android.partnerships.headspace.data.api.KEY_DISCOUNT_CODE
import com.kolibree.android.partnerships.headspace.data.api.KEY_REDEEM_URL
import com.kolibree.android.partnerships.headspace.data.api.KEY_STATUS
import com.kolibree.android.partnerships.headspace.data.api.VALUE_STATUS_UNLOCKED
import com.kolibree.android.partnerships.headspace.data.persistence.HeadspacePersistenceMapper
import com.kolibree.android.partnerships.headspace.data.persistence.model.HeadspacePartnershipEntity
import com.kolibree.android.partnerships.headspace.data.persistence.model.HeadspacePartnershipEntity.State
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.InProgress
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Inactive
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Unlocked
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class PartnershipStatusRepositoryImplTest : BaseUnitTest() {

    private val api = PartnershipApiFake

    private val apiMappers = mapOf(
        HEADSPACE to HeadspaceApiMapper,
        TEST_ONLY to TestOnlyApiMapper
    )

    private val persistenceMappers = mapOf(
        HEADSPACE to HeadspacePersistenceMapper,
        TEST_ONLY to TestOnlyPersistenceMapper
    )

    private lateinit var daos: Map<Partner, Dao>

    private lateinit var repository: PartnershipStatusRepository

    override fun setup() {
        super.setup()
        api.setStateSequence(emptyList())
        daos = Partner.values().map { it to Dao() }.toMap()

        repository = PartnershipStatusRepositoryImpl(
            api, apiMappers, daos, persistenceMappers
        )
    }

    @Test
    fun `empty response from refresh API is a no-op operation`() {
        val account = 100L
        val profile = 1000L

        api.setState(profile, emptyMap())

        repository.refreshPartnerships(account, profile).test().assertComplete()

        forEachPartner { partner ->
            assertTrue(daos[partner]!!.statusStreamPerProfile.isEmpty())
            repository.getPartnershipStatus(account, profile, partner).test().assertNoValues()
        }
    }

    @Test
    fun `refresh API payload goes to respective persistence storage`() {
        val account = 100L
        val profile = 1000L

        api.setState(profile, api.defaultState())
        val statusObservers = Partner.values().map { partner ->
            assertTrue(daos[partner]!!.statusStreamPerProfile.isEmpty())
            partner to repository.getPartnershipStatus(account, profile, partner).test()
        }.toMap()

        repository.refreshPartnerships(account, profile).test().assertComplete()

        forEachPartner { partner ->
            assertEquals(1, numberOfProfilesWithStates(partner))
        }

        statusObservers[HEADSPACE]!!.assertValue(
            InProgress(profile, HEADSPACE_POINTS_THRESHOLD, HEADSPACE_POINTS_THRESHOLD)
        )
        assertEquals(
            HeadspacePartnershipEntity(
                profile,
                status = State.IN_PROGRESS,
                pointsNeeded = HEADSPACE_POINTS_THRESHOLD,
                pointsThreshold = HEADSPACE_POINTS_THRESHOLD
            ),
            persistedEntity(profile, HEADSPACE)
        )

        statusObservers[TEST_ONLY]!!.assertValue(TestOnlyStatus(profile))
        assertEquals(
            TestOnlyEntity(profile),
            persistedEntity(profile, TEST_ONLY)
        )
    }

    @Test
    fun `refresh API payload is kept for each profile`() {
        val account = 100L
        val profile1 = 1000L
        val profile2 = 1010L

        api.setState(profile1, api.defaultState())
        val profile1Observer = repository.getPartnershipStatus(account, profile1, HEADSPACE).test()

        assertFalse(profileHasState(profile1))

        val profile2Observer = repository.getPartnershipStatus(account, profile2, HEADSPACE).test()

        assertFalse(profileHasState(profile2))

        repository.refreshPartnerships(account, profile1).test().assertComplete()

        assertTrue(profileHasState(profile1))
        assertFalse(profileHasState(profile2))
        profile1Observer.assertValue(
            InProgress(profile1, HEADSPACE_POINTS_THRESHOLD, HEADSPACE_POINTS_THRESHOLD)
        )
        profile2Observer.assertNoValues()

        api.setState(profile2, api.defaultState())
        repository.refreshPartnerships(account, profile2).test().assertComplete()

        assertTrue(profileHasState(profile1))
        assertTrue(profileHasState(profile2))
        profile1Observer.assertValue(
            InProgress(profile1, HEADSPACE_POINTS_THRESHOLD, HEADSPACE_POINTS_THRESHOLD)
        )
        profile2Observer.assertValue(
            InProgress(profile2, HEADSPACE_POINTS_THRESHOLD, HEADSPACE_POINTS_THRESHOLD)
        )
    }

    private fun profileHasState(profile1: Long, partner: Partner = HEADSPACE) =
        daos[partner]!!.statusStreamPerProfile[profile1]!!.hasValue()

    @Test
    fun `persistent state for profile is updated if refresh API returns an update`() {
        val account = 100L
        val profile = 1000L

        api.setState(profile, api.defaultState())
        val profileObserver =
            repository.getPartnershipStatus(account, profile, HEADSPACE).test()

        repository.refreshPartnerships(account, profile).test().assertComplete()

        assertEquals(1, numberOfProfilesWithStates(HEADSPACE))
        profileObserver.assertValue(
            InProgress(profile, HEADSPACE_POINTS_THRESHOLD, HEADSPACE_POINTS_THRESHOLD)
        )
        assertEquals(
            HeadspacePartnershipEntity(
                profile,
                status = State.IN_PROGRESS,
                pointsNeeded = HEADSPACE_POINTS_THRESHOLD,
                pointsThreshold = HEADSPACE_POINTS_THRESHOLD
            ),
            persistedEntity(profile, HEADSPACE)
        )

        api.setState(
            profile, mapOf(
                HEADSPACE to mapOf(
                    KEY_STATUS to VALUE_STATUS_UNLOCKED,
                    KEY_DISCOUNT_CODE to HEADSPACE_DISCOUNT_CODE,
                    KEY_REDEEM_URL to HEADSPACE_REDEEM_URL
                )
            )
        )
        repository.refreshPartnerships(account, profile).test().assertComplete()

        assertEquals(1, numberOfProfilesWithStates(HEADSPACE))
        assertEquals(
            HeadspacePartnershipEntity(
                profile,
                status = State.UNLOCKED,
                discountCode = HEADSPACE_DISCOUNT_CODE,
                redeemUrl = HEADSPACE_REDEEM_URL
            ),
            persistedEntity(profile, HEADSPACE)
        )
        profileObserver.assertValues(
            InProgress(profile, HEADSPACE_POINTS_THRESHOLD, HEADSPACE_POINTS_THRESHOLD),
            Unlocked(profile, HEADSPACE_DISCOUNT_CODE, HEADSPACE_REDEEM_URL)
        )
    }

    /*
    disablePartnership
     */
    @Test
    fun `disablePartnership emits Inactive when api disablePartnership is successful`() {
        val account = 100L
        val profile = 1000L
        val partner = HEADSPACE

        val profileObserver = repository.getPartnershipStatus(account, profile, partner).test()

        api.setState(profile, api.defaultState())

        repository.disablePartnership(
            accountId = account,
            profileId = profile,
            partner = partner
        )
            .test()
            .assertComplete()

        profileObserver.assertValues(
            Inactive(profile)
        )
    }

    /*
    unlockPartnership
     */
    @Test
    fun `unlockPartnership emits Unlocked when api unlockPartnership is successful`() {
        val account = 100L
        val profile = 1000L
        val partner = HEADSPACE

        val profileObserver = repository.getPartnershipStatus(account, profile, partner).test()

        api.setState(profile, api.defaultState())

        repository.unlockPartnership(
            accountId = account,
            profileId = profile,
            partner = partner
        )
            .test()
            .assertComplete()

        profileObserver.assertValues(
            Unlocked(profile, HEADSPACE_DISCOUNT_CODE, HEADSPACE_REDEEM_URL)
        )
    }

    @Test
    fun `unlockPartnership emits Throwable when api call throws a different error`() {
        val account = 100L
        val profile = 1000L
        val partner = HEADSPACE

        api.setError(httpErrorCode = 403)

        repository.unlockPartnership(
            accountId = account,
            profileId = profile,
            partner = partner
        )
            .test()
            .assertError(ApiError::class.java)
    }

    @Test
    fun `unlockPartnership emits ApiError when api call returns ApiError with code 115 and httpcode is 400`() {
        val account = 100L
        val profile = 1000L
        val partner = HEADSPACE

        api.setApiError(httpErrorCode = 400, internalErrorCode = 115)

        repository.unlockPartnership(
            accountId = account,
            profileId = profile,
            partner = partner
        )
            .test()
            .assertError(ApiError::class.java)
    }

    @Test
    fun `unlockPartnership emits DiscountCodeNotAvailableException when api call returns ApiError with code 115 and httpcode is 503`() {
        val account = 100L
        val profile = 1000L
        val partner = HEADSPACE

        api.setApiError(httpErrorCode = 503, internalErrorCode = 115)

        repository.unlockPartnership(
            accountId = account,
            profileId = profile,
            partner = partner
        )
            .test()
            .assertError(DiscountCodeNotAvailableException::class.java)
    }

    /*
    Utils
     */

    private fun numberOfProfilesWithStates(partner: Partner) =
        daos[partner]!!.statusStreamPerProfile.size

    private fun persistedEntity(profile: Long, partner: Partner) =
        daos[partner]!!.statusStreamPerProfile[profile]!!.blockingFirst()

    private inline fun forEachPartner(block: (Partner) -> Unit) = Partner.values().forEach(block)
}

private data class TestOnlyStatus(override val profileId: Long) : PartnershipStatus {

    override val partner = TEST_ONLY
}

private data class TestOnlyEntity(override val profileId: Long) : PartnershipEntity

private object TestOnlyApiMapper : PartnershipApiMapper {

    override fun apiResponseToStatus(profileId: Long, data: PartnershipData): PartnershipStatus {
        return TestOnlyStatus(profileId)
    }
}

private object TestOnlyPersistenceMapper : PartnershipPersistenceMapper {

    override fun statusToEntity(status: PartnershipStatus): PartnershipEntity {
        if (status !is TestOnlyStatus) throw IllegalArgumentException()
        return TestOnlyEntity(status.profileId)
    }

    override fun entityToStatus(entity: PartnershipEntity): PartnershipStatus {
        if (entity !is TestOnlyEntity) throw IllegalArgumentException()
        return TestOnlyStatus(entity.profileId)
    }
}

private class Dao : PartnershipDao {

    val statusStreamPerProfile = mutableMapOf<Long, BehaviorProcessor<PartnershipEntity>>()

    override fun insertOrReplace(entity: PartnershipEntity) {
        get(entity.profileId).onNext(entity)
    }

    override fun findBy(profileId: Long): Flowable<PartnershipEntity> = get(profileId)!!.hide()

    override fun truncate(): Completable = Completable.complete()

    private fun get(profileId: Long): BehaviorProcessor<PartnershipEntity> =
        statusStreamPerProfile[profileId] ?: run {
            statusStreamPerProfile[profileId] = BehaviorProcessor.create()
            statusStreamPerProfile[profileId]!!
        }
}
