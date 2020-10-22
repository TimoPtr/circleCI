/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils;

import static com.kolibree.android.accountinternal.account.ParentalConsent.GRANTED;
import static com.kolibree.android.accountinternal.account.ParentalConsent.UNKNOWN;
import static com.kolibree.android.app.coppa.AccountPermissionsImplKt.KEY_PARENTAL_CONSENT_NEEDED;
import static com.kolibree.android.app.dagger.EspressoShopDataModule.DEFAULT_QUANTITY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.kolibree.android.accountinternal.account.ParentalConsent;
import com.kolibree.android.accountinternal.profile.models.Profile;
import com.kolibree.android.app.App;
import com.kolibree.android.app.dagger.EspressoAppComponent;
import com.kolibree.android.app.dagger.EspressoShopDataModule;
import com.kolibree.android.app.ui.settings.secret.persistence.AppSessionFlags;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.feature.Feature;
import com.kolibree.android.feature.FeatureToggle;
import com.kolibree.android.feature.GooglePayFeature;
import com.kolibree.android.feature.PulsingDotFeature;
import com.kolibree.android.feature.ShowPlaqlessVersionOfViewsFeature;
import com.kolibree.android.feature.impl.PersistentFeatureToggle;
import com.kolibree.android.feature.impl.TransientFeatureToggle;
import com.kolibree.android.location.EnableLocation;
import com.kolibree.android.location.LocationAction;
import com.kolibree.android.location.NoAction;
import com.kolibree.android.location.RequestPermission;
import com.kolibree.android.persistence.BasePreferences;
import com.kolibree.android.rewards.models.LifetimeSmilesEntity;
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge;
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.persistence.model.AccountToothbrush;
import com.kolibree.android.shop.data.ShopifyClientWrapper;
import com.kolibree.android.shop.data.ShopifyFeaturedProductsUseCase;
import com.kolibree.android.shop.data.persitence.model.CartEntryEntity;
import com.kolibree.android.shop.domain.model.Product;
import com.kolibree.android.synchronization.SynchronizationState;
import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore;
import com.kolibree.android.test.extensions.TrustedClockExtensionsKt;
import com.kolibree.android.test.mocks.AccountToothbrushBuilder;
import com.kolibree.android.test.mocks.ProfileBuilder;
import com.kolibree.android.test.mocks.rewards.ProfileRewardsBuilder;
import com.kolibree.android.test.utils.rewards.ChallengesMocker;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.threeten.bp.temporal.ChronoUnit;
import timber.log.Timber;

/** Created by miguelaragues on 5/2/18. */
@SuppressWarnings("KotlinInternalInJava")
public class AppMocker {

  /** See GooglePayAvailabilityWatcher.kt */
  private static final String IS_GPAY_AVAILABLE_KEY = "is_gpay_available";

  private static final String HAS_SHOWN_TOOLBOX_EXPLANATION = "has_shown_explanation";

  private Observable<Boolean> networkConnectivityObservable;
  private Observable<SynchronizationState> synchronizationStateObservable;
  private boolean isNetworkEnabled = true;
  private SdkBuilder sdkBuilder;
  private Integer nbOfOrphanBrushings;
  private KolibreeProMocker kolibreeProMocker;
  private boolean prepareForMainScreen;
  private boolean locationPermissionGranted = true;
  private boolean locationEnabled = true;
  private boolean requestLocationForSession = false;
  private List<EspressoProduct> shopifyProducts = new ArrayList<>();
  private Long idOfProfileWithSmiles = null;
  private String nameOfProfileWithSmiles = null;
  private Integer smilesForProfile = null;
  private UnityGameMocker unityGameMocker = null;
  private Map<Feature, Object> featureToggles = new HashMap<>();
  private ParentalConsent parentalConsent;
  private boolean isGooglePayAvailable = true;
  private boolean hasToolboxExplanationBeenShown = true;
  private boolean isNewsletterSubscriptionEnabled = false;
  private List<EarnPointsChallenge> earnPointsChallenges = null;

