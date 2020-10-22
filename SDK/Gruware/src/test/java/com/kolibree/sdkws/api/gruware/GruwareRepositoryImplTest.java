/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.api.gruware;

import static com.kolibree.sdkws.api.response.GruwareResponseTest.FW_FILENAME;
import static com.kolibree.sdkws.api.response.GruwareResponseTest.FW_UPDATE_URL;
import static com.kolibree.sdkws.api.response.GruwareResponseTest.FW_VERSION;
import static com.kolibree.sdkws.api.response.GruwareResponseTest.GRU_FILENAME;
import static com.kolibree.sdkws.api.response.GruwareResponseTest.GRU_UPDATE_URL;
import static com.kolibree.sdkws.api.response.GruwareResponseTest.GRU_VERSION;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.commons.AvailableUpdate;
import com.kolibree.android.commons.UpdateType;
import com.kolibree.android.network.utils.FileDownloader;
import com.kolibree.android.test.SharedTestUtils;
import com.kolibree.sdkws.api.response.GruwareResponse;
import com.kolibree.sdkws.data.model.GruwareData;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.SingleSubject;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class GruwareRepositoryImplTest extends BaseUnitTest {

  static final String MODEL = "model";
  static final String HW = "hw";
  static final String FW = "fw";
  static final String SERIAL = "serial";
  private static final Long GRU_CRC = 0x058345fbL;
  private static final Long FW_CRC = 0x9d752091L;
  private final String GRUWARE_RESPONSE =
      "{\n"
          + "\t\"gru\": {\n"
          + "\t\t\"gru\": \""
          + GRU_VERSION
          + "\",\n"
          + "\t\t\"crc16\": \"\",\n"
          + "\t\t\"filename\": \""
          + GRU_FILENAME
          + "\",\n"
          + "\t\t\"beta\": true,\n"
          + "\t\t\"link\": \""
          + GRU_UPDATE_URL
          + "\",\n"
          + "\t\t\"crc32\": \"058345fb\"\n"
          + "\t},\n"
          + "\t\"fw\": {\n"
          + "\t\t\"crc16\": \"\",\n"
          + "\t\t\"fw\": \""
          + FW_VERSION
          + "\",\n"
          + "\t\t\"filename\": \""
          + FW_FILENAME
          + "\",\n"
          + "\t\t\"beta\": true,\n"
          + "\t\t\"link\": \""
          + FW_UPDATE_URL
          + "\",\n"
          + "\t\t\"crc32\": \"9d752091\"\n"
          + "\t}\n"
          + "}";
  private final String GRUWARE_RESPONSE_EMPTY_GRU =
      "{\n"
          + "\t\"gru\": {},\n"
          + "\t\"fw\": {\n"
          + "\t\t\"crc16\": null,\n"
          + "\t\t\"fw\": \""
          + FW_VERSION
          + "\",\n"
          + "\t\t\"filename\": \""
          + FW_FILENAME
          + "\",\n"
          + "\t\t\"beta\": false,\n"
          + "\t\t\"link\": \""
          + FW_UPDATE_URL
          + "\",\n"
          + "\t\t\"crc32\": null\n"
          + "\t}\n"
          + "}";
  @Mock private GruwareManager gruWareManager;

  @Mock private FileDownloader fileDownloader;

  private GruwareRepositoryImpl gruwareRepository;

  @Before
  public void setup() throws Exception {
    super.setup();

    gruwareRepository = Mockito.spy(new GruwareRepositoryImpl(gruWareManager, fileDownloader));
  }

  @Test
  public void getGruwareInfo_forPlaqless() throws IOException {
    File dspFile = Mockito.mock(File.class);
    File bootloaderFile = Mockito.mock(File.class);
    File fwFile = Mockito.mock(File.class);
    Mockito.when(
            fileDownloader.download(
                "https://kolibree-firmware.s3.amazonaws.com/prod/dsp/pmm-plaqless-dsp-0-1-2-ND-Jenkins-T.zip?AWSAccessKeyId=AKIAI34BUWYJTE7AFAIA&Expires=1573141980&Signature=Yjb8nVd2yqs5vZ0dHMdlSsugMAI%3D",
                "pmm-plaqless-dsp-0-1-2-ND-Jenkins-T.zip"))
        .thenReturn(dspFile);
    Mockito.when(
            fileDownloader.download(
                "https://kolibree-firmware.s3.amazonaws.com/prod/bootloader/pmm-bootloader-plaqless-3.0.2.zip?AWSAccessKeyId=AKIAI34BUWYJTE7AFAIA&Expires=1573141980&Signature=skVT%2Fisk9DZX%2FoQR2R1ycJUeKoo%3D",
                "pmm-bootloader-plaqless-3.0.2.zip"))
        .thenReturn(bootloaderFile);
    Mockito.when(
            fileDownloader.download(
                "https://kolibree-firmware.s3.amazonaws.com/prod/fw/pmm-plaqless-2.0.4.zip?AWSAccessKeyId=AKIAI34BUWYJTE7AFAIA&Expires=1573141980&Signature=uVpUC4M6cpOTmOz7MUc2Mgj4FA0%3D",
                "pmm-plaqless-2.0.4.zip"))
        .thenReturn(fwFile);

    String fwAbsolutePath = "fwPath";
    String bootloaderAbsolutePath = "bootloaderPath";
    String dspAbsolutePath = "gruPath";
    Mockito.when(dspFile.getAbsolutePath()).thenReturn(dspAbsolutePath);
    Mockito.when(fwFile.getAbsolutePath()).thenReturn(fwAbsolutePath);
    Mockito.when(bootloaderFile.getAbsolutePath()).thenReturn(bootloaderAbsolutePath);

    GruwareData expectedData =
        GruwareData.create(
            AvailableUpdate.create("2.0.4", fwAbsolutePath, UpdateType.TYPE_FIRMWARE, null),
            AvailableUpdate.empty(UpdateType.TYPE_GRU),
            AvailableUpdate.create(
                "3.0.2", bootloaderAbsolutePath, UpdateType.TYPE_BOOTLOADER, null),
            AvailableUpdate.create("0.1.2", dspAbsolutePath, UpdateType.TYPE_DSP, null));

    String json = SharedTestUtils.getJson("json/gruware_for_plaqless.json");

    GruwareResponse gruwareResponse = new Gson().fromJson(json, GruwareResponse.class);
    Mockito.when(gruWareManager.getGruwareInfos(MODEL, HW, SERIAL, FW))
        .thenReturn(Single.just(gruwareResponse));

    gruwareRepository.getGruwareInfo(MODEL, HW, SERIAL, FW).test().assertValue(expectedData);
  }

  @Test
  public void testGetGruWareDetailInfo() throws IOException {
    File fwFile = Mockito.mock(File.class);
    File gruFile = Mockito.mock(File.class);
    Mockito.when(fileDownloader.download(FW_UPDATE_URL, FW_FILENAME)).thenReturn(fwFile);
    Mockito.when(fileDownloader.download(GRU_UPDATE_URL, GRU_FILENAME)).thenReturn(gruFile);

    String fwAbsolutePath = "fwPath";
    String gruAbsolutePath = "greyPath";
    Mockito.when(fwFile.getAbsolutePath()).thenReturn(fwAbsolutePath);
    Mockito.when(gruFile.getAbsolutePath()).thenReturn(gruAbsolutePath);

    GruwareData expectedData =
        GruwareData.create(
            AvailableUpdate.create(FW_VERSION, fwAbsolutePath, UpdateType.TYPE_FIRMWARE, FW_CRC),
            AvailableUpdate.create(GRU_VERSION, gruAbsolutePath, UpdateType.TYPE_GRU, GRU_CRC),
            AvailableUpdate.empty(UpdateType.TYPE_BOOTLOADER),
            AvailableUpdate.empty(UpdateType.TYPE_DSP));

    GruwareResponse gruwareResponse = new Gson().fromJson(GRUWARE_RESPONSE, GruwareResponse.class);
    Mockito.when(gruWareManager.getGruwareInfos(MODEL, HW, SERIAL, FW))
        .thenReturn(Single.just(gruwareResponse));

    gruwareRepository.getGruwareInfo(MODEL, HW, SERIAL, FW).test().assertValue(expectedData);
  }

  @Test
  public void testGetGruWareDetailInfo_emptyGru_returnsEmptyGru() throws IOException {
    File fwFile = Mockito.mock(File.class);
    File gruFile = Mockito.mock(File.class);
    Mockito.when(fileDownloader.download(FW_UPDATE_URL, FW_FILENAME)).thenReturn(fwFile);
    Mockito.when(fileDownloader.download(GRU_UPDATE_URL, GRU_FILENAME)).thenReturn(gruFile);

    String fwAbsolutePath = "fwPath";
    String gruAbsolutePath = "greyPath";
    Mockito.when(fwFile.getAbsolutePath()).thenReturn(fwAbsolutePath);
    Mockito.when(gruFile.getAbsolutePath()).thenReturn(gruAbsolutePath);

    GruwareData expectedData =
        GruwareData.create(
            AvailableUpdate.create(FW_VERSION, fwAbsolutePath, UpdateType.TYPE_FIRMWARE, null),
            AvailableUpdate.empty(UpdateType.TYPE_GRU),
            AvailableUpdate.empty(UpdateType.TYPE_BOOTLOADER),
            AvailableUpdate.empty(UpdateType.TYPE_DSP));

    GruwareResponse gruwareResponse =
        new Gson().fromJson(GRUWARE_RESPONSE_EMPTY_GRU, GruwareResponse.class);
    Mockito.when(gruWareManager.getGruwareInfos(MODEL, HW, SERIAL, FW))
        .thenReturn(Single.just(gruwareResponse));

    gruwareRepository.getGruwareInfo(MODEL, HW, SERIAL, FW).test().assertValue(expectedData);
  }

  @Test
  public void
      getGruwareInfo_getGruwareInfosReturnsResponse_observerIsDisposed_createGruwareDataThrowsException_dowsNotThrowUndeliverableException()
          throws IOException {
    Disposable[] disposable = new Disposable[1];

    GruwareResponse gruwareResponse = new Gson().fromJson(GRUWARE_RESPONSE, GruwareResponse.class);
    SingleSubject<GruwareResponse> responseSubject = SingleSubject.create();
    Mockito.when(gruWareManager.getGruwareInfos(MODEL, HW, SERIAL, FW))
        .thenReturn(responseSubject.doOnSuccess(ignore -> disposable[0].dispose()));

    disposable[0] =
        gruwareRepository
            .getGruwareInfo(MODEL, HW, SERIAL, FW)
            .subscribe(ignore -> {}, Throwable::printStackTrace);

    Mockito.doThrow(new IOException("Test forced error"))
        .when(gruwareRepository)
        .createGruwareData(gruwareResponse);

    responseSubject.onSuccess(gruwareResponse);
  }

  @Test
  public void getGruwareInfo_invokesFileDownloaderCancelOnDispose() {
    Mockito.when(gruWareManager.getGruwareInfos(MODEL, HW, SERIAL, FW))
        .thenReturn(SingleSubject.create());

    TestObserver<GruwareData> observer =
        gruwareRepository.getGruwareInfo(MODEL, HW, SERIAL, FW).test();

    Mockito.verify(fileDownloader, Mockito.never()).cancelRequests();

    observer.dispose();

    Mockito.verify(fileDownloader).cancelRequests();
  }

  /*
  parseCrc32
   */

  @Test
  public void parseCrc32_nonNullReturnsLongValue() {
    final Long expectedCrcLong = 10L;
    assertEquals(expectedCrcLong, gruwareRepository.parseCrc32("0A"));
  }

  @Test
  public void parseCrc32_nullReturnsNull() {
    assertNull(gruwareRepository.parseCrc32(null));
  }
}
