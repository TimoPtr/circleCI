/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import io.reactivex.observers.TestObserver;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

/** {@link RootCommonBleImpl} test unit */
public class RootCommonBleImplTest {

  /** Dummy passkey */
  private static final int DUMMY_PASSKEY = 0x01010101;

  private RootCommonBleImpl test;

  @Before
  public void setUp() throws Exception {
    // CommonBleDriver mock
    final BleDriver driver = mock(BleDriver.class);

    // sendCommand method mock
    doAnswer(
            invocation -> {
              if (!Arrays.equals(
                  (byte[]) invocation.getArguments()[0],
                  new byte[] {0x14, 0x01, 0x01, 0x01, 0x01})) {
                throw new Exception();
              }

              return null;
            })
        .when(driver)
        .sendCommand(any(byte[].class));

    test = new RootCommonBleImpl(driver);
  }

  @Test
  public void testGrantAccess() {
    final TestObserver observer = test.grantAccess(DUMMY_PASSKEY).test();
    observer.assertNoErrors();
    observer.assertComplete();
    assertTrue(test.isAccessGranted());
  }

  @Test
  public void testGrantAccess_WrongPasskey() {
    final TestObserver observer = test.grantAccess(0x45454545).test();
    observer.assertError(Exception.class);
    assertFalse(test.isAccessGranted());
  }

  @Test
  public void testCheckMacAddress() {
    test.checkMacAddress("00:11:22:AC:44:55");
    test.checkMacAddress("00-11-22-AC-44-55");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckMacAddress_TooLong() {
    test.checkMacAddress("00:11:22:AC:44:55:66");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckMacAddress_TooShort() {
    test.checkMacAddress("00:11:22:AC:44");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckMacAddress_NoSeparator() {
    test.checkMacAddress("001122AC4455");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckMacAddress_InvalidChars() {
    test.checkMacAddress("00:11:22:AC:44:AG");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckMacAddress_Overflow() {
    test.checkMacAddress("00:111:22:AC:44:AA");
  }

  @Test
  public void testGetMacAddressBytes() {
    assertArrayEquals(
        new byte[] {0x01, 0x02, 0x03, (byte) 0xAC, (byte) 0xBB, (byte) 0xEF},
        test.getMacAddressBytes("01:02:03:AC:BB:EF"));
  }

  @Test
  public void testSetSerialNumber() { // /!\ Never set this serial to a real brush !!!!
    test.grantAccess(DUMMY_PASSKEY)
        .subscribe(
            () -> {
              final TestObserver observer = test.setSerialNumber("KLTB002EW1601K00050").test();
              observer.assertNoErrors();
              observer.assertComplete();
            });
  }

  @Test
  public void testSetSerialNumber_TooLong() {
    test.grantAccess(DUMMY_PASSKEY)
        .subscribe(
            () -> {
              final TestObserver observer = test.setSerialNumber("KLTB002EW1601K00050X").test();
              observer.assertError(Exception.class);
            });
  }

  @Test
  public void testSetMacAddress() {
    test.grantAccess(DUMMY_PASSKEY)
        .subscribe(
            () -> {
              final TestObserver observer = test.setMacAddress("12:34:56:78:9A:BC").test();
              observer.assertNoErrors();
              observer.assertComplete();
            });
  }
}