  private final Map<Long, Integer> profileLifetimeSmiles = new HashMap<>();

  private AppMocker() {
    // no-op
  }

  public static AppMocker create() {
    return new AppMocker().withShowCalendarTab();
  }

  public AppMocker withUnityGameMocker(UnityGameMocker unityGameMocker) {
    this.unityGameMocker = unityGameMocker;

    return this;
  }

  public AppMocker withOngoingSyncObservable(
      Observable<SynchronizationState> ongoingSyncObservable) {
    this.synchronizationStateObservable = ongoingSyncObservable;

    return this;
  }

  public AppMocker withNetworkConnectivity(Observable<Boolean> networkConnectivityObservable) {
    this.networkConnectivityObservable = networkConnectivityObservable;

    return this;
  }

  public AppMocker withNetworkConnectivity(boolean hasNetworkConnectivity) {
    this.isNetworkEnabled = hasNetworkConnectivity;

    return this;
  }

  public AppMocker withSdkBuilder(SdkBuilder sdkBuilder) {
    this.sdkBuilder = sdkBuilder;

    return this;
  }

  public AppMocker withLocationPermissionGranted(boolean locationPermissionGranted) {
    this.locationPermissionGranted = locationPermissionGranted;

    return this;
  }

  public AppMocker withLocationEnabled(boolean locationEnabled) {
    this.locationEnabled = locationEnabled;

    return this;
  }

  public AppMocker withRequestLocationForSession(boolean requestLocationForSession) {
    this.requestLocationForSession = requestLocationForSession;

    return this;
  }

  public AppMocker withNbOfOrphanBrushings(int nbOfOrphanBrushings) {
    this.nbOfOrphanBrushings = nbOfOrphanBrushings;

    return this;
  }

  public AppMocker withParentalConsent(ParentalConsent parentalConsent) {
    // this is very fragile and implementation dependant, that's why I centralize it here
    this.parentalConsent = parentalConsent;

    return this;
  }

  public AppMocker withKolibreeProMocker(KolibreeProMocker kolibreeProMocker) {
    this.kolibreeProMocker = kolibreeProMocker;
    return this;
  }

  public AppMocker withShowPlaqlessViewVersions(Boolean showPlaqlessViewVersions) {
    return withFeature(ShowPlaqlessVersionOfViewsFeature.INSTANCE, showPlaqlessViewVersions);
  }

  public AppMocker withPulsingDotsAlwaysVisible(Boolean pulsingDotsAlwaysVisible) {
    return withFeature(PulsingDotFeature.INSTANCE, pulsingDotsAlwaysVisible);
  }

  public <T> AppMocker withFeature(Feature<T> feature, T value) {
    featureToggles.put(feature, value);
    return this;
  }

  public AppMocker withProfileSmiles(long profileId, String profileName, int smiles) {
    idOfProfileWithSmiles = profileId;
    nameOfProfileWithSmiles = profileName;
    smilesForProfile = smiles;
    return this;
  }

  public AppMocker withLifetimeSmiles(long profileId, int smiles) {
    profileLifetimeSmiles.put(profileId, smiles);

    return this;
  }

  public AppMocker withMockedShopifyProducts() {
    this.shopifyProducts = EspressoShopDataModule.INSTANCE.getDefaultProductList();
    return this;
  }

  /** @deprecated Use withMockedShopifyData */
  @Deprecated
  public AppMocker withMockedShopifyProducts(List<Product> products) {
    ArrayList<EspressoProduct> espressoProducts = new ArrayList<>();
    for (Product product : products) {
      espressoProducts.add(new EspressoProduct(product, DEFAULT_QUANTITY));
    }

    return withMockedShopifyData(espressoProducts);
  }

  public AppMocker withMockedShopifyData(List<EspressoProduct> products) {
    this.shopifyProducts = products;
    return this;
  }

