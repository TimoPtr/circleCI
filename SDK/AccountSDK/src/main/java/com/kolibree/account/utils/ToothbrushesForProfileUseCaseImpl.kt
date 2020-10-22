/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.utils

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.mac
import com.kolibree.android.sdk.connection.user.isOwnedByOrShared
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

/** [ToothbrushesForProfileUseCase] implementation */
internal class ToothbrushesForProfileUseCaseImpl
@Inject constructor(
    private val toothbrushRepository: ToothbrushRepository,
    private val currentProfileProvider: CurrentProfileProvider,
    private val serviceProvider: ServiceProvider
) : ToothbrushesForProfileUseCase {
    override fun profileAccountToothbrushesOnceAndStream(profileId: Long): Flowable<List<AccountToothbrush>> {
        return accountToothbrushesStream()
            .map { toothbrushes ->
                toothbrushes.filter { toothbrush ->
                    toothbrush.profileId.isOwnedByOrShared(profileId)
                }
            }
    }

    override fun currentProfileAccountToothbrushesOnceAndStream(): Flowable<List<AccountToothbrush>> {
        return Flowable.combineLatest(
            accountToothbrushesStream(),
            currentProfileProvider.currentProfileFlowable(),
            BiFunction<List<AccountToothbrush>, Profile, List<AccountToothbrush>>
            { toothbrushes, activeProfile ->
                toothbrushes
                    .filter { toothbrush -> toothbrush.profileId.isOwnedByOrShared(activeProfile.id) }
            }
        )
    }

    override fun profileToothbrushesOnceAndStream(profileId: Long): Flowable<List<KLTBConnection>> {
        return Flowable.combineLatest(
            profileAccountToothbrushesOnceAndStream(profileId),
            connectionsStream(),
            BiFunction<List<AccountToothbrush>, List<KLTBConnection>, List<KLTBConnection>>
            { ownedToothbrushes, knownConnections ->
                ownedToothbrushes.mapNotNull { ownedToothbrush ->
                    knownConnections.firstOrNull { it.mac() == ownedToothbrush.mac }
                }
            }
        )
    }

    override fun currentProfileToothbrushesOnceAndStream(): Flowable<List<KLTBConnection>> {
        return Flowable.combineLatest(
            currentProfileAccountToothbrushesOnceAndStream(),
            connectionsStream(),
            BiFunction<List<AccountToothbrush>, List<KLTBConnection>, List<KLTBConnection>>
            { toothbrushes, knownConnections ->
                toothbrushes.mapNotNull { ownedToothbrush ->
                    knownConnections.firstOrNull { it.mac() == ownedToothbrush.mac }
                }
            }
        )
    }

    private fun connectionsStream() = serviceStream().switchMap { it.knownConnectionsOnceAndStream }

    /**
     * @return Flowable that will emit a [KolibreeService] each time it's recreated. A disconnect
     * won't terminate the stream
     */
    private fun serviceStream(): Flowable<KolibreeService> {
        return serviceProvider.connectStream()
            .filter { it is ServiceConnected }
            .map { (it as ServiceConnected).service }
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    /**
     * @return Flowable that will emit a new [List]<[AccountToothbrush]> each time the database
     * is updated
     */
    private fun accountToothbrushesStream() = toothbrushRepository.listAllOnceAndStream()
}
