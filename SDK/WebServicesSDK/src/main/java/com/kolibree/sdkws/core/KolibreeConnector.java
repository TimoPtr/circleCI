package com.kolibree.sdkws.core;

import static com.kolibree.android.extensions.LocaleExtensionsKt.getCountry;
import static com.kolibree.android.network.api.ApiErrorCode.ACCOUNT_ADDITIONAL_INFO_NEEDED;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.kolibree.android.accountinternal.CurrentProfileProvider;
import com.kolibree.android.accountinternal.account.ParentalConsent;
import com.kolibree.android.accountinternal.exception.NoAccountException;
import com.kolibree.android.accountinternal.internal.AccountInternal;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.accountinternal.profile.models.Profile;
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal;
import com.kolibree.android.network.api.ApiError;
import com.kolibree.sdkws.account.InternalAccountManager;
import com.kolibree.sdkws.api.request.ChangePasswordRequest;
import com.kolibree.sdkws.api.request.CheckAccountHasPasswordRequest;
import com.kolibree.sdkws.api.request.CheckUnderagedRequest;
import com.kolibree.sdkws.api.request.FacebookLoginRequest;
import com.kolibree.sdkws.api.request.GetAvroUploadUrlRequest;
import com.kolibree.sdkws.api.request.GetDefaultAvatarRequest;
import com.kolibree.sdkws.api.request.GetGoPirateDataRequest;
import com.kolibree.sdkws.api.request.GetInstructionsRequest;
import com.kolibree.sdkws.api.request.GetPractitionerRequest;
import com.kolibree.sdkws.api.request.GetVideoUploadUrlRequest;
import com.kolibree.sdkws.api.request.LoginRequest;
import com.kolibree.sdkws.api.request.MagicLinkLoginRequest;
import com.kolibree.sdkws.api.request.MagicLinkRequest;
import com.kolibree.sdkws.api.request.Request;
import com.kolibree.sdkws.api.request.ResetPasswordRequest;
import com.kolibree.sdkws.api.request.RevokePractitionerRequest;
import com.kolibree.sdkws.api.request.SaveAccountByFacebookRequest;
import com.kolibree.sdkws.api.request.UpdateGoPirateRequest;
import com.kolibree.sdkws.api.request.UpdateToothbrushRequest;
import com.kolibree.sdkws.api.request.ValidateCodeRequest;
import com.kolibree.sdkws.api.response.AvatarResponse;
import com.kolibree.sdkws.api.response.InstructionsResponse;
import com.kolibree.sdkws.api.response.UpdateToothbrushResponse;
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository;
import com.kolibree.sdkws.data.model.Brushing;
import com.kolibree.sdkws.data.model.ChangePasswordData;
import com.kolibree.sdkws.data.model.CreateBrushingData;
import com.kolibree.sdkws.data.model.CreateProfileData;
import com.kolibree.sdkws.data.model.EditProfileData;
import com.kolibree.sdkws.data.model.EmailData;
import com.kolibree.sdkws.data.model.FacebookLoginData;
import com.kolibree.sdkws.data.model.LoginData;
import com.kolibree.sdkws.data.model.MagicLinkData;
import com.kolibree.sdkws.data.model.PhoneNumberData;
import com.kolibree.sdkws.data.model.Practitioner;
import com.kolibree.sdkws.data.model.SaveAccountByFacebookData;
import com.kolibree.sdkws.data.model.UpdateToothbrushData;
import com.kolibree.sdkws.data.model.gopirate.GoPirateData;
import com.kolibree.sdkws.data.model.gopirate.GoPirateDatastore;
import com.kolibree.sdkws.data.model.gopirate.UpdateGoPirateData;
import com.kolibree.sdkws.data.request.BetaData;
import com.kolibree.sdkws.data.request.CreateAccountData;
import com.kolibree.sdkws.data.request.UpdateAccountV3Data;
import com.kolibree.sdkws.internal.OfflineUpdateDatastore;
import com.kolibree.sdkws.internal.OfflineUpdateInternal;
import com.kolibree.sdkws.networking.Response;
import com.kolibree.sdkws.profile.ProfileManager;
import com.kolibree.sdkws.profile.persistence.repo.ProfileRepository;
import com.kolibree.sdkws.room.MigrateAccountsFacade;
import com.kolibree.sdkws.sms.SmsToken;
import com.kolibree.sdkws.sms.data.AccountData;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import retrofit2.HttpException;
import timber.log.Timber;

/**
 * Created by aurelien on 15/09/15.
 *
 * <p>Kolibree server API connector
 *
 * <p>Should be used to connect to Kolibree API endpoints Please provide valid credentials in your
 * Manifest.xml such as :
 *
 * <p><meta-data android:name="com.kolibree.clientId" android:value="@string/kolibree_client_id" />
 * <meta-data android:name="com.kolibree.clientSecret"
 * android:value="@string/kolibree_client_secret" /> <meta-data android:name="com.kolibree.debug"
 * android:value="true" /> True means staging API target
 */
final class KolibreeConnector implements InternalKolibreeConnector {

  /** Class body */
  private final SynchronizationScheduler synchronizationScheduler;

  private final RefreshBroadcast refreshBroadcast = new RefreshBroadcast();

  private final InternalAccountManager accountManager;
  private final BrushingsRepository brushingRep;
  private final OfflineUpdateDatastore offlineUpdateDatastore;
  private final GoPirateDatastore goPirateDatastore;
  private final ProfileManager profileManager;
  private final ProfileRepository profileRepository;
  private final AccountDatastore accountDatastore;

  private final CurrentProfileProvider currentProfileProvider;
  private final Gson gson;
  private final BackendInteractor backendInteractor;
  private final InternalForceAppUpdater forceAppUpdater;

