/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing;

import android.content.Intent;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.guidedbrushing.mvi.GuidedBrushingActivity;
import com.kolibree.android.test.BaseEspressoTest;
import com.kolibree.android.test.KLBaseActivityTestRule;
import com.kolibree.android.test.KolibreeActivityTestRule;
import org.jetbrains.annotations.Nullable;

public abstract class GuidedBrushingEspressoTestBase
    extends BaseEspressoTest<GuidedBrushingActivity> {

  protected Intent intentFrom(@Nullable ToothbrushModel model, @Nullable String mac) {
    return new GuidedBrushingFactoryImpl().createConnectedGuidedBrushing(context(), mac, model);
  }

  @Override
  protected KLBaseActivityTestRule<GuidedBrushingActivity> createRuleForActivity() {
    return new KolibreeActivityTestRule.Builder<>(GuidedBrushingActivity.class)
        .launchActivity(false)
        .build();
  }
}
