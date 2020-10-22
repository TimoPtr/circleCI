/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.kolibree_pro;

import androidx.annotation.Nullable;
import io.reactivex.Single;

/** Created by miguelaragues on 17/1/18. */
public interface KolibreeProReminders {

  void schedulePeriodicReminders(String practitionerToken, String practitionerName);

  Single<Boolean> shouldShowReminder();

  void stopAllPeriodicReminders();

  void stopPeriodicRemindersForActiveProfile();

  void stopPeriodicRemindersForProfile(long profileId);

  boolean hasMultiplePractitionersNeedingConsent();

  @Nullable
  String practitionerToken();

  @Nullable
  String practitionerName();
}