  @Nullable private final OnUserLoggedInCallback onUserLoggedInCallback;

  @Inject
  KolibreeConnector(
      BackendInteractor backendInteractor,
      SynchronizationScheduler synchronizationScheduler,
      InternalAccountManager accountManager,
      BrushingsRepository brushingRep,
      ProfileManager profileManager,
      ProfileRepository profileRepository,
      OfflineUpdateDatastore offlineUpdateDatastore,
      GoPirateDatastore goPirateDatastore,
      AccountDatastore accountDatastore,
      CurrentProfileProvider currentProfileProvider,
      MigrateAccountsFacade migrateAccountsFacade,
      Gson gson,
      InternalForceAppUpdater forceAppUpdater,
      @Nullable OnUserLoggedInCallback onUserLoggedInCallback) {
    this.synchronizationScheduler = synchronizationScheduler;
    this.accountManager = accountManager;
    this.brushingRep = brushingRep;
    this.offlineUpdateDatastore = offlineUpdateDatastore;
    this.goPirateDatastore = goPirateDatastore;
    this.profileManager = profileManager;
    this.profileRepository = profileRepository;
    this.currentProfileProvider = currentProfileProvider;
    this.backendInteractor = backendInteractor;

    this.accountDatastore = accountDatastore;
    this.gson = gson;
    this.forceAppUpdater = forceAppUpdater;
    this.onUserLoggedInCallback = onUserLoggedInCallback;

    migrateAccountsFacade.maybeMigrateAccounts();
  }

  @Override
  public void addBrushing(@NonNull CreateBrushingData data, @NonNull ProfileInternal profile) {
    AccountInternal accountInternal = currentAccount();
    if (accountInternal == null) {
      return;
    }

    Single.defer(() -> brushingRep.addBrushing(data, profile, accountInternal.getId()))
        .subscribeOn(Schedulers.io())
        .subscribe(
            (Unit) -> sendRefreshBroadcast(),
            t -> {
              Timber.e(t);
              sendRefreshBroadcast();
            });
  }

  @NotNull
  @Override
  public Single<Brushing> addBrushingSingle(
      @NonNull CreateBrushingData data, @NonNull ProfileInternal profile) {
    AccountInternal accountInternal = currentAccount();
    if (accountInternal == null) {
      return Single.error(new IllegalStateException("No account available"));
    }

    return brushingRep
        .addBrushing(data, profile, accountInternal.getId())
        .subscribeOn(Schedulers.io())
        .doOnSuccess(brushing -> sendRefreshBroadcast())
        .doOnError(throwable -> sendRefreshBroadcast());
  }

  @NotNull
  @SuppressLint("CheckResult")
  @Override
  @Deprecated
  public Brushing addBrushingSync(
      @NonNull CreateBrushingData data, @NonNull ProfileInternal profile) {
    return addBrushingSingle(data, profile).blockingGet();
  }

  @Override
  public boolean assignBrushings(
      @NonNull List<Brushing> brushings, @NonNull ProfileInternal profile) {
    return brushingRep.assignBrushings(brushings, profile).blockingGet();
  }

  @NonNull
  @Override
  public Single<List<Brushing>> getBrushingList(long profileId) {
    return brushingRep.getBrushings(profileId);
  }

  @NonNull
  @Override
  public Single<Boolean> synchronizeBrushing(long profileId) {
    return brushingRep.synchronizeBrushing(getAccountId(), profileId);
  }

  @Deprecated // Use UpdateToothbrushUseCase instead
  @Override
  public void updateToothbrush(
      final @NonNull UpdateToothbrushData data,
      final @NonNull KolibreeConnectorListener<UpdateToothbrushResponse> callback) {

    new Thread(
            () -> {
              final UpdateToothbrushRequest request =
                  new UpdateToothbrushRequest(data, getAccountId());
              final Response response = backendInteractor.call(request, getAccessToken());

              if (response.succeeded()) {
                try {
                  callback.notifySuccess(
                      new ObjectMapper()
                          .readValue(response.getBody(), UpdateToothbrushResponse.class));
                } catch (IOException e) {
                  callback.notifyError(new ApiError(e));
                }
              } else {
                callback.notifyError(response.getError());
              }
            })
        .start();
  }

  @NonNull
  @Override
  public Single<UpdateToothbrushResponse> updateToothbrush(@NonNull UpdateToothbrushData data) {
    return Single.fromCallable(
        () -> {
          final UpdateToothbrushRequest request = new UpdateToothbrushRequest(data, getAccountId());
          final Response response = backendInteractor.call(request, getAccessToken());

          if (response.succeeded()) {
            return new ObjectMapper().readValue(response.getBody(), UpdateToothbrushResponse.class);
          } else {
            throw new ApiError(response.getError());
          }
        });
  }

  @NonNull
  @Override
  public Single<Boolean> syncAndNotify() {
    return Single.create(
        emitter -> {
          AccountInternal account = currentAccount();

          if (account == null) { // First launch or data erased, force authentication
            Timber.d("No data, need registration or login");
            emitter.onSuccess(false);
          } else {
            setCurrentAccount(account);

            if (currentAccount() != null) {
              emitter.onSuccess(true);

              notifySuccessfulLogin();

              synchronizationScheduler.syncNow(); // Offline sync
            } else {
              emitter.onSuccess(false);
            }
          }
        });
  }

  @NonNull
  @Override
  public Single<AccountInternal> getRemoteAccount(long accountId) {
    Timber.d("Enforcer getting remote account");
    return accountManager.getAccount(accountId);
  }

