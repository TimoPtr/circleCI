/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.data.database;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore;
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore;
import com.kolibree.android.commons.interfaces.Truncable;
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository;
import com.kolibree.sdkws.data.model.gopirate.GoPirateDatastore;
import com.kolibree.sdkws.internal.OfflineUpdateDatastore;
import io.reactivex.Completable;
import javax.inject.Inject;

/*
Check DaggerTruncableSetTest on rename
 */
class ApiSDKDatabaseInteractorImpl implements Truncable {

  private final BrushingsRepository brushingsRepository;
  private final AccountDatastore accountDatastore;
  private final ProfileDatastore profileDatastore;
  private final OfflineUpdateDatastore offlineUpdateDatastore;
  private final GoPirateDatastore goPirateDatastore;

  @Inject
  ApiSDKDatabaseInteractorImpl(
      BrushingsRepository brushingsRepository,
      AccountDatastore accountDatastore,
      ProfileDatastore profileDatastore,
      OfflineUpdateDatastore offlineUpdateDatastore,
      GoPirateDatastore goPirateDatastore) {
    this.brushingsRepository = brushingsRepository;
    this.accountDatastore = accountDatastore;
    this.profileDatastore = profileDatastore;
    this.offlineUpdateDatastore = offlineUpdateDatastore;
    this.goPirateDatastore = goPirateDatastore;
  }

  @Override
  @NonNull
  public Completable truncate() {
    return deleteBrushingsCompletable()
        .concatWith(deleteProfileCompletable())
        .concatWith(truncateAdaptersCompletable());
  }

  @VisibleForTesting
  Completable deleteBrushingsCompletable() {
    return brushingsRepository.deleteAll().onErrorComplete();
  }

  @VisibleForTesting
  Completable deleteProfileCompletable() {
    return profileDatastore.deleteAll().onErrorComplete();
  }

  @VisibleForTesting
  Completable truncateAdaptersCompletable() {
    return Completable.fromAction(
        () -> {
          try {
            accountDatastore.truncate();
          } catch (Exception e) {
            e.printStackTrace();
          }

          try {
            offlineUpdateDatastore.truncate();
          } catch (Exception e) {
            e.printStackTrace();
          }

          try {
            goPirateDatastore.truncate();
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }
}
