package com.kolibree.sdkws.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kolibree.sdkws.data.model.PractitionerToken;
import java.util.ArrayList;

/**
 * Created by aurelien on 17/05/17.
 *
 * <p>Update toothbrush response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class UpdateToothbrushResponse {
  @JsonProperty("bluetooth_id")
  private String bluetoothId;

  @JsonProperty("magn_gain_x")
  private Float magnGainX;

  @JsonProperty("magn_gain_y")
  private Float magnGainY;

  @JsonProperty("magn_gain_z")
  private Float magnGainZ;

  @JsonProperty("profile_id")
  private Long profileId;

  @JsonProperty("sync")
  private String sync;

  @JsonProperty("fw_version")
  private Long fwVersion;

  @JsonProperty("serial")
  private String serial;

  @JsonProperty("magn_offset_x")
  private Integer magnOffsetX;

  @JsonProperty("magn_offset_y")
  private Integer magnOffsetY;

  @JsonProperty("magn_offset_z")
  private Integer magnOffsetZ;

  @JsonProperty("mac_address")
  private String macAddress;

  @JsonProperty("account_id")
  private Long accountId;

  @JsonProperty("tokens")
  private ArrayList<PractitionerToken> tokens;

  @JsonProperty("device_id")
  private String deviceId;

  @JsonProperty("hw_version")
  private Long hardwareVersion;

  @JsonProperty("name")
  private String name;

  @JsonProperty("accel_offset_x")
  private Integer accelOffsetX;

  @JsonProperty("accel_offset_y")
  private Integer accelOffsetY;

  @JsonProperty("accel_offset_z")
  private Integer accelOffsetZ;

  @JsonProperty("OTA_disable")
  private Boolean otaDisable;

  public String getBluetoothId() {
    return bluetoothId;
  }

  public Float getMagnGainX() {
    return magnGainX;
  }

  public Float getMagnGainY() {
    return magnGainY;
  }

  public Float getMagnGainZ() {
    return magnGainZ;
  }

  public Long getProfileId() {
    return profileId;
  }

  public String getSync() {
    return sync;
  }

  public Long getFwVersion() {
    return fwVersion;
  }

  public String getSerial() {
    return serial;
  }

  public Integer getMagnOffsetX() {
    return magnOffsetX;
  }

  public Integer getMagnOffsetY() {
    return magnOffsetY;
  }

  public Integer getMagnOffsetZ() {
    return magnOffsetZ;
  }

  public String getMacAddress() {
    return macAddress;
  }

  public Long getAccountId() {
    return accountId;
  }

  public ArrayList<PractitionerToken> getTokens() {
    return tokens;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public Long getHardwareVersion() {
    return hardwareVersion;
  }

  public String getName() {
    return name;
  }

  public Integer getAccelOffsetX() {
    return accelOffsetX;
  }

  public Integer getAccelOffsetY() {
    return accelOffsetY;
  }

  public Integer getAccelOffsetZ() {
    return accelOffsetZ;
  }

  public Boolean getOtaDisable() {
    return otaDisable;
  }
}