  @NonNull
  public Completable createAnonymousAccount(@NonNull CreateAccountData data) {
    return saveAccountCompletable(accountManager.createAnonymousAccount(data));
  }

  @NonNull
  @Override
  public Single<SmsToken> sendSmsCodeTo(@NonNull String phoneNumber) {
    return accountManager
        .sendSmsCode(phoneNumber)
        .map((response) -> new SmsToken(phoneNumber, response.getVerificationToken()));
  }

  @NonNull
  @Override
  public Single<List<IProfile>> createAccount(
      @NonNull SmsToken token, @NonNull String code, @NonNull AccountData data) {
    CreateAccountData createAccountData =
        new CreateAccountData.Builder()
            .setBirthday(data.getBirthday())
            .setHandedness(data.getHandedness())
            .setGender(data.getGender())
            .setFirstName(data.getFirstName())
            .setCountry(data.getCountry())
            .setVerificationToken(token.getVerificationToken())
            .setVerificationCode(code)
            .setPhoneNumber(token.getPhoneNumber())
            .setParentalConsentGiven(true)
            .build();
    return saveAccountCompletable(accountManager.createAccountBySms(createAccountData))
        .toSingle(() -> toIProfiles(getProfileList()));
  }

  @NonNull
  @Override
  public Single<List<IProfile>> loginToAccount(@NonNull SmsToken token, @NonNull String code) {
    PhoneNumberData data =
        new PhoneNumberData(token.getPhoneNumber(), token.getVerificationToken(), code);
    return saveAccountCompletable(accountManager.loginBySms(data))
        .doOnComplete(this::notifySuccessfulLogin)
        .doOnComplete(synchronizationScheduler::syncNow)
        .toSingle(() -> toIProfiles(getProfileList()));
  }

  @VisibleForTesting
  void notifySuccessfulLogin() {
    if (onUserLoggedInCallback != null) {
      onUserLoggedInCallback.onUserLoggedIn();
    }
  }

  private List<IProfile> toIProfiles(List<Profile> currentProfiles) {
    return new ArrayList<>(currentProfiles);
  }

  @VisibleForTesting
  Completable saveAccountCompletable(Single<AccountInternal> singleAccount) {
    return singleAccount
        .map(
            accountInternal -> {
              saveAccount(accountInternal);
              return currentAccount();
            })
        .onErrorResumeNext(
            t -> {
              if (t instanceof HttpException) {
                HttpException e = (HttpException) t;
                if (e.response().errorBody() != null) {
                  //noinspection ConstantConditions
                  String errorBody = e.response().errorBody().string();
                  Response r = new Response(e.code(), errorBody);
                  forceAppUpdater.maybeNotifyForcedAppUpdate(r);

                  if (r.getError() != null) {
                    return Single.error(new Throwable(r.getError().getMessage()));
                  } else {
                    return Single.error(new Exception(r.getBody()));
                  }
                }
              }
              return Single.error(t);
            })
        .ignoreElement();
  }

  /*
  The accountId parameter is added so that in the future we can quickly replace invocations
  to KolibreeConnector with AccountManager.
   */
  @NonNull
  @Override
  public Completable updateAccount(long accountId, @NonNull UpdateAccountV3Data data) {
    return accountManager
        .updateAccount(accountId, data)
        .map(
            accountInternal -> {
              saveAccount(accountInternal);
              return currentAccount();
            })
        .toCompletable();
  }

  @NonNull
  @Override
  public Completable getMyData() {
    return accountManager.getMyData();
  }

  @NonNull
  @Override
  public Completable updateBetaAccount(long accountId, @NonNull BetaData data) {
    return saveAccountCompletable(accountManager.updateBetaAccount(accountId, data));
  }

  @Nullable
  @Override
  public AccountInternal currentAccount() {
    return accountDatastore.getAccountMaybe().subscribeOn(Schedulers.io()).blockingGet();
  }

  @NonNull
  @Override
  public Single<Profile> createProfile(@NonNull CreateProfileData data) {
    AccountInternal accountInternal = currentAccount();
    if (accountInternal == null) {
      return Single.error(NoAccountException.INSTANCE);
    }

    return profileManager
        .createProfile(data, accountInternal.getId())
        .map(
            profile -> {
              changeProfilePicture(profile, data.getPicturePath());
              accountInternal.addProfile(profile);
              accountInternal.setCurrentProfileId(profile.getId());
              accountDatastore.updateCurrentProfileId(accountInternal);
              sendRefreshBroadcast();
              return profile.exportProfile();
            });
  }

  @NonNull
  @Override
  public Single<Boolean> internalDeleteProfile(long profileId) {
    AccountInternal accountInternal = currentAccount();
    if (accountInternal == null) {
      return Single.error(NoAccountException.INSTANCE);
    }

    return profileManager
        .deleteProfile(getAccountId(), profileId)
        .doOnSuccess(
            success -> {
              if (success) {
                accountInternal.removeProfile(accountInternal.getProfileInternalWithId(profileId));

                checkIfUpdateCurrentProfileIsNeeded(profileId);
              }
            })
        .doOnError(
            throwable -> {
              // this should be moved to ProfileManager or ProfileRepository
              offlineUpdateDatastore.insertOrUpdate(new OfflineUpdateInternal(profileId));
            });
  }

  private boolean checkIfUpdateCurrentProfileIsNeeded(long profileId) {
    AccountInternal accountInternal = currentAccount();
    if (accountInternal == null) {
      return false;
    }

    // If the deleted profile is the current one, set the owner profile as current
    if (accountInternal.getCurrentProfileId() == null
        || accountInternal.getCurrentProfileId() == profileId) {
      accountInternal.setOwnerProfileAsCurrent();
      accountDatastore.updateCurrentProfileId(accountInternal);
    }
    return true;
  }

