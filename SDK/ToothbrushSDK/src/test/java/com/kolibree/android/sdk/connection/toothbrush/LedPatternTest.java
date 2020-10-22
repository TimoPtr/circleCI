/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush;

import static org.junit.Assert.assertEquals;

import com.kolibree.android.sdk.connection.toothbrush.led.LedPattern;
import org.junit.Test;

/** {@link LedPattern} test unit */
public class LedPatternTest {

  @Test
  public void testFixedPattern() {
    assertEquals(0, LedPattern.FIXED.ordinal());
  }

  @Test
  public void testSinusPattern() {
    assertEquals(1, LedPattern.SINUS.ordinal());
  }

  @Test
  public void testShortPulsePattern() {
    assertEquals(2, LedPattern.SHORT_PULSE.ordinal());
  }

  @Test
  public void testLongPulsePattern() {
    assertEquals(3, LedPattern.LONG_PULSE.ordinal());
  }
}
