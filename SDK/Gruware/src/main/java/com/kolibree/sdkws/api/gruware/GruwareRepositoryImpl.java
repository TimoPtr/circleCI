/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.sdkws.api.gruware;

import static com.kolibree.android.commons.UpdateType.TYPE_BOOTLOADER;
import static com.kolibree.android.commons.UpdateType.TYPE_DSP;
import static com.kolibree.android.commons.UpdateType.TYPE_FIRMWARE;
import static com.kolibree.android.commons.UpdateType.TYPE_GRU;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.commons.AvailableUpdate;
import com.kolibree.android.network.utils.FileDownloader;
import com.kolibree.sdkws.api.response.GruwareResponse;
import com.kolibree.sdkws.api.response.GruwareResponse.GruwareBootloaderResponse;
import com.kolibree.sdkws.api.response.GruwareResponse.GruwareDspResponse;
import com.kolibree.sdkws.api.response.GruwareResponse.GruwareFirmwareResponse;
import com.kolibree.sdkws.api.response.GruwareResponse.GruwareGruResponse;
import com.kolibree.sdkws.core.GruwareRepository;
import com.kolibree.sdkws.data.model.GruwareData;
import io.reactivex.Single;
import java.io.File;
import java.io.IOException;
import javax.inject.Inject;

@Keep
public class GruwareRepositoryImpl implements GruwareRepository {

  private final GruwareManager gruwareManager;
  private final FileDownloader fileDownloader;

  @Inject
  public GruwareRepositoryImpl(GruwareManager gruwareManager, FileDownloader fileDownloader) {
    this.gruwareManager = gruwareManager;
    this.fileDownloader = fileDownloader;
  }

  @NonNull
  @Override
  public Single<GruwareData> getGruwareInfo(
      @NonNull String toothbrushModel,
      @NonNull String hardwareVersion,
      @Nullable String serial,
      @NonNull String firmwareVersion) {
    return gruwareManager
        .getGruwareInfos(toothbrushModel, hardwareVersion, serial, firmwareVersion)
        .map(this::createGruwareData)
        .doOnDispose(fileDownloader::cancelRequests);
  }

  @NonNull
  @VisibleForTesting
  GruwareData createGruwareData(final GruwareResponse response) throws IOException {
    /*
    Links expire in 10 minutes, so we need to download it and return the file path
    */
    AvailableUpdate gruUpdate = createGruAvailableUpdate(response.gru());
    AvailableUpdate fwUpdate = createFwAvailableUpdate(response.firmware());
    AvailableUpdate bootloaderUpdate = createBootloaderAvailableUpdate(response.bootloader());
    AvailableUpdate dspUpdate = createDspAvailableUpdate(response.dsp());

    return GruwareData.create(fwUpdate, gruUpdate, bootloaderUpdate, dspUpdate);
  }

  @NonNull
  private AvailableUpdate createFwAvailableUpdate(GruwareFirmwareResponse fwData)
      throws IOException {
    if (fwData.isEmpty()) {
      return AvailableUpdate.empty(TYPE_FIRMWARE);
    }

    File fwFile = fileDownloader.download(fwData.getLink(), fwData.getFilename());

    return AvailableUpdate.create(
        fwData.getFirmwareVersion(), fwFile, TYPE_FIRMWARE, parseCrc32(fwData.getCrc32()));
  }

  @NonNull
  private AvailableUpdate createGruAvailableUpdate(@Nullable GruwareGruResponse gruData)
      throws IOException {
    if (gruData == null || gruData.isEmpty()) {
      return AvailableUpdate.empty(TYPE_GRU);
    }

    File gruFile = fileDownloader.download(gruData.getLink(), gruData.getFilename());

    return AvailableUpdate.create(
        gruData.getDataVersion(), gruFile, TYPE_GRU, parseCrc32(gruData.getCrc32()));
  }

  @NonNull
  private AvailableUpdate createBootloaderAvailableUpdate(
      @Nullable GruwareBootloaderResponse bootloaderData) throws IOException {
    if (bootloaderData == null || bootloaderData.isEmpty()) {
      return AvailableUpdate.empty(TYPE_BOOTLOADER);
    }

    File bootloaderFile =
        fileDownloader.download(bootloaderData.getLink(), bootloaderData.getFilename());

    return AvailableUpdate.createCrcLess(
        bootloaderData.getBootloaderVersion(), bootloaderFile, TYPE_BOOTLOADER);
  }

  @NonNull
  private AvailableUpdate createDspAvailableUpdate(@Nullable GruwareDspResponse dspData)
      throws IOException {
    if (dspData == null || dspData.isEmpty()) {
      return AvailableUpdate.empty(TYPE_DSP);
    }

    File dspFile = fileDownloader.download(dspData.getLink(), dspData.getFilename());

    return AvailableUpdate.createCrcLess(dspData.getDspVersion(), dspFile, TYPE_DSP);
  }

  /*
  The CRC32 value can be null on the backend so we have to handle the case
   */
  @VisibleForTesting
  @Nullable
  Long parseCrc32(@Nullable String crc32) {
    return crc32 == null ? null : Long.parseLong(crc32, 16);
  }
}