  /**
   * Updateprofile info
   *
   * @param data New profile info
   */
  @NonNull
  @Override
  public Single<Boolean> editProfile(
      final @NonNull EditProfileData data, @NonNull ProfileInternal profile) {
    return profileManager
        .editProfile(data, profile)
        .map(newProfile -> true)
        .doOnSuccess(ignore -> sendRefreshBroadcast());
  }

  // Change profile picture
  @Override
  public void changeProfilePicture(@NonNull ProfileInternal profile, String picturePath) {
    changeProfilePictureSingle(profile, picturePath)
        .subscribe(__ -> {}, Throwable::printStackTrace);
  }

  @NotNull
  @Override
  public Single<ProfileInternal> changeProfilePictureSingle(
      @NotNull ProfileInternal profile,
      @Nullable @org.jetbrains.annotations.Nullable String picturePath) {
    return profileManager
        .changeProfilePicture(profile, picturePath)
        .doOnSuccess(__ -> sendRefreshBroadcast());
  }

  /** AccountDataStore operations must happen in a worker thread */
  @WorkerThread
  @Override
  public void saveAccount(@NonNull AccountInternal account) {
    preserveTokens(account);
    setCurrentAccount(account);
  }

  @VisibleForTesting
  void preserveTokens(@NonNull AccountInternal account) {
    if (account.getAccessToken().isEmpty()) {
      account.setAccessToken(getAccessToken());
    }

    if (account.getRefreshToken().isEmpty()) {
      account.setRefreshToken(getRefreshToken());
    }
  }

  @NonNull
  @Override
  public Single<Boolean> needsParentalConsent(@NonNull LocalDate birthdate) {
    return Single.create(
        emitter -> {
          CheckUnderagedRequest request = new CheckUnderagedRequest(birthdate, getCountry());

          try {
            final Response response = backendInteractor.call(request, getAccessToken());

            /*
            204, null body -> no parental consent needed
            400 -> parental consent needed
            other error code -> something went wrong
             */

            if (response.getHttpCode() == 204 && response.getBody() == null) {
              emitter.onSuccess(false);
            } else {
              if (response.getHttpCode() == 400) {
                emitter.onSuccess(true);
              } else if (response.getError() != null) {
                throw response.getError();
              } else {
                throw new Exception("Unknown parental consent needed response");
              }
            }
          } catch (Exception e) {
            emitter.tryOnError(e);
          }
        });
  }

  /**
   * Get current account ID
   *
   * @return Current account ID
   */
  @Override
  public long getAccountId() {
    AccountInternal accountInternal = currentAccount();

    return accountInternal != null ? accountInternal.getId() : -1;
  }

  /**
   * Get Kolibree default avatar url list
   *
   * @param callback Callback
   */
  @Override
  public void getDefaultAvatarList(
      final @NonNull KolibreeConnectorListener<ArrayList<String>> callback) {
    new Thread(
            () -> {
              final Response response =
                  backendInteractor.call(new GetDefaultAvatarRequest(), getAccessToken());

              if (response.succeeded()) {
                try {
                  final AvatarResponse p = new AvatarResponse(response.getBody());
                  callback.notifySuccess(p.getList());
                } catch (JSONException e) {
                  callback.notifyError(ApiError.generateUnknownError(response.getHttpCode()));
                }
              } else {
                callback.notifyError(response.getError());
              }
            })
        .start();
  }

  /**
   * Fluent access to profile actions
   *
   * @param profileId Target profile ID
   * @return An access to profile actions
   */
  @Override
  @NonNull
  public ProfileWrapper withProfileId(long profileId) {
    return new ProfileWrapper(profileId, this, brushingRep, profileRepository, goPirateDatastore);
  }

  /**
   * Fluent access to currnt profile actions
   *
   * @return An access to current profile actions
   */
  @Override
  @Nullable
  public ProfileWrapper withCurrentProfile() {
    AccountInternal accountInternal = currentAccount();
    if (accountInternal == null || accountInternal.getCurrentProfileId() == null) {
      return null;
    }
    return new ProfileWrapper(
        accountInternal.getCurrentProfileId(),
        this,
        brushingRep,
        profileRepository,
        goPirateDatastore);
  }

  /**
   * Get the current account's current profile
   *
   * @return Current Profile
   */
  @Override
  @Nullable
  public Profile getCurrentProfile() {
    AccountInternal account = currentAccount();
    if (account == null) {
      return null;
    }

    sanitizeCurrentProfile();

    return getProfileWithId(account.getCurrentProfileId());
  }

  // https://kolibree.atlassian.net/browse/KLTB002-9706
  @VisibleForTesting
  void sanitizeCurrentProfile() {
    final AccountInternal account = currentAccount();

    if (account != null) {
      final Long currentProfileId = account.getCurrentProfileId();

      if (currentProfileId == null || !account.knows(currentProfileId)) {
        setActiveProfile(account.getOwnerProfileId());
        account.setOwnerProfileAsCurrent();
      }
    }
  }

  @NonNull
  public Flowable<Profile> currentProfileFlowable() {
    return currentProfileProvider.currentProfileFlowable();
  }

  @NonNull
  @Override
  public Completable setActiveProfileCompletable(long activeProfileId) {
    return currentAccountOptionalSingle()
        .flatMapCompletable(
            account -> {
              if (account.isPresent()) {
                return Completable.fromAction(
                        () -> {
                          account.get().setCurrentProfileId(activeProfileId);
                          accountDatastore.updateCurrentProfileId(account.get());
                        })
                    .subscribeOn(Schedulers.io())
                    .doOnComplete(
                        () -> {
                          sendRefreshBroadcast();
                          Timber.i("Profile with id %s is now active", activeProfileId);
                        });
              } else {
                return Completable.complete();
              }
            });
  }