  /**
   * This method mocks the value returned by GooglePayAvailabilityWatcher, not the real availability
   * of Google Pay
   */
  public AppMocker withGooglePayAvailable(boolean isGooglePayAvailable) {
    this.isGooglePayAvailable = isGooglePayAvailable;
    withFeature(GooglePayFeature.INSTANCE, isGooglePayAvailable);
    return this;
  }

  public AppMocker withToolboxExplanationShown(boolean hasToolboxExplanationBeenShown) {
    this.hasToolboxExplanationBeenShown = hasToolboxExplanationBeenShown;
    return this;
  }

  public AppMocker withNewsletterSubscription(boolean isNewsletterSubscriptionEnabled) {
    this.isNewsletterSubscriptionEnabled = isNewsletterSubscriptionEnabled;
    return this;
  }

  public AppMocker withEarnPointsChallenges(List<EarnPointsChallenge> challenges) {
    this.earnPointsChallenges = challenges;
    return this;
  }

  public AppMocker prepareForMainScreen() {
    this.prepareForMainScreen = true;

    return this;
  }

  /** Given all the parameters, mock the App */
  public AppMocker mock() {
    setupFeatureToggles();

    setupNetworkConnectivity();

    setupProfileWithSmiles();

    setupSdk();

    setupOrphanBrushings();

    setupAccountToothbrushes();

    setupParentalEmailNeeded();

    setupToolboxExplanation();

    setupNewsletterSubscription();

    setupKolibreePro();

    setupLocation();

    if (unityGameMocker != null) {
      setupUnityGame();
    } else {
      setupEmptyUnityGame();
    }

    setupShopify();

    setupGooglePayAvailability();

    setupOngoingSync();

    setupHomeScreen();

    return this;
  }

  private void setupEmptyUnityGame() {
    UnityGameMocker.Companion.emptyUnityGame();
  }

  private void setupUnityGame() {
    unityGameMocker.build();
  }

  private void setupLocation() {
    when(component().locationActionChecker().enableLocationActionSingle())
        .thenReturn(
            Single.defer(() -> Single.create(emitter -> emitter.onSuccess(getLocationAction()))));

    when(component().locationStatus().isReadyToScan())
        .thenReturn(locationEnabled && locationPermissionGranted);
    when(component().locationStatus().getLocationAction()).thenReturn(getLocationAction());
    when(component().locationStatus().shouldAskPermission()).thenReturn(!locationPermissionGranted);
    when(component().locationStatus().shouldEnableLocation()).thenReturn(!locationEnabled);

    when(component().locationStatusListener().locationActionStream())
        .thenReturn(Observable.just(getLocationAction()));

    when(component().checkConnectionPrerequisitesUseCase().checkOnceAndStream())
        .thenReturn(Observable.just(getConnectionPrerequisitesState()));
    when(component().checkConnectionPrerequisitesUseCase().checkConnectionPrerequisites())
        .thenReturn(getConnectionPrerequisitesState());

    AppSessionFlags sessionFlags = new AppSessionFlags(component().context());
    sessionFlags.setShouldRequestEnableLocation(requestLocationForSession);
  }

  private ConnectionPrerequisitesState getConnectionPrerequisitesState() {
    if (!locationPermissionGranted)
      return ConnectionPrerequisitesState.LocationPermissionNotGranted;
    else if (!locationEnabled) return ConnectionPrerequisitesState.LocationServiceDisabled;

    return ConnectionPrerequisitesState.ConnectionAllowed;
  }

  private LocationAction getLocationAction() {
    if (!locationPermissionGranted) return RequestPermission.INSTANCE;
    else if (!locationEnabled) return EnableLocation.INSTANCE;

    return NoAction.INSTANCE;
  }

