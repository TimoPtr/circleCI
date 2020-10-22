/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.sdk.core.ota.kltb002.updater.MainAppRebooter.DISCONNECTION_INTERVAL_SECONDS;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.kolibree.android.app.test.BaseAsyncUnitTest;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.error.ConnectionEstablishException;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.Spy;

/** Created by miguelaragues on 3/5/18. */
@SuppressWarnings("KotlinInternalInJava")
public class MainAppRebooterTest extends BaseAsyncUnitTest {

  @Spy MainAppRebooter rebooter;

  @Override
  public void setup() throws Exception {
    super.setup();

    doReturn(Schedulers.computation()).when(rebooter).getTimeControlScheduler();
  }

  @Test
  public void rebootToMainApp_driverCheckDeferredUntilSubscription() {
    BleDriver driver = mock(BleDriver.class);
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);

    when(driver.isRunningBootloader()).thenReturn(true);

    rebooter.rebootToMainApp(connection, driver);

    verify(driver, never()).isRunningBootloader();

    rebooter.rebootToMainApp(connection, driver).test();

    verify(driver).isRunningBootloader();
  }

  @Test
  public void rebootToMainApp_notInBootloader_completesImmediately() {
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);
    BleDriver bleDriver = mock(BleDriver.class);
    when(bleDriver.isRunningBootloader()).thenReturn(false);

    rebooter.rebootToMainApp(connection, bleDriver).test().assertComplete();
  }

  @Test
  public void rebootToMainApp_inBootloader_invokesEstablishConnectionCompletableAfterDelay() {
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);
    BleDriver driver = mock(BleDriver.class);
    when(driver.isRunningBootloader()).thenReturn(true);

    boolean[] establishInvoked = new boolean[1];
    Completable establishCompletable =
        Completable.create(
            e -> {
              establishInvoked[0] = true;

              e.onComplete();
            });
    doReturn(establishCompletable)
        .when(rebooter)
        .establishConnectionCompletable(connection, driver);

    rebooter.rebootToMainApp(connection, driver).test();

    verify(connection).disconnect();

    assertFalse(establishInvoked[0]);

    advanceTimeBy(DISCONNECTION_INTERVAL_SECONDS, TimeUnit.SECONDS);

    assertTrue(establishInvoked[0]);
  }

  @Test
  public void
      rebootToMainApp_inBootloaderAfterEstablishConnectionCompletableAfterDelay_reinvokesDisconnectAndEstablishCompletable() {
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);
    BleDriver driver = mock(BleDriver.class);
    when(driver.isRunningBootloader()).thenReturn(true, true, false);

    int[] establishInvocations = new int[1];
    Completable establishCompletable =
        Completable.create(
            e -> {
              establishInvocations[0]++;

              e.onComplete();
            });
    doReturn(establishCompletable)
        .when(rebooter)
        .establishConnectionCompletable(connection, driver);

    rebooter.rebootToMainApp(connection, driver).test();

    verify(connection).disconnect();

    advanceTimeBy(DISCONNECTION_INTERVAL_SECONDS * 2, TimeUnit.SECONDS);

    verify(connection, times(2)).disconnect();
    assertEquals(2, establishInvocations[0]);
  }

  @Test
  public void rebootToMainApp_inBootloader_completes_setsStateActive() {
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);
    BleDriver driver = mock(BleDriver.class);
    when(driver.isRunningBootloader()).thenReturn(true, false);

    doReturn(Completable.complete())
        .when(rebooter)
        .establishConnectionCompletable(connection, driver);

    rebooter.rebootToMainApp(connection, driver).test();

    verify(connection, never()).setState(KLTBConnectionState.ACTIVE);

    advanceTimeBy(DISCONNECTION_INTERVAL_SECONDS, TimeUnit.SECONDS);

    verify(connection).setState(KLTBConnectionState.ACTIVE);
  }

  /*
  ESTABLISH CONNECTION COMPLETABLE
   */

  @Test
  public void
      establishConnectionCompletable_connectionErrorOnFirstAttempt_invokesDisconnectAndRetriesAfterDelay() {
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);
    BleDriver bleDriver = mock(BleDriver.class);
    when(connection.establishCompletable())
        .thenReturn(
            Completable.create(
                new CompletableOnSubscribe() {
                  boolean firstInvocation = true;

                  @Override
                  public void subscribe(CompletableEmitter emitter) throws Exception {
                    if (firstInvocation) {
                      firstInvocation = false;

                      emitter.onError(mock(ConnectionEstablishException.class));
                    } else {
                      emitter.onComplete();
                    }
                  }
                }));

    TestObserver<Void> observer =
        rebooter.establishConnectionCompletable(connection, bleDriver).test();

    verify(connection).disconnect();

    observer.assertComplete();
  }

  @Test
  public void establishConnectionCompletable_connectionException_doesNotRetryAndEmitsError() {
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);
    BleDriver bleDriver = mock(BleDriver.class);
    Throwable expectedError = mock(Throwable.class);
    when(connection.establishCompletable()).thenReturn(Completable.error(expectedError));

    rebooter
        .establishConnectionCompletable(connection, bleDriver)
        .test()
        .assertError(expectedError);
  }
}
