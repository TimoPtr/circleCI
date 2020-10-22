/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test;

import android.view.View;
import androidx.fragment.app.FragmentActivity;
import com.kolibree.android.app.App;
import com.kolibree.android.app.dagger.EspressoAppComponent;
import org.junit.runner.RunWith;

/** Created by miguelaragues on 23/2/18. */
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
abstract class BaseActivityCommonEspressoTest<T extends FragmentActivity>
    extends KLBaseActivityCommonEspressoTest<T> {

  public EspressoAppComponent component() {
    return (EspressoAppComponent) App.appComponent;
  }

  protected void makeScreenshot(String name) {
    component().paparazzi().snap(aTestRule.getActivity(), name);
  }

  protected void makeScreenshot(View view, String name) {
    component().paparazzi().snap(view, name);
  }
}
