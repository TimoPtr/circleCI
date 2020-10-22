/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.api.response;

import static com.kolibree.sdkws.api.response.GruwareResponse.GruwareBootloaderResponse.FIELD_BOOTLOADER;
import static com.kolibree.sdkws.api.response.GruwareResponse.GruwareDspResponse.FIELD_DSP;
import static com.kolibree.sdkws.api.response.GruwareResponse.GruwareFirmwareResponse.FIELD_FW;
import static com.kolibree.sdkws.api.response.GruwareResponse.GruwareGruResponse.FIELD_GRU;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import com.kolibree.android.extensions.StringExtensionsKt;
import java.util.Objects;

/** Gruware response. Unified information with FW and GRU for current TB and model */
@Keep
public class GruwareResponse {

  @SerializedName(FIELD_GRU)
  @Nullable
  private GruwareGruResponse gruData;

  @SerializedName(FIELD_FW)
  private GruwareFirmwareResponse fwData;

  @SerializedName(FIELD_BOOTLOADER)
  @Nullable
  private GruwareBootloaderResponse bootloaderData;

  @SerializedName(FIELD_DSP)
  @Nullable
  private GruwareDspResponse dspData;

  @Nullable
  public GruwareDspResponse dsp() {
    return dspData;
  }

  @Nullable
  public GruwareBootloaderResponse bootloader() {
    return bootloaderData;
  }

  @NonNull
  public GruwareFirmwareResponse firmware() {
    return fwData;
  }

  @Nullable
  public GruwareGruResponse gru() {
    return gruData;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GruwareResponse that = (GruwareResponse) o;
    return Objects.equals(gruData, that.gruData)
        && Objects.equals(fwData, that.fwData)
        && Objects.equals(bootloaderData, that.bootloaderData)
        && Objects.equals(dspData, that.dspData);
  }

  @Override
  public int hashCode() {

    return Objects.hash(gruData, fwData, bootloaderData, dspData);
  }

  private abstract static class GruwareMemberResponse {

    private static final String FIELD_LINK = "link";
    private static final String FIELD_CRC32 = "crc32";
    private static final String FIELD_CRC16 = "crc16";
    private static final String FIELD_FILENAME = "filename";
    private static final String FIELD_BETA = "beta";

    @SerializedName(FIELD_LINK)
    private String link;

    @SerializedName(FIELD_CRC32)
    @Nullable
    private String crc32;

    @SerializedName(FIELD_CRC16)
    @Nullable
    private String crc16;

    @SerializedName(FIELD_FILENAME)
    private String filename;

    @SerializedName(FIELD_BETA)
    private boolean beta;

    @NonNull
    public String getLink() {
      return link;
    }

    @NonNull
    public String getFilename() {
      return filename;
    }

    @Nullable
    public String getCrc32() {
      return StringExtensionsKt.isNullOrNullValue(crc32) ? null : crc32;
    }

    @Nullable
    public String getCrc16() {
      return crc16;
    }

    public boolean isEmpty() {
      return link == null || link.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof GruwareMemberResponse)) {
        return false;
      }
      GruwareMemberResponse that = (GruwareMemberResponse) o;
      return beta == that.beta
          && Objects.equals(link, that.link)
          && Objects.equals(crc32, that.crc32)
          && Objects.equals(crc16, that.crc16)
          && Objects.equals(filename, that.filename);
    }

    @Override
    public int hashCode() {

      return Objects.hash(link, crc32, crc16, filename, beta);
    }
  }

  @androidx.annotation.Keep
  public static final class GruwareFirmwareResponse extends GruwareMemberResponse {

    static final String FIELD_FW = "fw";

    @SerializedName(FIELD_FW)
    private String firmwareVersion;

    @NonNull
    public String getFirmwareVersion() {
      return firmwareVersion;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof GruwareFirmwareResponse)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      GruwareFirmwareResponse that = (GruwareFirmwareResponse) o;
      return Objects.equals(firmwareVersion, that.firmwareVersion);
    }

    @Override
    public int hashCode() {

      return Objects.hash(super.hashCode(), firmwareVersion);
    }
  }

  @androidx.annotation.Keep
  public static final class GruwareGruResponse extends GruwareMemberResponse {

    static final String FIELD_GRU = "gru";

    @SerializedName(FIELD_GRU)
    private String dataVersion;

    @NonNull
    public String getDataVersion() {
      return dataVersion;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof GruwareGruResponse)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      GruwareGruResponse that = (GruwareGruResponse) o;
      return Objects.equals(dataVersion, that.dataVersion);
    }

    @Override
    public int hashCode() {

      return Objects.hash(super.hashCode(), dataVersion);
    }
  }

  @androidx.annotation.Keep
  public static final class GruwareBootloaderResponse extends GruwareMemberResponse {

    static final String FIELD_BOOTLOADER = "bootloader";

    @SerializedName(FIELD_BOOTLOADER)
    private String bootloaderVersion;

    @NonNull
    public String getBootloaderVersion() {
      return bootloaderVersion;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof GruwareBootloaderResponse)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      GruwareBootloaderResponse that = (GruwareBootloaderResponse) o;
      return Objects.equals(bootloaderVersion, that.bootloaderVersion);
    }

    @Override
    public int hashCode() {

      return Objects.hash(super.hashCode(), bootloaderVersion);
    }
  }

  @androidx.annotation.Keep
  public static final class GruwareDspResponse extends GruwareMemberResponse {

    static final String FIELD_DSP = "dsp";

    @SerializedName(FIELD_DSP)
    private String dspVersion;

    @NonNull
    public String getDspVersion() {
      return dspVersion;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof GruwareDspResponse)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      GruwareDspResponse that = (GruwareDspResponse) o;
      return Objects.equals(dspVersion, that.dspVersion);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), dspVersion);
    }
  }
}
