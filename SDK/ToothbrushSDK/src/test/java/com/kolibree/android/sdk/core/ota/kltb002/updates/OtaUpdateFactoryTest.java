/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updates;

import static com.kolibree.android.commons.UpdateType.TYPE_FIRMWARE;
import static com.kolibree.android.commons.UpdateType.TYPE_GRU;
import static com.kolibree.android.sdk.core.ota.kltb002.updates.GruDataUpdateTest.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.commons.AvailableUpdate;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.error.FailureReason;
import java.io.File;
import org.junit.Test;
import org.mockito.Mock;

/** Created by miguelaragues on 10/4/18. */
@SuppressWarnings("KotlinInternalInJava")
public class OtaUpdateFactoryTest extends BaseUnitTest {

  @Mock File otaFile;

  @Mock AvailableUpdate availableUpdate;

  @Override
  public void setup() throws Exception {
    super.setup();

    when(availableUpdate.getUpdateFilePath()).thenReturn("name.bin");
    when(availableUpdate.getVersion()).thenReturn("1.1.1");
  }

  private OtaUpdateFactory otaUpdateFactory = spy(new OtaUpdateFactory());

  @Test(expected = FailureReason.class)
  public void fileDoesNotExist_throwsFailureReason() throws Exception {
    otaUpdateFactory.create(availableUpdate, ToothbrushModel.ARA);
  }

  @Test(expected = FailureReason.class)
  public void fileCanReadFalse_throwsFailureReason() throws Exception {
    when(otaFile.exists()).thenReturn(true);

    otaUpdateFactory.create(availableUpdate, ToothbrushModel.CONNECT_E1);
  }

  @Test(expected = FailureReason.class)
  public void fileEmptyName_throwsFailureReason() throws Exception {
    prepareValidFile("");

    doReturn(new byte[0]).when(otaUpdateFactory).readWholeFile(otaFile);

    otaUpdateFactory.create(availableUpdate, ToothbrushModel.CONNECT_M1);
  }

  @Test(expected = FailureReason.class)
  public void unknownUpdateType_throwsFailureReason() throws Exception {
    prepareValidFile("name");

    otaUpdateFactory.create(availableUpdate, ToothbrushModel.CONNECT_M1);
  }

  @Test
  public void fileAraFirmware_returnsAraFirmwareUpdate() throws Exception {
    prepareValidFile("name");
    when(otaUpdateFactory.getFile(anyString())).thenReturn(otaFile);

    byte[] binaryContent = new byte[0];
    doReturn(binaryContent).when(otaUpdateFactory).readWholeFile(otaFile);

    prepareFirmwareUpdate();

    AraFirmwareUpdate araFirmwareUpdate =
        (AraFirmwareUpdate) otaUpdateFactory.create(availableUpdate, ToothbrushModel.ARA);

    assertEquals(binaryContent, araFirmwareUpdate.getData());
  }

  @Test
  public void fileE1Firmware_returnsE1FirmwareUpdate() throws Exception {
    prepareValidFile("name");
    when(otaUpdateFactory.getFile(anyString())).thenReturn(otaFile);

    byte[] binaryContent = new byte[0];
    doReturn(binaryContent).when(otaUpdateFactory).readWholeFile(otaFile);

    prepareFirmwareUpdate();

    E1FirmwareUpdate e1FirmwareUpdate =
        (E1FirmwareUpdate) otaUpdateFactory.create(availableUpdate, ToothbrushModel.CONNECT_E1);

    assertEquals(binaryContent, e1FirmwareUpdate.getData());
  }

  @Test
  public void fileE1Firmware_withNullCrcFields_returnsE1FirmwareUpdate() throws Exception {
    prepareValidFile("name");
    when(otaUpdateFactory.getFile(anyString())).thenReturn(otaFile);

    byte[] binaryContent = new byte[0];
    doReturn(binaryContent).when(otaUpdateFactory).readWholeFile(otaFile);

    prepareFirmwareUpdate();
    when(availableUpdate.getCrc32()).thenReturn(null);

    E1FirmwareUpdate e1FirmwareUpdate =
        (E1FirmwareUpdate) otaUpdateFactory.create(availableUpdate, ToothbrushModel.CONNECT_E1);

    assertEquals(binaryContent, e1FirmwareUpdate.getData());
  }

  @Test
  public void fileGRUDataUpdate_unencryptedContent_returnsGRUDataUpdate() throws Exception {
    prepareValidFile("");
    when(otaUpdateFactory.getFile(anyString())).thenReturn(otaFile);

    byte[] binaryContent = UNENCRYPTED_BINARY_CONTENT;
    doReturn(binaryContent).when(otaUpdateFactory).readWholeFile(otaFile);

    prepareGruUpdate();

    GRUDataUpdate gruDataUpdate =
        (GRUDataUpdate) otaUpdateFactory.create(availableUpdate, ToothbrushModel.CONNECT_E1);

    assertEquals(binaryContent, gruDataUpdate.getData());
  }

  @Test
  public void fileGRUDataCompatUpdate_oldGruContent_returnsGRUDataCompatUpdate() throws Exception {
    prepareValidFile("");
    when(otaUpdateFactory.getFile(anyString())).thenReturn(otaFile);

    byte[] binaryContent = OLD_GRU_BINARY_CONTENT;
    doReturn(binaryContent).when(otaUpdateFactory).readWholeFile(otaFile);

    prepareGruUpdate();

    GRUDataUpdate gruDataCompatUpdate =
        (GRUDataUpdate) otaUpdateFactory.create(availableUpdate, ToothbrushModel.ARA);

    assertEquals(binaryContent, gruDataCompatUpdate.getData());
  }

  @Test
  public void fileGRUDataUpdate_encryptedContent_returnsEncryptedGRUDataUpdate() throws Exception {
    prepareValidFile("");
    when(otaUpdateFactory.getFile(anyString())).thenReturn(otaFile);

    byte[] binaryContent = ENCRYPTED_BINARY_CONTENT;
    doReturn(binaryContent).when(otaUpdateFactory).readWholeFile(otaFile);

    prepareGruUpdate();

    GRUDataUpdate gruDataUpdate =
        (GRUDataUpdate) otaUpdateFactory.create(availableUpdate, ToothbrushModel.CONNECT_E1);

    assertEquals(binaryContent, gruDataUpdate.getData());
  }

  /*
  UTILS
   */

  private void prepareValidFile(String name) {
    when(otaFile.exists()).thenReturn(true);
    when(otaFile.canRead()).thenReturn(true);

    when(otaFile.getName()).thenReturn(name);
  }

  private void prepareFirmwareUpdate() {
    when(availableUpdate.getType()).thenReturn(TYPE_FIRMWARE);
  }

  private void prepareGruUpdate() {
    when(availableUpdate.getType()).thenReturn(TYPE_GRU);
  }
}
