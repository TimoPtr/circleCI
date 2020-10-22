package com.kolibree.android.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.annotation.VisibleForApp;

/**
 * Base class to manage preferences
 *
 * <p>Created by maragues on 7/09/17.
 *
 * <p>Preferences written by descendants of this class will automatically be wiped on user logout.
 * See ClearUserContentJobService
 */
@VisibleForApp
public abstract class BasePreferencesImpl implements BasePreferences {

  private Context context;

  public BasePreferencesImpl(Context context) {
    this.context = context.getApplicationContext();
  }

  @Override
  public void clear() {
    getPrefsEditor().clear().apply();
  }

  protected SharedPreferences.Editor getPrefsEditor() {
    return getPrefs().edit();
  }

  protected SharedPreferences getPrefs() {
    return getPrefs(getPreferencesName());
  }

  @VisibleForTesting
  protected SharedPreferences getPrefs(String name) {
    return context.getSharedPreferences(name, Context.MODE_PRIVATE);
  }

  @NonNull
  protected String getPreferencesName() {
    return PREFS_FILENAME;
  }
}
