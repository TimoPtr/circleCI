/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.appdata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.kolibree.sdkws.appdata.persistence.AppDataDao;
import com.kolibree.sdkws.core.InternalKolibreeConnector;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;
import timber.log.Timber;

/** {@link AppDataManager} implementation */
class AppDataManagerImpl implements AppDataManager {

  private final AppDataApi apiService;

  private final AppDataDao appDataDao;

  private final InternalKolibreeConnector kolibreeConnector;

  private final boolean isAppDataSyncEnabled;

  private AppDataConflictSolver conflictSolver;

  @Inject
  AppDataManagerImpl(
      @NonNull AppDataApi apiService,
      @NonNull AppDataDao appDataDao,
      @NonNull InternalKolibreeConnector kolibreeConnector,
      @AppDataSyncEnabled boolean isAppDataSyncEnabled) {
    this.apiService = apiService;
    this.appDataDao = appDataDao;
    this.kolibreeConnector = kolibreeConnector;
    this.isAppDataSyncEnabled = isAppDataSyncEnabled;
  }

  @NonNull
  @Override
  public Maybe<AppData> getAppData(long profileId) {
    return appDataDao.getLastAppData(profileId).map(appData -> appData);
  }

  @NonNull
  @Override
  public Completable saveAppData(@NonNull AppData appData) {
    final AppDataImpl appDataImpl = (AppDataImpl) appData;
    appDataDao.insert(appDataImpl);

    return postAppData(appDataImpl).doOnComplete(() -> flagAsSynchronized(appDataImpl));
  }

  @Override
  public synchronized void setAppDataConflictSolver(@NonNull AppDataConflictSolver conflictSolver) {
    this.conflictSolver = conflictSolver;
  }

  @NonNull
  @Override
  public Completable synchronize(long profileId) {
    if (!isAppDataSyncEnabled) {
      return Completable.complete();
    }

    return Single.zip(
            downloadAppData(profileId),
            appDataDao.getAppData(profileId, true).toSingle(AppDataImpl.NULL),
            appDataDao.getAppData(profileId, false).toSingle(AppDataImpl.NULL),
            this::mergeData)
        .ignoreElement();
  }

  @VisibleForTesting
  boolean haveConflict(
      @NonNull AppData serverData, @NonNull AppData lastSynchronized, @NonNull AppData lastSaved) {

    if (isNullAppData(lastSynchronized)) return false;

    return serverData.getDateTime().isAfter(lastSynchronized.getDateTime())
        && lastSaved.getDateTime().isAfter(lastSynchronized.getDateTime());
  }

  @NonNull
  private AppData mergeData(
      @NonNull AppData serverData, @NonNull AppData lastSynchronized, @NonNull AppData lastSaved)
      throws Exception {

    if (isNullAppData(serverData) && isNullAppData(lastSynchronized) && isNullAppData(lastSaved)) {
      return AppDataImpl.NULL;
    }

    AppDataImpl lastSavedImpl = (AppDataImpl) lastSaved;
    final AppDataImpl serverDataImpl = (AppDataImpl) serverData;

    if (haveConflict(serverData, lastSynchronized, lastSaved))
      lastSavedImpl = (AppDataImpl) onDataVersionConflict(serverData, lastSynchronized, lastSaved);

    if (serverData.getDateTime().isBefore(lastSaved.getDateTime())) {
      postAppData(lastSavedImpl);
      flagAsSynchronized(lastSavedImpl);
    } else if (serverData.getDateTime().isAfter(lastSaved.getDateTime())) {
      appDataDao.insert(serverDataImpl);
    }

    return AppDataImpl.NULL;
  }

  @VisibleForTesting
  @NonNull
  Single<AppData> downloadAppData(long profileId) {
    return Single.defer(() -> apiService.getAppData(kolibreeConnector.getAccountId(), profileId))
        .map(
            appData -> {
              appData.setProfileId(profileId);
              return appData;
            })
        .subscribeOn(Schedulers.io())
        .doOnError(Timber::e)
        .onErrorReturn((t) -> AppDataImpl.NULL)
        .map(appData -> appData); // Hack to deal with the cast to AppData
  }

  @NonNull
  private Completable postAppData(@NonNull AppDataImpl appData) {
    return apiService
        .saveAppData(kolibreeConnector.getAccountId(), appData.getProfileId(), appData)
        .ignoreElement()
        .doOnError(Throwable::printStackTrace)
        .subscribeOn(Schedulers.io());
  }

  private void flagAsSynchronized(@NonNull AppDataImpl appData) {
    appData.setSynchronized(true);
    appDataDao.insert(appData);
  }

  @NonNull
  private AppData onDataVersionConflict(
      @NonNull AppData serverData, @Nullable AppData lastSynchronized, @Nullable AppData lastSaved)
      throws Exception {
    if (conflictSolver == null) {
      throw new Exception(
          "No data version conflict solver has been set, please read the " + "documentation");
    }

    final AppDataImpl resolved =
        (AppDataImpl) conflictSolver.onConflict(serverData, lastSynchronized, lastSaved);

    appDataDao.insert(resolved); // Not synced at this point
    saveAppData(resolved);
    flagAsSynchronized(resolved);
    return resolved;
  }

  @VisibleForTesting
  boolean isNullAppData(@NonNull AppData appData) {
    return appData.getProfileId() == AppDataImpl.NULL.getProfileId();
  }
}
