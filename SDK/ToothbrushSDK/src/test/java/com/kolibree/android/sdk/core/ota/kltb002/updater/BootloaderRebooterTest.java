/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.sdk.core.ota.kltb002.updater.BootloaderRebooter.COMMAND_ID_REBOOT_TO_BOOTLOADER;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BootloaderRebooter.DISCONNECTION_INTERVAL_SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kolibree.android.app.test.BaseAsyncUnitTest;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.ota.kltb002.updater.BootloaderRebooter.UnableToRebootToBootloaderException;
import com.kolibree.android.test.TestForcedException;
import com.kolibree.android.test.mocks.KLTBConnectionBuilder;
import io.reactivex.Completable;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.Spy;

/** Created by miguelaragues on 3/5/18. */
@SuppressWarnings("KotlinInternalInJava")
public class BootloaderRebooterTest extends BaseAsyncUnitTest {

  @Spy BootloaderRebooter bootloaderRebooter;

  @Override
  public void setup() throws Exception {
    super.setup();

    doReturn(Schedulers.computation()).when(bootloaderRebooter).getTimeControlScheduler();
  }

  /*
  START UPDATE AFTER DFU_BOOTLOADER RESTART
   */
  @Test
  public void rebootToBootloader_driverCheckDeferredUntilSubscription() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);

    when(driver.isRunningBootloader()).thenReturn(true);

    bootloaderRebooter.rebootToBootloader(connection, driver);

    verify(driver, never()).isRunningBootloader();

    bootloaderRebooter.rebootToBootloader(connection, driver).test();

    verify(driver).isRunningBootloader();
  }

  @Test
  public void rebootToBootloader_alreadyInBootloader_returnsComplete() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);

    when(driver.isRunningBootloader()).thenReturn(true);

    bootloaderRebooter.rebootToBootloader(connection, driver).test().assertComplete();
  }

  @Test
  public void rebootToBootloader_sendsRebootToBootloaderError_emitsError() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);

    Throwable expectedError = new Throwable("Test forced error");
    doReturn(Completable.error(expectedError))
        .when(bootloaderRebooter)
        .sendRebootToBootloader(driver);

    when(connection.establishCompletable()).thenReturn(Completable.complete());

    doReturn(Completable.complete()).when(bootloaderRebooter).disconnectCompletable(connection);

    TestObserver<Void> observer = bootloaderRebooter.rebootToBootloader(connection, driver).test();

    observer.assertNoErrors();

    advanceTimeBy(DISCONNECTION_INTERVAL_SECONDS, TimeUnit.SECONDS);

    observer.assertError(expectedError);
  }

  @Test
  public void rebootToBootloader_sendRebootToBootloaderCompletes_invokesDisconnectOnce() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);
    doReturn(Completable.complete()).when(bootloaderRebooter).sendRebootToBootloader(driver);
    int[] disconnectCounter = new int[] {0};
    doReturn(
            Completable.create(
                e -> {
                  disconnectCounter[0]++;

                  e.onComplete();

                  advanceTimeBy(DISCONNECTION_INTERVAL_SECONDS, TimeUnit.SECONDS);
                }))
        .when(bootloaderRebooter)
        .disconnectCompletable(connection);

    when(connection.establishCompletable()).thenReturn(Completable.complete());

    bootloaderRebooter.rebootToBootloader(connection, driver).test();

    assertEquals(1, disconnectCounter[0]);
  }

  @Test
  public void
      rebootToBootloader_sendRebootToBootloaderCompletes_disconnectCompletes_invokesEstablishConnectionCompletableAfterDelay() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);
    doReturn(Completable.complete()).when(bootloaderRebooter).sendRebootToBootloader(driver);
    final boolean[] establishCompletableInvoked = new boolean[1];
    doReturn(
            Completable.create(
                e -> {
                  establishCompletableInvoked[0] = true;

                  e.onComplete();
                }))
        .when(bootloaderRebooter)
        .establishConnectionCompletable(connection, driver);

    bootloaderRebooter.rebootToBootloader(connection, driver).test();

    assertFalse(establishCompletableInvoked[0]);

    advanceTimeBy(DISCONNECTION_INTERVAL_SECONDS, TimeUnit.SECONDS);

    assertTrue(establishCompletableInvoked[0]);
  }

  /*
  ESTABLISH CONNECTION COMPLETABLE
   */

  @Test
  public void repeatUntilBootloaderSupplier_inBootloader_returnsTrue() throws Exception {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);

    doReturn(true).when(driver).isRunningBootloader();

    assertTrue(bootloaderRebooter.repeatUntilBootloaderSupplier(connection, driver).getAsBoolean());

    verify(bootloaderRebooter, never()).rebootToBootloader(connection, driver);
  }

  @Test(expected = UnableToRebootToBootloaderException.class)
  public void repeatUntilBootloaderSupplier_notInBootloader_emitsExceptionAfter3rdTry()
      throws Exception {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);

    doReturn(false).when(driver).isRunningBootloader();

    BooleanSupplier supplier = bootloaderRebooter.repeatUntilBootloaderSupplier(connection, driver);
    assertFalse(supplier.getAsBoolean());
    assertFalse(supplier.getAsBoolean());
    assertFalse(supplier.getAsBoolean());
  }

  @Test
  public void
      repeatUntilBootloaderSupplier_notInBootloader_invokesRebootToBootloaderAndDisconnectTwice()
          throws Exception {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);

    doReturn(false).when(driver).isRunningBootloader();

    assertFalse(
        bootloaderRebooter.repeatUntilBootloaderSupplier(connection, driver).getAsBoolean());

    BooleanSupplier supplier = bootloaderRebooter.repeatUntilBootloaderSupplier(connection, driver);
    supplier.getAsBoolean();
    supplier.getAsBoolean();
    try {
      supplier.getAsBoolean();
    } catch (UnableToRebootToBootloaderException ignore) {
      // we want to verify some behavior
    }

    verify(connection, times(3)).disconnect();
  }

  @Test
  public void repeatUntilBootloaderSupplier_inBootloaderAfter2ndAttempt_returnsTrue()
      throws Exception {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);

    doReturn(false, true).when(driver).isRunningBootloader();

    BooleanSupplier supplier = bootloaderRebooter.repeatUntilBootloaderSupplier(connection, driver);
    assertFalse(supplier.getAsBoolean());
    assertTrue(supplier.getAsBoolean());
  }

  @Test
  public void
      repeatUntilBootloaderSupplier_inBootloaderAfter2ndAttempt_invokesRebootToBootloaderAndDisconnectOnce()
          throws Exception {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);

    doReturn(false, true).when(driver).isRunningBootloader();

    BooleanSupplier supplier = bootloaderRebooter.repeatUntilBootloaderSupplier(connection, driver);
    supplier.getAsBoolean();
    supplier.getAsBoolean();

    verify(connection).disconnect();
  }

  /*
  ESTABLISH CONNECTION COMPLETABLE
   */

  @Test
  public void establishConnectionCompletable_stateACTIVE_runningBootloader_completes() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .withBootloader(true)
            .build();

    bootloaderRebooter.establishConnectionCompletable(connection, driver).test().assertComplete();
  }

  @Test
  public void
      establishConnectionCompletable_stateACTIVE_notRunningBootloader_booleanSupplierReturnsTrue_completes() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .withBootloader(false)
            .build();
    int[] establishCompletableCounter = new int[] {0};
    when(connection.establishCompletable())
        .thenReturn(
            Completable.create(
                e -> {
                  establishCompletableCounter[0]++;

                  e.onComplete();
                }));

    doReturn((BooleanSupplier) () -> true)
        .when(bootloaderRebooter)
        .repeatUntilBootloaderSupplier(connection, driver);

    bootloaderRebooter.establishConnectionCompletable(connection, driver).test().assertComplete();

    assertEquals(1, establishCompletableCounter[0]);
  }

  @Test
  public void establishConnectionCompletable_stateNotActive_booleanSupplierReturnsTrue_completes() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.OTA).build();
    int[] establishCompletableCounter = new int[] {0};
    when(connection.establishCompletable())
        .thenReturn(
            Completable.create(
                e -> {
                  establishCompletableCounter[0]++;

                  e.onComplete();
                }));

    doReturn((BooleanSupplier) () -> true)
        .when(bootloaderRebooter)
        .repeatUntilBootloaderSupplier(connection, driver);

    bootloaderRebooter.establishConnectionCompletable(connection, driver).test().assertComplete();

    assertEquals(1, establishCompletableCounter[0]);
  }

  @Test
  public void
      establishConnectionCompletable_stateNotActive_booleanSupplierThrowsError_emitsError() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.OTA).build();
    int[] establishCompletableCounter = new int[] {0};
    when(connection.establishCompletable())
        .thenReturn(
            Completable.create(
                e -> {
                  establishCompletableCounter[0]++;

                  e.onComplete();
                }));

    Exception expectedError = mock(Exception.class);
    doReturn(
            (BooleanSupplier)
                () -> {
                  throw expectedError;
                })
        .when(bootloaderRebooter)
        .repeatUntilBootloaderSupplier(connection, driver);

    bootloaderRebooter
        .establishConnectionCompletable(connection, driver)
        .test()
        .assertError(expectedError);
  }

  @Test
  public void
      establishConnectionCompletable_stateNotActive_booleanSupplierReturnsFalseAndThenTrue_retriesEstablishCompletable() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.OTA).build();
    int[] establishCompletableCounter = new int[] {0};
    when(connection.establishCompletable())
        .thenReturn(
            Completable.create(
                e -> {
                  establishCompletableCounter[0]++;

                  e.onComplete();
                }));

    BooleanSupplier booleanSupplier =
        new BooleanSupplier() {
          int invocations = 0;

          @Override
          public boolean getAsBoolean() throws Exception {
            return invocations++ > 0;
          }
        };

    doReturn(booleanSupplier)
        .when(bootloaderRebooter)
        .repeatUntilBootloaderSupplier(connection, driver);

    bootloaderRebooter.establishConnectionCompletable(connection, driver).test().assertComplete();

    assertEquals(2, establishCompletableCounter[0]);
  }

  @Test
  public void establishConnectionCompletable_stateNotActive_establishCompletableError_emitsError() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.OTA).build();
    Throwable expectedError = mock(Throwable.class);
    when(connection.establishCompletable()).thenReturn(Completable.error(expectedError));

    bootloaderRebooter
        .establishConnectionCompletable(connection, driver)
        .test()
        .assertError(expectedError);
  }

  /*
  SEND REBOOT TO DFU_BOOTLOADER
   */
  @Test
  public void sendRebootToBootloader_invokeswriteCharacteristicCompletableWithExpectedParameters() {
    BleDriver driver = mock(BleDriver.class);

    byte[] expectedPayload = new byte[] {COMMAND_ID_REBOOT_TO_BOOTLOADER};

    when(driver.writeOtaUpdateStartCharacteristic(expectedPayload))
        .thenReturn(Completable.complete());

    bootloaderRebooter.sendRebootToBootloader(driver).test().assertComplete();

    verify(driver).writeOtaUpdateStartCharacteristic(expectedPayload);
  }

  /*
  This is needed as long as they don't fix the FW bug specified in the comments
   */
  @Test
  public void sendRebootToBootloader_writeCharacteristicError_ignoresErrorAndCompletes() {
    BleDriver driver = mock(BleDriver.class);

    byte[] expectedPayload = new byte[] {COMMAND_ID_REBOOT_TO_BOOTLOADER};

    when(driver.writeOtaUpdateStartCharacteristic(expectedPayload))
        .thenReturn(Completable.error(new TestForcedException()));

    bootloaderRebooter.sendRebootToBootloader(driver).test().assertNoErrors().assertComplete();
  }
}
