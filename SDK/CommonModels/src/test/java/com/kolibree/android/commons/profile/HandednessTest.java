/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.commons.profile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** {@link Handedness} test unit */
public class HandednessTest {

  @Test
  public void testRightHanded_name() {
    assertEquals("RIGHT_HANDED", Handedness.RIGHT_HANDED.name());
  }

  @Test
  public void testLeftHanded_name() {
    assertEquals("LEFT_HANDED", Handedness.LEFT_HANDED.name());
  }

  @Test
  public void testUnknown_name() {
    assertEquals("UNKNOWN", Handedness.UNKNOWN.name());
  }

  @Test
  public void testRightHanded_serializedName() {
    assertEquals("R", Handedness.RIGHT_HANDED.getSerializedName());
  }

  @Test
  public void testLeftHanded_serializedName() {
    assertEquals("L", Handedness.LEFT_HANDED.getSerializedName());
  }

  @Test
  public void testUnknown_serializedName() {
    assertEquals("", Handedness.UNKNOWN.getSerializedName());
  }

  @Test
  public void findBySerializedName_returnsRIGHT_HANDED_whenParameterIsR() {
    assertEquals(Handedness.RIGHT_HANDED, Handedness.findBySerializedName("R"));
  }

  @Test
  public void findBySerializedName_returnsLEFT_HANDED_whenParameterIsL() {
    assertEquals(Handedness.LEFT_HANDED, Handedness.findBySerializedName("L"));
  }

  @Test
  public void findBySerializedName_returnsUNKNOWN_whenParameterIsNullOrEmptyOrRandom() {
    assertEquals(Handedness.UNKNOWN, Handedness.findBySerializedName(null));
    assertEquals(Handedness.UNKNOWN, Handedness.findBySerializedName(""));
    assertEquals(Handedness.UNKNOWN, Handedness.findBySerializedName("U"));
    assertEquals(Handedness.UNKNOWN, Handedness.findBySerializedName("dasdas"));
    assertEquals(Handedness.UNKNOWN, Handedness.findBySerializedName("1323"));
  }
}
