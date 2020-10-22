/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.ui.settings;

import android.content.Context;
import androidx.annotation.NonNull;
import com.kolibree.android.annotation.VisibleForApp;
import com.kolibree.android.persistence.BasePreferencesImpl;
import javax.inject.Inject;

/** Created by miguelaragues on 6/3/18. */
@VisibleForApp
public final class SecretSettingsManager extends BasePreferencesImpl {
  // don't change 'secret_' prefix. See BaseClearUserContentJobService
  private static final String SECRET_SETTINGS_PREFS_NAME = "secret_settings";

  private static final String SECRET_SETTINGS_ENABLED_KEY = "secret_settings_enabled";

  @Inject
  protected SecretSettingsManager(Context context) {
    super(context);
  }

  public boolean shouldShowSecretSettings() {
    return getPrefs().getBoolean(SECRET_SETTINGS_ENABLED_KEY, false);
  }

  public void enableSecretSettings() {
    getPrefsEditor().putBoolean(SECRET_SETTINGS_ENABLED_KEY, true).apply();
  }

  public void disableSecretSettings() {
    getPrefsEditor().putBoolean(SECRET_SETTINGS_ENABLED_KEY, false).apply();
  }

  @NonNull
  @Override
  protected String getPreferencesName() {
    return SECRET_SETTINGS_PREFS_NAME;
  }
}