  private void setupFeatureToggles() {
    for (FeatureToggle toggle : component().featureToggles()) {
      Feature feature = toggle.getFeature();
      Object value = featureToggles.get(feature);
      if (toggle instanceof PersistentFeatureToggle || toggle instanceof TransientFeatureToggle) {
        if (value != null) {
          Timber.i(
              "Setting toggle for feature `%s` to value `%s`", feature.getDisplayName(), value);
          toggle.setValue(value);
        } else {
          Timber.i(
              "Setting toggle for feature `%s` to initial value `%s`",
              feature.getDisplayName(), feature.getInitialValue());
          toggle.setValue(feature.getInitialValue());
        }
      } else if (value != null) {
        throw new IllegalStateException(
            String.format(
                "Toggle of type %s for feature `%s` cannot change value to `%s`",
                toggle.getClass().getCanonicalName(), feature.getDisplayName(), value));
      }
    }
  }

  private void setupKolibreePro() {
    if (kolibreeProMocker == null) kolibreeProMocker = KolibreeProMocker.create();

    kolibreeProMocker.mock();
  }

  private void setupParentalEmailNeeded() {
    ParentalConsent localParentalConsent = inferParentalConsent();

    if (localParentalConsent == UNKNOWN) {
      App.appComponent
          .context()
          .getSharedPreferences(BasePreferences.PREFS_FILENAME, Context.MODE_PRIVATE)
          .edit()
          .remove(KEY_PARENTAL_CONSENT_NEEDED)
          .apply();
    } else {
      boolean parentalEmailNeeded = localParentalConsent != GRANTED;

      App.appComponent
          .context()
          .getSharedPreferences(BasePreferences.PREFS_FILENAME, Context.MODE_PRIVATE)
          .edit()
          .putBoolean(KEY_PARENTAL_CONSENT_NEEDED, parentalEmailNeeded)
          .apply();
    }
  }

