package com.kolibree.android.app.dagger;

import static com.kolibree.android.test.dagger.EspressoSdkScannerModule.BLE_SCANNER;

import android.app.Application;
import android.app.job.JobScheduler;
import android.content.SharedPreferences;
import com.kolibree.android.accountinternal.CurrentProfileProvider;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore;
import com.kolibree.android.app.loader.GameService;
import com.kolibree.android.app.loader.repo.AssetBundlePreferences;
import com.kolibree.android.app.loader.repo.AssetBundleRepository;
import com.kolibree.android.app.loader.repo.api.AssetBundleApi;
import com.kolibree.android.app.location.LocationActionChecker;
import com.kolibree.android.app.ui.kolibree_pro.KolibreeProReminders;
import com.kolibree.android.feature.FeatureToggle;
import com.kolibree.android.location.LocationStatus;
import com.kolibree.android.network.environment.EnvironmentManager;
import com.kolibree.android.network.errorhandler.RemoteAccountDoesNotExistDetector;
import com.kolibree.android.network.retrofit.DeviceParameters;
import com.kolibree.android.network.utils.NetworkChecker;
import com.kolibree.android.offlinebrushings.persistence.EspressoOfflineBrushingsRepositoriesModule;
import com.kolibree.android.offlinebrushings.persistence.OrphanBrushingRepository;
import com.kolibree.android.offlinebrushings.sync.LastSyncObservable;
import com.kolibree.android.partnerships.data.PartnershipStatusRepository;
import com.kolibree.android.partnerships.data.api.PartnershipApiFake;
import com.kolibree.android.persistence.SessionFlags;
import com.kolibree.android.processedbrushings.CheckupCalculator;
import com.kolibree.android.rewards.feedback.FirstLoginDateProvider;
import com.kolibree.android.rewards.persistence.LifetimeSmilesDao;
import com.kolibree.android.rewards.synchronization.challengeprogress.ChallengeProgressSynchronizableReadOnlyDatastore;
import com.kolibree.android.rewards.synchronization.challenges.ChallengesSynchronizableCatalogDatastore;
import com.kolibree.android.rewards.synchronization.prizes.PrizesSynchronizableCatalogDatastore;
import com.kolibree.android.rewards.synchronization.profilesmiles.ProfileSmilesSynchronizableReadOnlyDatastore;
import com.kolibree.android.rewards.synchronization.profilesmileshistory.ProfileSmilesHistorySynchronizableReadOnlyDatastore;
import com.kolibree.android.rewards.synchronization.profiletier.ProfileTierSynchronizableReadOnlyDatastore;
import com.kolibree.android.rewards.synchronization.redeem.RedeemNetworkService;
import com.kolibree.android.rewards.synchronization.tiers.TiersSynchronizableCatalogDatastore;
import com.kolibree.android.rewards.synchronization.transfer.TransferNetworkService;
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase;
import com.kolibree.android.sdk.core.KLTBConnectionPool;
import com.kolibree.android.sdk.core.KLTBConnectionProvider;
import com.kolibree.android.sdk.core.KolibreeService;
import com.kolibree.android.sdk.core.ServiceProvider;
import com.kolibree.android.sdk.location.LocationStatusListener;
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository;
import com.kolibree.android.sdk.plaqless.DspAwaker;
import com.kolibree.android.sdk.scan.ToothbrushScanner;
import com.kolibree.android.shop.data.persitence.CartDao;
import com.kolibree.android.shop.data.repo.CartRepository;
import com.kolibree.android.synchronization.SynchronizationStateUseCase;
import com.kolibree.android.test.Paparazzi;
import com.kolibree.android.test.dagger.EspressoSdkComponent;
import com.kolibree.android.test.dagger.FakeQuestionOfTheDayRepository;
import com.kolibree.android.test.utils.AttachUnityPlayerWrapper;
import com.kolibree.android.test.utils.rewards.FakeFeedbackRepository;
import com.kolibree.charts.DashboardCalculatorView;
import com.kolibree.charts.persistence.repo.StatRepository;
import com.kolibree.crypto.SecurityKeeper;
import com.kolibree.pairing.assistant.PairingAssistant;
import com.kolibree.sdkws.account.AccountManager;
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository;
import com.kolibree.sdkws.calendar.logic.CalendarBrushingsUseCase;
import com.kolibree.sdkws.core.GruwareRepository;
import com.kolibree.sdkws.core.OnUserLoggedInCallback;
import com.kolibree.sdkws.core.avro.AvroFileUploader;
import com.kolibree.sdkws.profile.ProfileManager;
import com.kolibree.sdkws.utils.ApiSDKUtils;
import com.kolibree.sdkws.utils.ProfileUtils;
import com.kolibree.statsoffline.models.api.AggregatedStatsRepository;
import dagger.BindsInstance;
import java.util.Set;
import javax.inject.Named;

