package com.kolibree.sdkws.api.response;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonProperty;

/** GRU data update response */
public class CheckGRUDataResponse {

  private static final String FIELD_LINK = "link";
  private static final String FIELD_GRU = "fw";
  private static final String FIELD_CRC32 = "crc32";
  private static final String FIELD_CRC16 = "crc16";
  private static final String FIELD_FILENAME = "filename";

  @JsonProperty("link")
  private String link;

  @JsonProperty("gru")
  private String dataVersion;

  @JsonProperty("crc32")
  private String crc32;

  @JsonProperty("crc16")
  private String crc16;

  @NonNull
  public String getLink() {
    return link;
  }

  @NonNull
  public String getDataVersion() {
    return dataVersion;
  }

  @NonNull
  public String getCrc32() {
    return crc32;
  }
}
