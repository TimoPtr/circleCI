/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.android.test;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.fragment.app.FragmentActivity;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.platform.app.InstrumentationRegistry;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.test.extensions.TrustedClockExtensionsKt;
import com.kolibree.android.test.idlingresources.IdlingResourceFactory;
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/** Created by miguelaragues on 23/2/18. */
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public abstract class KLBaseActivityCommonEspressoTest<T extends FragmentActivity> {
  KLBaseActivityTestRule<T> aTestRule;

  @Rule public ScreenshotTestRule screenshotTestRule = new ScreenshotTestRule();

  @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

  @Rule public RuleChain rules = RuleChain.emptyRuleChain().around(getActivityTestRule());

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  private SystemAnimations systemAnimations;

  private IdlingResource picassoIdlingResource = IdlingResourceFactory.INSTANCE.picassoIdle();

  protected final KLBaseActivityTestRule<T> getActivityTestRule() {
    if (aTestRule == null) {
      aTestRule = createRuleForActivity();
    }

    return aTestRule;
  }

  protected abstract KLBaseActivityTestRule<T> createRuleForActivity();

  protected T getActivity() {
    return aTestRule.getActivity();
  }

  protected void initActivity() {
    aTestRule.launchActivity(null);
  }

  @Before
  @SuppressLint("VisibleForTests")
  public void setUp() throws Exception {
    FailEarly.overrideDelegateWith(NoopTestDelegate.INSTANCE);
    systemAnimations = new SystemAnimations(context());
    systemAnimations.disableAll();

    TrustedClockExtensionsKt.reset(TrustedClock.INSTANCE);

    setupPicasso();
  }

  private void setupPicasso() {
    IdlingRegistry.getInstance().register(picassoIdlingResource);
  }

  @After
  public void tearDown() {
    aTestRule = null;
    systemAnimations.enableAll();

    IdlingRegistry.getInstance().unregister(picassoIdlingResource);
  }

  protected Context context() {
    return InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
  }
}
