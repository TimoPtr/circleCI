/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.data.model;

import static org.junit.Assert.*;

import com.kolibree.android.clock.TrustedClock;
import org.junit.Test;

/** Created by miguelaragues on 6/2/18. */
public class BrushingTest {

  @Test
  public void brushingWithNullProcessedData_getProcessedDataIsEmptyString() {
    Brushing brushing =
        new Brushing(0L, 120, TrustedClock.getNowOffsetDateTime(), 0, 0, null, 0L, 0L, "", "mac");

    assertNotNull(brushing.getProcessedData());
  }

  @Test
  public void hasProcessedData_emptyProcessedData_returnsFalse() {
    Brushing brushing =
        new Brushing(0L, 120, TrustedClock.getNowOffsetDateTime(), 0, 0, null, 0L, 0L, "", "mac");

    assertFalse(brushing.hasProcessedData());
  }

  @Test
  public void hasProcessedData_withProcessedData_returnsTrue() {
    Brushing brushing =
        new Brushing(0L, 120, TrustedClock.getNowOffsetDateTime(), 0, 0, "{}", 0L, 0L, "", "mac");

    assertTrue(brushing.hasProcessedData());
  }
}
