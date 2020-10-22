package com.kolibree.android.persistence;

import com.kolibree.android.annotation.VisibleForApp;

/** Created by miguelaragues on 13/9/17. */
@VisibleForApp
public interface BasePreferences {

  String PREFS_FILENAME = "datapp_preferences";

  void clear();
}
