/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.sdk.core.ota.kltb002.updater.FastFirmwareWriter.COMMAND_ID_FAST_FW_UPDATE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.mockito.Mock;

/** Created by miguelaragues on 2/5/18. */
@SuppressWarnings("KotlinInternalInJava")
public class FastFirmwareWriterTest extends BaseUnitTest {

  @Mock BleDriver driver;

  private FastFirmwareWriter updater;

  @Override
  public void setup() throws Exception {
    super.setup();

    updater = spy(new FastFirmwareWriter(driver, Schedulers.io(), 0));
  }

  /*
  VALIDATE UPDATE PRECONDITIONS OBSERVABLE
   */

  @Test
  public void validateUpdatePreconditionsObservable_notInBootloader_returnsError() {
    when(driver.isRunningBootloader()).thenReturn(false);

    updater.validateUpdatePreconditionsObservable().test().assertError(IllegalStateException.class);
  }

  @Test
  public void validateUpdatePreconditionsObservable_inBootloader_completes() {
    when(driver.isRunningBootloader()).thenReturn(true);

    updater.validateUpdatePreconditionsObservable().test().assertNoErrors().assertComplete();
  }

  /*
  GET START OTA COMMAND ID
   */
  @Test
  public void getStartOTACommandId_otaTypeFirmware_returnsCOMMAND_ID_FAST_FW_UPDATE() {
    assertEquals(COMMAND_ID_FAST_FW_UPDATE, updater.getStartOTACommandId());
  }
}
