/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.ProfileWithSmilesUseCase
import com.kolibree.android.rewards.persistence.RewardsRepository
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.core.AvatarCache
import com.kolibree.sdkws.profile.ProfileManager
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

@VisibleForApp
class SmilesHistoryUseCase @Inject constructor(
    private val profileManager: ProfileManager,
    private val currentProfileProvider: CurrentProfileProvider,
    private val rewardsRepository: RewardsRepository,
    private val brushingsRepository: BrushingsRepository,
    private val avatarCache: AvatarCache,
    private val profileWithSmilesUseCase: ProfileWithSmilesUseCase
) {

    fun smilesHistoryStream(): Flowable<Pair<List<SmilesHistoryItem>, List<ProfileSmilesItemResources>>> =
        currentProfileProvider.currentProfileFlowable()
            .switchMap { profile ->
                Flowable.combineLatest(
                    retrieveSmilesHistory(profile),
                    retrieveOtherProfilesSmiles(profile),
                    BiFunction<
                        List<SmilesHistoryItem>,
                        List<ProfileSmilesItemResources>,
                        Pair<List<SmilesHistoryItem>, List<ProfileSmilesItemResources>>>
                    { smilesHistoryItems, profileSmilesItemViewModel ->
                        Pair(smilesHistoryItems, profileSmilesItemViewModel)
                    })
            }

    @VisibleForTesting
    internal fun retrieveSmilesHistory(currentProfile: Profile): Flowable<List<SmilesHistoryItem>> =
    // TODO pagination is not available on back end side
        // TODO take 5 (on click five more with a repeat when (with a counter) if we want to deal with pagination)
        rewardsRepository.smilesHistoryEvents(currentProfile.id).mapToItem(
            profileManager,
            brushingsRepository,
            rewardsRepository,
            currentProfile.id
        ).startWith(emptyList<SmilesHistoryItem>())

    @VisibleForTesting
    internal fun retrieveOtherProfilesSmiles(currentProfile: Profile): Flowable<List<ProfileSmilesItemResources>> =
        profileWithSmilesUseCase.retrieveOtherProfilesSmilesStream(currentProfile)
            .map { profileWithSmiles ->
                profileWithSmiles.map { profileWithSmile ->
                    val avatarUrl = avatarCache.getAvatarUrl(profileWithSmile)
                    ProfileSmilesItemResources(
                        avatarUrl,
                        profileWithSmile.firstName,
                        profileWithSmile.smiles.toString()
                    )
                }
            }.startWith(emptyList<ProfileSmilesItemResources>())
}