  @VisibleForTesting
  @NonNull
  Single<Optional<AccountInternal>> currentAccountOptionalSingle() {
    return Single.fromCallable(() -> Optional.fromNullable(currentAccount()));
  }

  @Override
  public void setActiveProfile(long activeProfileId) {
    try {
      // Really ugly, but changing the setCurrent call to callable would mean too many changes
      setActiveProfileCompletable(activeProfileId).subscribeOn(Schedulers.io()).blockingAwait();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Get the current account's owner profile
   *
   * @return Owner profile
   */
  @Override
  @Nullable
  public Profile getOwnerProfile() {
    AccountInternal account = currentAccount();
    if (account == null) {
      return null;
    }

    try {
      return account.getOwnerProfile();
    } catch (IllegalStateException ignore) {
      return null;
    }
  }

  /**
   * Get current account profiles
   *
   * @return Profile list
   */
  @Override
  @NonNull
  public List<Profile> getProfileList() {
    try {
      return getProfileListSingle().blockingGet();
    } catch (NoSuchElementException e) {
      e.printStackTrace();

      Timber.e("Can't fetch profiles");
    }

    return new ArrayList<>();
  }

  @Override
  @NonNull
  public Single<List<Profile>> getProfileListSingle() {
    if (hasConnectedAccount()) {
      return profileManager.getProfilesLocally().subscribeOn(Schedulers.io());
    }

    return Single.error(new IllegalStateException("No account associated"));
  }

  /**
   * Get a profile with its ID
   *
   * @param profileId Target profile ID
   * @return Profile if found null otherwise
   */
  @Override
  @Nullable
  public Profile getProfileWithId(long profileId) {
    try {
      return getProfileWithIdSingle(profileId).blockingGet();
    } catch (NoSuchElementException ignored) {
      Timber.e("Profile with id %s does not exist ", profileId);
    } catch (Exception e) {
      Timber.e(e);
    }

    return null;
  }

  @NonNull
  @Override
  public Single<Profile> getProfileWithIdSingle(long profileId) {
    return profileManager.getProfileLocally(profileId).subscribeOn(Schedulers.io());
  }

  @Override
  public boolean doesCurrentAccountKnow(long profileId) {
    return currentAccount() != null && currentAccount().knows(profileId);
  }

  /**
   * Login with Kolibree credentials
   *
   * @param data Kolibree native credentials
   */
  @NonNull
  @Override
  public Completable login(@NonNull LoginData data) {
    return doLogin(new LoginRequest(data)).toCompletable();
  }

  /**
   * Login with facebook credentials
   *
   * @param data Facebook credentials
   */
  @NonNull
  @Override
  public Single<Boolean> login(@NonNull FacebookLoginData data) {
    return doLogin(new FacebookLoginRequest(data));
  }

  @NonNull
  @Override
  public Single<String> validateMagicLinkCode(@NonNull String code) {
    return Single.create(
        emitter -> {
          try {
            final Response response =
                backendInteractor.call(new ValidateCodeRequest(code), getAccessToken());

            if (response.succeeded()) {
              emitter.onSuccess(
                  new ObjectMapper().readValue(response.getBody(), MagicLinkData.class).getCode());
            } else {
              emitter.tryOnError(response.getError());
            }
          } catch (Exception e) {
            emitter.tryOnError(e);
          }
        });
  }

  @NonNull
  @Override
  public Completable requestMagicLink(@NonNull String email) {
    return Completable.create(
        emitter -> {
          try {
            final Response response = backendInteractor.call(new MagicLinkRequest(email), null);

            if (!response.succeeded()) {
              emitter.tryOnError(response.getError());
            } else {
              emitter.onComplete();
            }
          } catch (Exception e) {
            emitter.tryOnError(e);
          }
        });
  }

  @NonNull
  @Override
  public Single<Boolean> accountHasPassword(@NonNull String email) {
    return Single.create(
        emitter -> {
          try {
            final Response response =
                backendInteractor.call(new CheckAccountHasPasswordRequest(email), null);

            // This request is born to fail :( the backend will never respond with a OK 200 code
            final int errorCode = response.getError().getInternalErrorCode();
            if (errorCode == 25) { // Needs a real password
              emitter.onSuccess(true);
            } else if (errorCode == 326) { // Needs a magic link
              emitter.onSuccess(false);
            } else { // Failed for real
              ApiError error = response.getError();
              emitter.tryOnError(response.getError());
            }
          } catch (Exception e) { // Failed for real but harder
            emitter.tryOnError(e);
          }
        });
  }

  @NonNull
  @Override
  public Completable login(@NonNull String validatedCode) {
    return doLogin(new MagicLinkLoginRequest(validatedCode)).toCompletable();
  }

  /** Log current account out */
  @NonNull
  @Override
  public Completable logout() {
    Completable logoutCompletable;
    if (canAttemptToLogout()) {
      //noinspection ConstantConditions
      logoutCompletable =
          accountManager.logout(getAccountId(), getRefreshToken(), getAccessToken());
    } else {
      logoutCompletable = Completable.complete();
    }

    return logoutCompletable.doOnComplete(this::onLogoutCompleted);
  }

  @VisibleForTesting
  void onLogoutCompleted() {
    // TODO should clear all user content, not only account.
    // https://jira.kolibree.com/browse/KLTB002-5360
    clearCurrentAccount();

    currentProfileProvider.reset();

    if (onUserLoggedInCallback != null) {
      onUserLoggedInCallback.onUserLoggedOut();
    }
  }

  @VisibleForTesting
  boolean canAttemptToLogout() {
    return currentAccount() != null && getRefreshToken() != null && getAccessToken() != null;
  }

  @NonNull
  @Override
  public Completable deleteAccount() {
    AccountInternal internalAccount = currentAccount();
    if (internalAccount == null) {
      return Completable.complete();
    }

    return accountManager
        .deleteAccount(internalAccount.getId())
        .doOnComplete(this::deleteAccountSucceed);
  }

  private void deleteAccountSucceed() {
    clearCurrentAccount();
  }

  /**
   * Get current account email
   *
   * @return Account email
   */
  @Override
  @Nullable
  public String getEmail() {
    AccountInternal internalAccount = currentAccount();
    if (internalAccount == null) {
      return null;
    }

    return internalAccount.getEmail();
  }

  @Override
  public boolean getBeta() {
    AccountInternal internalAccount = currentAccount();
    if (internalAccount == null) {
      return false;
    }

    return internalAccount.isBeta();
  }

  /**
   * Check if an account has been connected
   *
   * @return true if a user is logged in, false otherwise
   */
  @Override
  public boolean hasConnectedAccount() {
    return currentAccount() != null;
  }

  /**
   * Get curent account pub ID
   *
   * @return non null pub ID
   */
  @Override
  @Nullable
  public String getPubId() {
    AccountInternal internalAccount = currentAccount();
    if (internalAccount == null) {
      return null;
    }

    return internalAccount.getPubId();
  }

  @Override
  @Nullable
  public ParentalConsent parentalConsentStatus() {
    AccountInternal internalAccount = currentAccount();
    if (internalAccount == null) {
      return ParentalConsent.UNKNOWN;
    }

    return internalAccount.getParentalConsent();
  }

  /**
   * Enable or disable data collecting for current account
   *
   * @param allow True to enable
   */
  @Override
  public Completable allowDataCollecting(boolean allow) {
    if (!hasConnectedAccount()) return Completable.error(NoAccountException.INSTANCE);

    return Completable.fromAction(
            () -> {
              AccountInternal internalAccount = currentAccount();
              if (internalAccount != null) {
                internalAccount.setAllowDataCollecting(allow);
                accountDatastore.setUpdateAllowDataCollecting(internalAccount);
              }
            })
        .subscribeOn(Schedulers.io());
  }

  /**
   * Get if data collection is allowed for current account
   *
   * @return True if allowed
   */
  @Override
  public boolean isDataCollectingAllowed() {
    AccountInternal internalAccount = currentAccount();
    if (internalAccount == null) {
      return false;
    }

    return internalAccount.isAllowDataCollecting();
  }

  /**
   * Get if Amazon drs is enabled for current account
   *
   * @return True if enabled
   */
  @Override
  public boolean isAmazonDrsEnabled() {
    AccountInternal internalAccount = currentAccount();
    if (internalAccount == null) {
      return false;
    }

    return internalAccount.isAmazonDrsEnabled();
  }

  /**
   * Send a reset password request
   *
   * @param email the email of the account to be reset
   */
  @Override
  public void resetPassword(
      @NonNull final String email, @NonNull final KolibreeConnectorListener<Boolean> l) {
    new Thread(
            () -> {
              final Response response =
                  backendInteractor.call(new ResetPasswordRequest(new EmailData(email)), null);
              if (response.succeeded()) {
                l.notifySuccess(Boolean.TRUE);
              } else {
                l.notifyError(response.getError());
              }
            })
        .start();
  }

  /**
   * Change current account password
   *
   * <p>Should only be called when currentAccount() is not null
   *
   * @param oldPassword the old password
   * @param newPassword the new one
   */
  @Override
  public void changePassword(
      @NonNull final String oldPassword,
      @NonNull final String newPassword,
      @NonNull final KolibreeConnectorListener<Boolean> l) {
    new Thread(
            () -> {
              final ChangePasswordRequest request =
                  new ChangePasswordRequest(
                      currentAccount().getId(), new ChangePasswordData(oldPassword, newPassword));
              final Response response = backendInteractor.call(request, getAccessToken());
              if (response.succeeded()) {
                l.notifySuccess(Boolean.TRUE);
              } else {
                l.notifyError(response.getError());
              }
            })
        .start();
  }

  @Override
  public void sendRefreshBroadcast() {
    AccountInternal internalAccount = currentAccount();
    if (internalAccount != null && internalAccount.getCurrentProfileId() != null) {
      refreshBroadcast.sendRefreshBroadcast(internalAccount.getCurrentProfileId());
    }
  }

  /**
   * Enable weekly digest subscription for current account
   *
   * <p>Should only be called when currentAccount() is not null
   *
   * @param enable True to enable
   */
  @NonNull
  @Override
  public Completable enableWeeklyDigest(boolean enable) {
    return accountManager
        .weeklyDigest(currentAccount().getId(), enable)
        .doOnComplete(() -> onWeeklyDigestUpdated(enable));
  }

  private void onWeeklyDigestUpdated(boolean enable) {

    AccountInternal account = currentAccount();
    if (account != null) {
      account.setDigestEnabled(enable);
      accountDatastore.updateAllowDigest(account);
    }
  }

  @Override
  public void getInstructions(
      @NonNull final KolibreeConnectorListener<InstructionsResponse> callback) {
    new Thread(
            () -> {
              final GetInstructionsRequest request = new GetInstructionsRequest(getAccountId());
              final Response response = backendInteractor.call(request, getAccessToken());

              if (response.succeeded()) {
                try {
                  callback.notifySuccess(
                      new ObjectMapper().readValue(response.getBody(), InstructionsResponse.class));
                } catch (IOException e) {
                  callback.notifyError(new ApiError(e));
                }
              } else {
                callback.notifyError(response.getError());
              }
            })
        .start();
  }

  /**
   * Get if weekly digest is enable for current account
   *
   * @return True if enabled
   */
  @Override
  public boolean isWeeklyDigestEnabled() {
    AccountInternal internalAccount = currentAccount();
    if (internalAccount == null) {
      return false;
    }

    return internalAccount.isDigestEnabled();
  }

  /**
   * Get an Amazon S3 URL for AVRO daa file upload purpose
   *
   * <p>Blocking call
   *
   * @return non null String url
   * @throws IOException if no URL could be get
   */
  @Override
  @NonNull
  public String getAvroFileUploadUrl() throws IOException {
    final Response response =
        backendInteractor.call(new GetAvroUploadUrlRequest(getAccountId()), getAccessToken());

    if (response.succeeded()) {
      try {
        return new JSONObject(response.getBody()).getString("url");
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    throw new IOException("Could not get Amazon S3 URL");
  }

  /**
   * Get an Amazon S3 URL for Video file upload purpose
   *
   * <p>Blocking call
   *
   * @return non null String url
   * @throws IOException if no URL could be get
   */
  @Override
  @NonNull
  public String getVideoUploadUrl(@NonNull String avroAmazonUrl) throws IOException {
    final Response response =
        backendInteractor.call(
            new GetVideoUploadUrlRequest(getAccountId(), avroAmazonUrl), getAccessToken());

    if (response.succeeded()) {
      try {
        return new JSONObject(response.getBody()).getString("url");
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    throw new IOException("Could not get Amazon S3 URL for video");
  }

  /**
   * *********************************************************************************************
   * Brushing methods ****************************************************************************
   * ********************************************************************************************
   */
  @NonNull
  @Override
  public Completable createAccountByEmail(@NonNull String email) {
    AccountInternal account = currentAccount();

    if (account == null) {
      return Completable.error(NoAccountException.INSTANCE);
    }

    return accountManager
        .createAccount(email, account.getAppId())
        .doOnComplete(() -> updateEmail(account, email));
  }

  @NonNull
  @Override
  public Single<Boolean> createAccountByFacebook(
      @NonNull String email, @Nullable String facebookId, @Nullable String facebookAuthToken) {
    return Single.create(
        emitter -> {
          try {
            AccountInternal account = currentAccount();
            if (account == null) {
              emitter.tryOnError(NoAccountException.INSTANCE);
              return;
            }

            String appId = account.getAppId();

            SaveAccountByFacebookData data =
                new SaveAccountByFacebookData(facebookId, facebookAuthToken, email, appId);
            SaveAccountByFacebookRequest request = new SaveAccountByFacebookRequest(data);

            final Response response = backendInteractor.call(request, account.getAccessToken());
            if (response.succeeded()) {
              updateEmail(account, email);
              updateFacebookId(account, facebookId);
              emitter.onSuccess(true);

              synchronizationScheduler.syncNow();
            } else {
              emitter.tryOnError(response.getError());
            }
          } catch (Exception e) {
            emitter.tryOnError(e);
          }
        });
  }

  private void updateEmail(AccountInternal account, String email) {
    account.setEmail(email);
    accountDatastore.updateEmail(account);
  }

  private void updateFacebookId(AccountInternal account, String facebookId) {
    account.setFacebookId(facebookId);
    accountDatastore.updateFacebookId(account);
  }

  @NonNull
  @Override
  public Completable loginByGoogle(@NonNull CreateAccountData data) {
    return accountManager
        .loginByGoogle(data)
        .map(
            account -> {
              saveAccount(account);
              return currentAccount();
            })
        .ignoreElement()
        .doOnComplete(synchronizationScheduler::syncNow);
  }

  @NonNull
  @Override
  public Completable createAccountByGoogle(@NonNull CreateAccountData data) {
    return accountManager
        .registerWithGoogle(data)
        .doOnSuccess(this::saveAccount)
        .flatMap(
            account -> {
              if (data.getGoogleAvatarUrl() == null || data.getGoogleAvatarUrl().isEmpty()) {
                return Single.fromCallable(this::currentAccount);
              } else {
                return maybeUploadGoogleAvatar(account, data.getGoogleAvatarUrl());
              }
            })
        .ignoreElement()
        .doOnComplete(synchronizationScheduler::syncNow);
  }

  @NonNull
  public Single<AccountInternal> createEmailAccount(@NonNull CreateAccountData data) {
    return accountManager
        .createEmailAccount(data)
        .map(
            account -> {
              saveAccount(account);
              return currentAccount();
            })
        .doOnSuccess(ignore -> synchronizationScheduler.syncNow());
  }

  /**
   * Get current account access token
   *
   * @return Current account access token
   */
  @VisibleForTesting
  @Nullable
  String getAccessToken() {
    AccountInternal internalAccount = currentAccount();

    return internalAccount == null ? null : internalAccount.getAccessToken();
  }

  /**
   * Get current account access token
   *
   * @return Current account access token
   */
  @VisibleForTesting
  @Nullable
  String getRefreshToken() {
    AccountInternal internalAccount = currentAccount();

    return internalAccount == null ? null : internalAccount.getRefreshToken();
  }

  // Login process
  @VisibleForTesting
  @NonNull
  Single<Boolean> doLogin(final Request request) {
    return Single.create(
        emitter -> {
          try {
            final Response response = backendInteractor.call(request, getAccessToken());

            if (response.succeeded()) {
              final AccountInternal loginAccount =
                  gson.fromJson(response.getBody(), AccountInternal.class);

              Timber.d("saving account %s", loginAccount);
              saveAccount(loginAccount);

              notifySuccessfulLogin();

              emitter.onSuccess(true);

              synchronizationScheduler.syncNow();
            } else if (response.getError().getInternalErrorCode()
                == ACCOUNT_ADDITIONAL_INFO_NEEDED) {
              emitter.onSuccess(false); // Facebook
            } else {
              emitter.tryOnError(response.getError());
            }
          } catch (Exception e) {
            e.printStackTrace();
            emitter.tryOnError(e);
          }
        });
  }

  @VisibleForTesting
  void setCurrentAccount(AccountInternal account) {
    accountDatastore.setAccount(account);
  }

  @VisibleForTesting
  void clearCurrentAccount() {
    accountDatastore.truncate();
  }

  @VisibleForTesting
  Single<AccountInternal> maybeUploadGoogleAvatar(AccountInternal account, String googleAvatarUrl) {
    return Single.fromCallable(() -> account.getProfileInternalWithId(account.getOwnerProfileId()))
        .flatMap(
            ownerProfile -> profileManager.downloadExternalPicture(ownerProfile, googleAvatarUrl))
        .map(profile -> currentAccount())
        .onErrorReturn(e -> currentAccount());
  }

  @Override
  public void synchronizeGoPirate(long profileId) {
    AccountInternal internalAccount = currentAccount();

    if (internalAccount == null) {
      return;
    }

    final OfflineUpdateInternal updateInternal =
        offlineUpdateDatastore.getOfflineUpdateForProfileId(
            profileId, OfflineUpdateInternal.TYPE_GO_PIRATE);

    if (updateInternal != null) {
      Timber.d("Found Go Pirate data to be synchronized for profile id %s", profileId);

      final UpdateGoPirateData data = updateInternal.getGoPirateUpdateData();
      final UpdateGoPirateRequest request =
          new UpdateGoPirateRequest(internalAccount.getId(), profileId, data);
      final Response response = backendInteractor.call(request, internalAccount.getAccessToken());

      if (!response.succeeded()) {
        offlineUpdateDatastore.insertOrUpdate(updateInternal);
      }
    }

    final GetGoPirateDataRequest getRequest =
        new GetGoPirateDataRequest(internalAccount.getId(), profileId);
    final Response getResponse =
        backendInteractor.call(getRequest, internalAccount.getAccessToken());

    if (getResponse.succeeded()) {
      try {
        goPirateDatastore
            .update(GoPirateData.fromJson(profileId, getResponse.getBody()), profileId)
            .subscribeOn(Schedulers.io())
            .blockingAwait();
        Timber.d("Go Pirate data has been synchronized for profile id %s", profileId);
      } catch (JSONException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @NonNull
  @Override
  public Observable<Long> getRefreshObservable() {
    return refreshBroadcast.getRefreshObservable();
  }

  @NonNull
  @Override
  public Observable<Boolean> isAppUpdateNeeded() {
    return forceAppUpdater.isAppUpdateNeeded();
  }

  /**
   * Update Go Pirate data
   *
   * @param data New Go Pirate data
   */
  @Override
  public void updateGoPirateData(final @NonNull UpdateGoPirateData data, ProfileInternal profile) {
    try {
      // Synchronize
      new Thread(
              () -> {
                goPirateDatastore.update(data, profile.getId()).blockingAwait();

                final UpdateGoPirateRequest request =
                    new UpdateGoPirateRequest(currentAccount().getId(), profile.getId(), data);
                final Response response =
                    backendInteractor.call(request, currentAccount().getAccessToken());

                if (!response.succeeded()) {
                  offlineUpdateDatastore.insertOrUpdate(
                      new OfflineUpdateInternal(data, profile.getId()));
                } else {
                  Timber.d("Successfully updated Go Pirate data for profile id " + profile.getId());
                  offlineUpdateDatastore.delete(
                      profile.getId(), OfflineUpdateInternal.TYPE_GO_PIRATE);

                  try {
                    GoPirateData newPirateData =
                        GoPirateData.fromJson(profile.getId(), response.getBody());

                    goPirateDatastore.update(newPirateData, profile.getId()).blockingAwait();
                    Timber.d(
                        "Unable to update Go Pirate data for profile id "
                            + profile.getId()
                            + ", adding local update");
                  } catch (JSONException e) {
                    e.printStackTrace();
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                }
              })
          .start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Get associated practitioners
   *
   * @param l Callback
   */
  public void getPractitioners(
      final @NonNull KolibreeConnectorListener<Practitioner[]> l,
      @NonNull ProfileInternal profile) {
    new Thread(
            () -> {
              AccountInternal accountInternal = currentAccount();

              if (accountInternal == null) {
                return;
              }

              final GetPractitionerRequest request =
                  new GetPractitionerRequest(accountInternal.getId(), profile.getId());
              final Response response = backendInteractor.call(request, getAccessToken());

              if (response.succeeded()) {
                try {
                  l.notifySuccess(
                      new ObjectMapper().readValue(response.getBody(), Practitioner[].class));
                } catch (IOException e) {
                  l.notifyError(ApiError.generateUnknownError(response.getHttpCode()));
                }
              } else {
                l.notifyError(ApiError.generateUnknownError(response.getHttpCode()));
              }
            })
        .start();
  }

  @Override
  public void revokePractitioner(
      final @NonNull String token, @NonNull final KolibreeConnectorListener<Boolean> listener) {
    new Thread(
            () -> {
              final Response response =
                  backendInteractor.call(new RevokePractitionerRequest(token), getAccessToken());

              if (response.succeeded()) {
                listener.notifySuccess(Boolean.TRUE);
              } else {
                listener.notifyError(ApiError.generateUnknownError(response.getHttpCode()));
              }
            })
        .start();
  }
}
