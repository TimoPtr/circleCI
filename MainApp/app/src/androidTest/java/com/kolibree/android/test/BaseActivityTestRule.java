/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test;

import android.content.Context;
import android.os.Build;
import androidx.fragment.app.FragmentActivity;
import com.kolibree.android.app.BaseKolibreeApplication;
import com.kolibree.android.app.dagger.EspressoAppComponent;
import com.kolibree.android.app.ui.home.brushhead_renew.BrushHeadManager;
import com.kolibree.android.persistence.BasePreferences;
import com.kolibree.android.utils.DebouncedClickListener;

/** Created by miguelaragues on 8/8/17. */
public abstract class BaseActivityTestRule<T extends FragmentActivity>
    extends KLBaseActivityTestRule<T> {

  protected BaseActivityTestRule(Class<T> activityClass, boolean launchActivity) {
    super(activityClass, launchActivity);

    clearPreferences();

    clearDatabases();

    DebouncedClickListener.allowFastClicks();
  }

  private void clearPreferences() {
    Context context = targetContext();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      context.deleteSharedPreferences(BasePreferences.PREFS_FILENAME);
      context.deleteSharedPreferences(BrushHeadManager.BRUSH_HEAD_PREFS_NAME);
    } else {
      context
          .getSharedPreferences(BasePreferences.PREFS_FILENAME, Context.MODE_PRIVATE)
          .edit()
          .clear()
          .apply();
      context
          .getSharedPreferences(BrushHeadManager.BRUSH_HEAD_PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .clear()
          .apply();
    }
  }

  private void clearDatabases() {
    EspressoAppComponent component = ((EspressoAppComponent) BaseKolibreeApplication.appComponent);
    // room databases are cleared between test because we use in memory databases for room
    component.accountDatastore().truncate();
    component.statRepository().truncate().blockingAwait();
  }

  /** InternalBuilder to isolate creation of TestRule from the world. */
  protected static class InnerBuilder<K extends FragmentActivity, B extends InnerBuilder<K, B>> {

    final Class<K> activityClass;
    protected Boolean launchActivity;

    InnerBuilder(Class<K> activityClass) {
      this.activityClass = activityClass;
    }

    public B launchActivity(boolean launchActivity) {
      this.launchActivity = launchActivity;

      return (B) this;
    }
  }
}
