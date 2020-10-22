/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.account;

import com.kolibree.android.accountinternal.AccountInternalRoomModule;
import com.kolibree.android.accountinternal.CurrentAccountAndProfileIdsProvider;
import com.kolibree.android.accountinternal.CurrentAccountAndProfileIdsProviderImpl;
import com.kolibree.android.accountinternal.CurrentProfileProvider;
import com.kolibree.android.accountinternal.CurrentProfileProviderImpl;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastoreImpl;
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore;
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastoreImpl;
import com.kolibree.android.accountinternal.profile.persistence.dao.ProfileDao;
import com.kolibree.android.app.dagger.AppScope;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module(includes = AccountInternalRoomModule.class)
public abstract class AccountInternalModule {

  @Provides
  static ProfileDatastore providesProfileDatastore(ProfileDao profileDao) {
    return new ProfileDatastoreImpl(profileDao);
  }

  @Binds
  abstract AccountDatastore bindsAccountDataStore(AccountDatastoreImpl accountDataStore);

  @Binds
  @AppScope
  abstract CurrentProfileProvider bindsAccountProvider(CurrentProfileProviderImpl accountProvider);

  @Binds
  @AppScope
  abstract CurrentAccountAndProfileIdsProvider bindsCurrentAccountAndProfileIdsProvider(
      CurrentAccountAndProfileIdsProviderImpl provider);
}
