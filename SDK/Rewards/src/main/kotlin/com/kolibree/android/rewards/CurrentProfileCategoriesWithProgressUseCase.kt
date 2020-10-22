package com.kolibree.android.rewards

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.rewards.models.CategoryWithProgress
import com.kolibree.android.rewards.persistence.RewardsRepository
import io.reactivex.Flowable
import javax.inject.Inject

@Keep
interface CurrentProfileCategoriesWithProgressUseCase {

    fun categoriesWithProgress(): Flowable<List<CategoryWithProgress>>
}

internal class CurrentProfileCategoriesWithProgressUseCaseImpl
@Inject constructor(
    private val currentProfileProvider: CurrentProfileProvider,
    private val rewardsRepository: RewardsRepository
) : CurrentProfileCategoriesWithProgressUseCase {

    override fun categoriesWithProgress(): Flowable<List<CategoryWithProgress>> {
        return currentProfileProvider.currentProfileFlowable()
            .distinctUntilChanged()
            .switchMap { profile -> rewardsRepository.categoriesWithChallengeProgress(profile.id) }
    }
}
