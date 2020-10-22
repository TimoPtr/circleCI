/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.test.mocks.rewards;

import com.kolibree.android.rewards.models.ProfileSmilesEntity;
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly;

@SuppressWarnings("KotlinInternalInJava")
public class ProfileRewardsBuilder {
  public static SynchronizableReadOnly createProfileSmiles(long profileId, int smiles) {
    return new ProfileSmilesEntity(profileId, smiles);
  }
}
