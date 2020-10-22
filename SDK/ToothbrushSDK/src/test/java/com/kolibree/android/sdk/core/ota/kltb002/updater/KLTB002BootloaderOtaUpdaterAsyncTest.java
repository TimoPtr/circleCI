/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromAction;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_REBOOTING;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.kolibree.android.app.test.BaseAsyncUnitTest;
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.error.FailureReason;
import io.reactivex.Completable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.Mock;

/** Created by miguelaragues on 2/5/18. */
@SuppressWarnings("KotlinInternalInJava")
public class KLTB002BootloaderOtaUpdaterAsyncTest extends BaseAsyncUnitTest {

  private KLTB002BootloaderOtaUpdater bootloaderUpdater;

  @Mock InternalKLTBConnection connection;

  @Mock BleDriver driver;

  @Override
  public void setup() throws Exception {
    super.setup();

    bootloaderUpdater = spy(new KLTB002BootloaderOtaUpdater(connection, driver, 0));

    doReturn(Schedulers.computation()).when(bootloaderUpdater).getTimeControlScheduler();
  }

  /*
  REBOOT TO MAIN APP OBSERVABLE
   */
  @Test
  public void rebootToMainAppObservable_rebootSuccessful_emitsEventRebootingAndCompletes() {
    MainAppRebooter mainAppRebooter = mock(MainAppRebooter.class);
    doReturn(mainAppRebooter).when(bootloaderUpdater).mainAppRebooter();

    when(mainAppRebooter.rebootToMainApp(connection, driver)).thenReturn(Completable.complete());

    bootloaderUpdater
        .rebootToMainAppObservable()
        .test()
        .assertValue(fromAction(OTA_UPDATE_REBOOTING))
        .assertComplete();
  }

  @Test
  public void rebootToMainAppObservable_rebootError_emitsRebootingAndError() {
    MainAppRebooter mainAppRebooter = mock(MainAppRebooter.class);
    doReturn(mainAppRebooter).when(bootloaderUpdater).mainAppRebooter();

    Throwable expectedError = mock(Throwable.class);
    when(mainAppRebooter.rebootToMainApp(connection, driver))
        .thenReturn(Completable.error(expectedError));

    bootloaderUpdater
        .rebootToMainAppObservable()
        .test()
        .assertValue(OtaUpdateEvent.fromAction(OTA_UPDATE_REBOOTING))
        .assertError(expectedError);
  }

  @Test
  public void rebootToMainAppObservable_31SecondsDelay_emitsTimeoutException() {
    MainAppRebooter mainAppRebooter = mock(MainAppRebooter.class);
    doReturn(mainAppRebooter).when(bootloaderUpdater).mainAppRebooter();

    when(mainAppRebooter.rebootToMainApp(connection, driver))
        .thenReturn(Completable.complete().delay(31, TimeUnit.SECONDS));

    TestObserver<OtaUpdateEvent> observer =
        bootloaderUpdater
            .rebootToMainAppObservable()
            .test()
            .assertValue(OtaUpdateEvent.fromAction(OTA_UPDATE_REBOOTING));

    observer.assertNoErrors();

    advanceTimeBy(31, TimeUnit.SECONDS);

    observer.assertError(FailureReason.class);
  }
}
