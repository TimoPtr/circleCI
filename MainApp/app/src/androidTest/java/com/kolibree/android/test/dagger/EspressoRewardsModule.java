package com.kolibree.android.test.dagger;

import static org.mockito.Mockito.mock;

import android.content.Context;
import androidx.room.Room;
import com.kolibree.android.app.dagger.AppScope;
import com.kolibree.android.commons.interfaces.RemoteBrushingsProcessor;
import com.kolibree.android.rewards.EarnPointsChallengeUseCase;
import com.kolibree.android.rewards.EarnPointsChallengeUseCaseImpl;
import com.kolibree.android.rewards.ProfileProgress;
import com.kolibree.android.rewards.RewardsRemoteBrushingProcessor;
import com.kolibree.android.rewards.RewardsUseCaseModule;
import com.kolibree.android.rewards.feedback.FeedbackRepository;
import com.kolibree.android.rewards.feedback.FirstLoginDateProvider;
import com.kolibree.android.rewards.feedback.FirstLoginDateUpdater;
import com.kolibree.android.rewards.morewaystoearnpoints.di.MoreWaysToEarnPointsModule;
import com.kolibree.android.rewards.persistence.LifetimeStatsRepository;
import com.kolibree.android.rewards.persistence.LifetimeStatsRoomRepository;
import com.kolibree.android.rewards.persistence.ProfileSmilesRepository;
import com.kolibree.android.rewards.persistence.RewardsPersistenceModule;
import com.kolibree.android.rewards.persistence.RewardsRepository;
import com.kolibree.android.rewards.persistence.RewardsRepositoryImpl;
import com.kolibree.android.rewards.persistence.RewardsRoomDatabase;
import com.kolibree.android.rewards.personalchallenge.data.api.PersonalChallengeApi;
import com.kolibree.android.rewards.personalchallenge.data.persistence.PersonalChallengeDao;
import com.kolibree.android.rewards.personalchallenge.di.BrushingEventsModule;
import com.kolibree.android.rewards.personalchallenge.domain.logic.PersonalChallengeV1Repository;
import com.kolibree.android.rewards.personalchallenge.domain.logic.PersonalChallengeV1RepositoryImpl;
import com.kolibree.android.rewards.synchronization.RewardsApi;
import com.kolibree.android.rewards.synchronization.redeem.RedeemNetworkService;
import com.kolibree.android.rewards.synchronization.transfer.TransferNetworkService;
import com.kolibree.android.test.utils.rewards.FakeFeedbackRepository;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@SuppressWarnings("KotlinInternalInJava")
@Module(
    includes = {
      RewardsUseCaseModule.class,
      RewardsPersistenceModule.class,
      BrushingEventsModule.class,
      MoreWaysToEarnPointsModule.class
    })
public abstract class EspressoRewardsModule {
  @Binds
  @ProfileProgress
  abstract ProfileSmilesRepository bindsProfileSmilesRepository(RewardsRepositoryImpl impl);

  @Binds
  abstract RewardsRepository bindsRewardsRepository(RewardsRepositoryImpl rewardsRepositoryImpl);

  @Binds
  abstract RemoteBrushingsProcessor bindsRewardsRemoteBrushingProcessor(
      RewardsRemoteBrushingProcessor impl);

  @Provides
  @AppScope
  static RewardsRoomDatabase providesRewardsDatabase(Context context) {
    return Room.inMemoryDatabaseBuilder(context, RewardsRoomDatabase.class)
        .allowMainThreadQueries()
        .build();
  }

  @Provides
  static RewardsApi providesRewardsApi() {
    return mock(RewardsApi.class);
  }

  @Provides
  @AppScope
  static RedeemNetworkService providesRedeemNetworkService() {
    return mock(RedeemNetworkService.class);
  }

  @Provides
  @AppScope
  static TransferNetworkService providesTransferNetworkService() {
    return mock(TransferNetworkService.class);
  }

  @Provides
  @AppScope
  static FirstLoginDateUpdater providesFirstRunDateUpdater() {
    return mock(FirstLoginDateUpdater.class);
  }

  @Provides
  @AppScope
  static FirstLoginDateProvider providesFirstRunDateProvider() {
    return mock(FirstLoginDateProvider.class);
  }

  @Provides
  @AppScope
  static PersonalChallengeApi providesPersonalChallengeApi() {
    return mock(PersonalChallengeApi.class);
  }

  @Provides
  static PersonalChallengeV1Repository providesPersonalChallengeV1Repository(
      PersonalChallengeV1RepositoryImpl impl) {
    return impl;
  }

  @Provides
  static PersonalChallengeDao providesPersonalChallengeDao(RewardsRoomDatabase rewardsDatabase) {
    return rewardsDatabase.personalChallengeDao();
  }

  @Binds
  abstract LifetimeStatsRepository bindsLifetimeStatsRepository(LifetimeStatsRoomRepository impl);

  @Binds
  abstract EarnPointsChallengeUseCase bindsEarnPointsUseCase(EarnPointsChallengeUseCaseImpl impl);

  @AppScope
  @Provides
  static FakeFeedbackRepository providesFakeFeedbackRepository() {
    return new FakeFeedbackRepository();
  }

  @Provides
  static FeedbackRepository providesFeedbackRepository(FakeFeedbackRepository fakeRepository) {
    return fakeRepository;
  }
}