/** Created by miguelaragues on 8/8/17. */
@SuppressWarnings("KotlinInternalInJava")
public interface BaseEspressoAppComponent extends AppComponent {

  Paparazzi paparazzi();

  @Named(BLE_SCANNER)
  ToothbrushScanner toothbrushBleScanner();

  KolibreeService kolibreeService();

  DeviceParameters deviceParameters();

  BrushingsRepository brushingsRepository();

  ToothbrushRepository toothbrushRepository();

  SharedPreferences sharedPreferences();

  OrphanBrushingRepository orphanBrushingRepository();

  NetworkChecker networkChecker();

  JobScheduler jobScheduler();

  ApiSDKUtils apiSdkUtils();

  AvroFileUploader avroFileUploader();

  ProfileUtils profileUtils();

  ProfileManager profileManager();

  GruwareRepository gruwareRepository();

  DashboardCalculatorView dashboardCalculatorView();

  ServiceProvider serviceProvider();

  PairingAssistant pairingAssistant();

  KLTBConnectionProvider connectionProvider();

  EnvironmentManager environmentManager();

  KolibreeProReminders kolibreeProReminders();

  LastSyncObservable lastSyncObservable();

  AccountDatastore accountDatastore();

  CurrentProfileProvider currentProfileProvider();

  StatRepository statRepository();

  ChallengeProgressSynchronizableReadOnlyDatastore challengeProgressDatastore();

  ChallengesSynchronizableCatalogDatastore challengesCatalogDatastore();

  LocationActionChecker locationActionChecker();

  LocationStatus locationStatus();

  GameService gameService();

  AssetBundleRepository assetBundleRepository();

  AssetBundlePreferences assetBundlePreferences();

  AssetBundleApi assetBundleApi();

  ProfileSmilesSynchronizableReadOnlyDatastore profileSmilesDatastore();

  ProfileSmilesHistorySynchronizableReadOnlyDatastore profileSmilesHistoryDatastore();

  ProfileTierSynchronizableReadOnlyDatastore profileTierDatastore();

  TiersSynchronizableCatalogDatastore tierDatastore();

  PrizesSynchronizableCatalogDatastore prizesDatastore();

  RedeemNetworkService redeemNetworkService();

  TransferNetworkService transferNetworkService();

  AttachUnityPlayerWrapper attachUnityPlayerWrapper();

  CalendarBrushingsUseCase calendarBrushingsUseCase();

  AggregatedStatsRepository aggregatedStatsRepository();

  RemoteAccountDoesNotExistDetector remoteAccountDoesNotExistDetector();

  OnUserLoggedInCallback onUserLoggedInCallback();

  Set<FeatureToggle<?>> featureToggles();

  SecurityKeeper securityKeeper();

  FirstLoginDateProvider firstLoginDateProvider();

  KLTBConnectionPool connectionPool();

  DspAwaker dspAwaker();

  CheckupCalculator checkupCalculator();

  CartRepository cartRepository();

  SessionFlags sessionFlags();

  CartDao cartDao();

  SynchronizationStateUseCase synchronizationStateUseCase();

  LifetimeSmilesDao lifetimeSmilesDao();

  FakeQuestionOfTheDayRepository questionOfTheDayRepository();

  AccountManager accountManager();

  LocationStatusListener locationStatusListener();

  CheckConnectionPrerequisitesUseCase checkConnectionPrerequisitesUseCase();

  FakeFeedbackRepository feedbackRepository();

  FakeAmazonDashApi amazonDashApi();

  PartnershipApiFake partnershipApiFake();

  PartnershipStatusRepository partnershipStatusRepository();

  interface BaseBuilder<T extends BaseBuilder<T>> {

    @BindsInstance
    T application(Application application);

    T appModule(EspressoAppModule appModule);

    T espressoRoomModule(EspressoOfflineBrushingsRepositoriesModule roomRepositoriesModule);

    T espressoSdkComponent(EspressoSdkComponent espressoSdkComponent);
  }
}
