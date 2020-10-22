package com.kolibree.android.test;

import androidx.fragment.app.FragmentActivity;
import androidx.test.filters.LargeTest;
import com.kolibree.android.test.rules.EspressoImmediateRxSchedulersOverrideRule;
import com.kolibree.android.test.utils.AppMocker;
import com.kolibree.android.test.utils.SdkBuilder;
import org.junit.Rule;
import org.junit.runner.RunWith;

/** Created by miguelaragues on 8/8/17. */
@LargeTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public abstract class BaseEspressoTest<T extends FragmentActivity>
    extends BaseActivityCommonEspressoTest<T> {

  @Rule
  public final EspressoImmediateRxSchedulersOverrideRule mOverrideSchedulersRule =
      new EspressoImmediateRxSchedulersOverrideRule();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    AppMocker.create()
        .withSdkBuilder(SdkBuilder.create().prepareForMainScreen())
        .prepareForMainScreen()
        .mock();
  }
}
