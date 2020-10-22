/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** {@link SwitchOffMode} test unit */
public class SwitchOffModeTest {

  private static final String ERROR =
      "Don't change ordinals unless you change " + "the Commands class as well";

  @Test
  public void testHardOffMode() {
    assertEquals(ERROR, 0, SwitchOffMode.HARD_OFF.ordinal());
  }

  @Test
  public void testRebootMode() {
    assertEquals(ERROR, 1, SwitchOffMode.REBOOT.ordinal());
  }

  @Test
  public void testSoftOffMode() {
    assertEquals(ERROR, 2, SwitchOffMode.SOFT_OFF.ordinal());
  }

  @Test
  public void testFactoryBackupHardOffMode() {
    assertEquals(ERROR, 3, SwitchOffMode.FACTORY_HARD_OFF.ordinal());
  }

  @Test
  public void testResetBackupDomainMode() {
    assertEquals(ERROR, 4, SwitchOffMode.RESET_BACKUP_DOMAIN.ordinal());
  }

  @Test
  public void testTravelMode() {
    assertEquals(ERROR, 5, SwitchOffMode.TRAVEL_MODE.ordinal());
  }
}
