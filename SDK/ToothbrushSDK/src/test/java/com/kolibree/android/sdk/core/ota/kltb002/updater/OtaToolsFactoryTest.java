/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate.TYPE_FIRMWARE;
import static com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate.TYPE_GRU_DATA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.sdk.TestSdkDaggerWrapper;
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate.OtaType;
import com.kolibree.android.sdk.dagger.SdkComponent;
import com.kolibree.android.sdk.version.SoftwareVersion;
import org.junit.Test;

/** Created by miguelaragues on 3/5/18. */
@SuppressWarnings("KotlinInternalInJava")
public class OtaToolsFactoryTest extends BaseUnitTest {

  private OtaToolsFactory otaToolsFactory;

  @Override
  public void setup() throws Exception {
    super.setup();

    otaToolsFactory = spy(new OtaToolsFactory());
  }

  /*
  CREATE OTA UPDATER
   */

  @Test
  public void createOtaUpdater_supportsFastOtaFalse_returnsKLTB002BootloaderOtaUpdater() {
    doReturn(false).when(otaToolsFactory).supportsFastOta(any(InternalKLTBConnection.class));

    OtaUpdater otaUpdater =
        otaToolsFactory.createOtaUpdater(
            mock(InternalKLTBConnection.class), mock(BleDriver.class), mock(OtaUpdate.class), 0);

    assertTrue(otaUpdater instanceof KLTB002BootloaderOtaUpdater);
  }

  @Test
  public void
      createOtaUpdater_supportsFastOtaTrue_typeFirmware_returnsKLTB002BootloaderOtaUpdater() {
    doReturn(false).when(otaToolsFactory).supportsFastOta(any(InternalKLTBConnection.class));

    OtaUpdater otaUpdater =
        otaToolsFactory.createOtaUpdater(
            mock(InternalKLTBConnection.class),
            mock(BleDriver.class),
            mockOtaUpdate(TYPE_FIRMWARE),
            0);

    assertTrue(otaUpdater instanceof KLTB002BootloaderOtaUpdater);
  }

  @Test
  public void createOtaUpdater_supportsFastOtaTrue_typeGru_returnsKLTB002FastGruUpdater() {
    doReturn(true).when(otaToolsFactory).supportsFastOta(any(InternalKLTBConnection.class));

    TestSdkDaggerWrapper.setSdkComponent(mock(SdkComponent.class));

    OtaUpdater otaUpdater =
        otaToolsFactory.createOtaUpdater(
            mock(InternalKLTBConnection.class),
            mock(BleDriver.class),
            mockOtaUpdate(TYPE_GRU_DATA),
            0);

    assertTrue(otaUpdater instanceof KLTB002FastGruUpdater);
  }

  /*
  CREATE OTA WRITER
   */

  @Test
  public void createOtaWriter_supportsFastOtaFalse_returnsLegacyOtaWriter() {
    BleDriver bleDriver = mock(BleDriver.class);

    doReturn(false).when(otaToolsFactory).supportsFastOta(any(InternalKLTBConnection.class));
    OtaWriter otaWriter =
        otaToolsFactory.createOtaWriter(
            mock(InternalKLTBConnection.class), bleDriver, mock(OtaUpdate.class), 0);

    assertTrue(otaWriter instanceof LegacyOtaWriter);
  }

  @Test
  public void createOtaWriter_supportsFastOtaTrue_otaUpdateIsFirmware_returnsFastFirmwareWriter() {
    BleDriver driver = mock(BleDriver.class);

    doReturn(true).when(otaToolsFactory).supportsFastOta(any(InternalKLTBConnection.class));
    OtaWriter otaWriter =
        otaToolsFactory.createOtaWriter(
            mock(InternalKLTBConnection.class), driver, mockOtaUpdate(TYPE_FIRMWARE), 0);

    assertTrue(otaWriter instanceof FastFirmwareWriter);
  }

  @Test
  public void createOtaWriter_supportsFastOtaTrue_otaUpdateIsGru_returnsFastGruWriter() {
    BleDriver driver = mock(BleDriver.class);

    doReturn(true).when(otaToolsFactory).supportsFastOta(any(InternalKLTBConnection.class));
    OtaWriter otaWriter =
        otaToolsFactory.createOtaWriter(
            mock(InternalKLTBConnection.class), driver, mockOtaUpdate(TYPE_GRU_DATA), 0);

    assertTrue(otaWriter instanceof FastGruWriter);
  }

  /*
  SUPPORTS FAST OTA
   */

  @Test
  public void supportsFastOta_softwareVersion01765534_returnsFalse() {
    SoftwareVersion softwareVersion = new SoftwareVersion(0, 17, 65534);

    assertFalse(otaToolsFactory.supportsFastOta(mockConnectionWithBootloader(softwareVersion)));
  }

  @Test
  public void supportsFastOta_softwareVersion01665536_returnsFalse() {
    SoftwareVersion softwareVersion = new SoftwareVersion(0, 16, 65536);

    assertFalse(otaToolsFactory.supportsFastOta(mockConnectionWithBootloader(softwareVersion)));
  }

  @Test
  public void supportsFastOta_softwareVersion01765535_returnsTrue() {
    SoftwareVersion softwareVersion = new SoftwareVersion(0, 17, 65535);

    assertTrue(otaToolsFactory.supportsFastOta(mockConnectionWithBootloader(softwareVersion)));
  }

  @Test
  public void supportsFastOta_softwareVersion01765536_returnsTrue() {
    SoftwareVersion softwareVersion = new SoftwareVersion(0, 17, 65536);

    assertTrue(otaToolsFactory.supportsFastOta(mockConnectionWithBootloader(softwareVersion)));
  }

  @Test
  public void supportsFastOta_softwareVersion01865534_returnsTrue() {
    SoftwareVersion softwareVersion = new SoftwareVersion(0, 18, 65534);

    assertTrue(otaToolsFactory.supportsFastOta(mockConnectionWithBootloader(softwareVersion)));
  }

  @Test
  public void supportsFastOta_softwareVersion11665534_returnsTrue() {
    SoftwareVersion softwareVersion = new SoftwareVersion(1, 16, 65534);

    assertTrue(otaToolsFactory.supportsFastOta(mockConnectionWithBootloader(softwareVersion)));
  }

  /*
  UTILS
   */

  private InternalKLTBConnection mockConnectionWithBootloader(SoftwareVersion softwareVersion) {
    InternalKLTBConnection connection = mock(InternalKLTBConnection.class);

    Toothbrush toothbrush = mock(Toothbrush.class);
    when(toothbrush.getBootloaderVersion()).thenReturn(softwareVersion);
    when(connection.toothbrush()).thenReturn(toothbrush);

    return connection;
  }

  private OtaUpdate mockOtaUpdate(@OtaType int otaType) {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    when(otaUpdate.getType()).thenReturn(otaType);

    return otaUpdate;
  }
}