  private void setupToolboxExplanation() {
    App.appComponent
        .context()
        .getSharedPreferences(BasePreferences.PREFS_FILENAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(HAS_SHOWN_TOOLBOX_EXPLANATION, hasToolboxExplanationBeenShown)
        .apply();
  }

  private void setupNewsletterSubscription() {
    when(component().accountManager().isEmailNewsletterSubscriptionEnabled())
        .thenReturn(Single.just(isNewsletterSubscriptionEnabled));
  }

  @NotNull
  private ParentalConsent inferParentalConsent() {
    return parentalConsent == null ? UNKNOWN : parentalConsent;
  }

  private void setupAccountToothbrushes() {
    List<KLTBConnection> connections = component().kolibreeService().getKnownConnections();
    long accountId = component().kolibreeConnector().getAccountId();

    List<AccountToothbrush> accountToothbrushes = new ArrayList<>();
    for (KLTBConnection connection : connections) {
      accountToothbrushes.add(AccountToothbrushBuilder.fromConnection(connection));

      long ownerId = connection.userMode().profileOrSharedModeId().blockingGet();
      component()
          .toothbrushRepository()
          .associate(connection.toothbrush(), ownerId, accountId)
          .blockingAwait();
    }
  }

  private void setupOrphanBrushings() {
    if (nbOfOrphanBrushings != null) {
      when(component().orphanBrushingRepository().count())
          .thenReturn(
              Flowable.create(e -> e.onNext(nbOfOrphanBrushings), BackpressureStrategy.BUFFER));
    }
  }

  private void setupNetworkConnectivity() {
    if (networkConnectivityObservable != null) {
      when(component().networkChecker().connectivityStateObservable())
          .thenReturn(networkConnectivityObservable);

      networkConnectivityObservable.subscribe(
          hasConnectivity ->
              when(component().networkChecker().hasConnectivity()).thenReturn(hasConnectivity));
    } else {
      when(component().networkChecker().connectivityStateObservable())
          .thenReturn(Observable.just(isNetworkEnabled));

      when(component().networkChecker().hasConnectivity()).thenReturn(isNetworkEnabled);
    }
  }

  private void setupProfileWithSmiles() {
    if (idOfProfileWithSmiles != null
        && nameOfProfileWithSmiles != null
        && smilesForProfile != null) {

      SynchronizableReadOnlyDataStore profileSmilesDatastore = component().profileSmilesDatastore();
      profileSmilesDatastore.replace(
          ProfileRewardsBuilder.createProfileSmiles(idOfProfileWithSmiles, smilesForProfile));

      if (sdkBuilder == null) {
        Profile profile =
            ProfileBuilder.create()
                .withId(idOfProfileWithSmiles)
                .withName(nameOfProfileWithSmiles)
                .build();

        sdkBuilder = SdkBuilder.create().withActiveProfile(profile).withProfiles(profile);
      }
    }

    profileLifetimeSmiles.forEach(
        (profileId, lifetimeSmiles) ->
            component()
                .lifetimeSmilesDao()
                .insertOrReplace(new LifetimeSmilesEntity(profileId, lifetimeSmiles)));
  }

  private void setupSdk() {
    if (sdkBuilder == null) {
      sdkBuilder = SdkBuilder.create();
    }

    if (prepareForMainScreen) {
      sdkBuilder = sdkBuilder.prepareForMainScreen();
    }

    if (parentalConsent != null) {
      sdkBuilder = sdkBuilder.withParentalConsent(inferParentalConsent());
    }

    sdkBuilder.build();
  }

  private void setupShopify() {
    ShopifyClientWrapper mockedWrapper =
        EspressoShopDataModule.INSTANCE.getShopifyClientWrapperMock();

    when(mockedWrapper.getStoreDetails())
        .thenReturn(Single.just(EspressoShopDataModule.INSTANCE.getDefaultShopDetails()));

    List<Product> products = new ArrayList<>();
    for (EspressoProduct espressoProduct : shopifyProducts) {
      products.add(espressoProduct.getProduct());

      component()
          .cartDao()
          .insertEntry(
              new CartEntryEntity(
                  component().currentProfileProvider().currentProfile().getId(),
                  espressoProduct.getProduct().getProductId(),
                  espressoProduct.getProduct().getVariantId(),
                  espressoProduct.getQuantity()));
    }
    when(mockedWrapper.getProducts()).thenReturn(Single.just(products));

    ShopifyFeaturedProductsUseCase mockedUseCase =
        EspressoShopDataModule.INSTANCE.getShopifyFeaturedProductsUseCase();

    when(mockedUseCase.getFeaturedProducts()).thenReturn(Flowable.just(products));
  }

  private void setupGooglePayAvailability() {
    component().sessionFlags().setSessionFlag(IS_GPAY_AVAILABLE_KEY, isGooglePayAvailable);
  }

  private void setupOngoingSync() {
    if (synchronizationStateObservable == null) {
      /*
      By default, we mock that there was a successful sync after user logged in

      This is needed by UserExpectsSmilesUseCase. It compares the time at which the user wants
      smiles with the last sync success.
       */
      TrustedClockExtensionsKt.withFixedInstant(
          TrustedClock.getNowInstant().plus(1, ChronoUnit.MINUTES),
          instant -> {
            synchronizationStateObservable = Observable.just(new SynchronizationState.Success());

            TrustedClockExtensionsKt.reset(TrustedClock.INSTANCE);

            return Unit.INSTANCE;
          });
    }

    when(component().synchronizationStateUseCase().getOnceAndStream())
        .thenReturn(synchronizationStateObservable);
  }

  private void setupHomeScreen() {
    ChallengesMocker mocker = new ChallengesMocker(component());
    mocker.truncateRewardsDatabase();

    // More ways to earn points
    if (earnPointsChallenges != null) {
      mocker.populateChallengeDatabase(idOfProfileWithSmiles, earnPointsChallenges);
    }
  }

  private EspressoAppComponent component() {
    return (EspressoAppComponent) App.appComponent;
  }

  private AppMocker withShowCalendarTab() {
    when(component().calendarBrushingsUseCase().getBrushingDateRange(any()))
        .thenReturn(Single.never());
    when(component().calendarBrushingsUseCase().getBrushingState(any()))
        .thenReturn(Flowable.never());
    when(component().calendarBrushingsUseCase().maybeFetchBrushingsBeforeMonth(any(), any()))
        .thenReturn(Completable.complete());
    return prepareForMainScreen();
  }
}
