package com.kolibree.sdkws.data.model;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kolibree.android.commons.models.StrippedMac;
import com.kolibree.sdkws.data.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

/** Created by aurelien on 08/03/16. */
@Keep
public class UpdateToothbrushData implements JSONModel {
  private static final String FIELD_PROFILE_ID = "profile_id";
  private static final String FIELD_FW_VERSION = "fw_version";
  private static final String FIELD_GRU_VERSION = "gru_version";
  private static final String FIELD_SERIAL = "serial";
  private static final String FIELD_HW_VERSION = "hw_version";
  private static final String FIELD_MAGN_OFFSET_X = "magn_offset_x";
  private static final String FIELD_MAGN_OFFSET_Y = "magn_offset_y";
  private static final String FIELD_MAGN_OFFSET_Z = "magn_offset_z";
  private static final String FIELD_MAGN_GAIN_X = "magn_gain_x";
  private static final String FIELD_MAGN_GAIN_Y = "magn_gain_y";
  private static final String FIELD_MAGN_GAIN_Z = "magn_gain_z";
  private static final String FIELD_ACCEL_OFFSET_X = "accel_offset_x";
  private static final String FIELD_ACCEL_OFFSET_Y = "accel_offset_y";
  private static final String FIELD_ACCEL_OFFSET_Z = "accel_offset_z";
  private static final String FIELD_MAC_ADDRESS = "mac_address";
  private static final String FIELD_DEVICE_ID = "device_id";

  private static final long NOT_AVAILABLE = -1L;

  private long profileId = -1;
  private long fwVersion = -1;
  private long gruVersion = -1;
  private String serial;
  private long hwVersion = -1;
  private int magnOffsetX = -1;
  private int magnOffsetY = -1;
  private int magnOffsetZ = -1;
  private float magnGainX = -1f;
  private float magnGainY = -1f;
  private float magnGainZ = -1f;
  private int accelOffsetX = -1;
  private int accelOffsetY = -1;
  private int accelOffsetZ = -1;
  private String macAddress;
  private String deviceId;

  public UpdateToothbrushData(
      @Nullable String serial, @NonNull String macAddress, @NonNull String deviceId) {
    this.serial = serial;
    this.macAddress = StrippedMac.fromMac(macAddress).getValue();
    this.deviceId = deviceId;
  }

  public void setHardwareData(
      int magnOffsetX,
      int magnOffsetY,
      int magnOffsetZ,
      float magnGainX,
      float magnGainY,
      float magnGainZ,
      int accelOffsetX,
      int accelOffsetY,
      int accelOffsetZ) {
    this.magnOffsetX = magnOffsetX;
    this.magnOffsetY = magnOffsetY;
    this.magnOffsetZ = magnOffsetZ;
    this.magnGainX = magnGainX;
    this.magnGainY = magnGainY;
    this.magnGainZ = magnGainZ;
    this.accelOffsetX = accelOffsetX;
    this.accelOffsetY = accelOffsetY;
    this.accelOffsetZ = accelOffsetZ;
  }

  public void setFwVersion(long fwVersion) {
    this.fwVersion = fwVersion;
  }

  public void setGruVersion(@Nullable Long gruVersion) {
    if (gruVersion != null) {
      this.gruVersion = gruVersion;
    } else {
      this.gruVersion = NOT_AVAILABLE;
    }
  }

  public void setHwVersion(long hwVersion) {
    this.hwVersion = hwVersion;
  }

  public void setProfileId(long profileId) {
    this.profileId = profileId;
  }

  @Override
  public @NonNull String toJsonString() throws JSONException {
    final JSONObject json = new JSONObject();

    if (profileId != -1) json.put(FIELD_PROFILE_ID, profileId);
    if (fwVersion != -1) json.put(FIELD_FW_VERSION, fwVersion);
    if (gruVersion != -1) json.put(FIELD_GRU_VERSION, gruVersion);
    if (serial != null) json.put(FIELD_SERIAL, serial);
    if (hwVersion != -1) json.put(FIELD_HW_VERSION, hwVersion);
    if (magnOffsetX != -1) json.put(FIELD_MAGN_OFFSET_X, magnOffsetX);
    if (magnOffsetY != -1) json.put(FIELD_MAGN_OFFSET_Y, magnOffsetY);
    if (magnOffsetZ != -1) json.put(FIELD_MAGN_OFFSET_Z, magnOffsetZ);
    if (magnGainX != -1f) json.put(FIELD_MAGN_GAIN_X, magnGainX);
    if (magnGainY != -1f) json.put(FIELD_MAGN_GAIN_Y, magnGainY);
    if (magnGainZ != -1f) json.put(FIELD_MAGN_GAIN_Z, magnGainZ);
    if (accelOffsetX != -1) json.put(FIELD_ACCEL_OFFSET_X, accelOffsetX);
    if (accelOffsetY != -1) json.put(FIELD_ACCEL_OFFSET_Y, accelOffsetY);
    if (accelOffsetZ != -1) json.put(FIELD_ACCEL_OFFSET_Z, accelOffsetZ);
    json.put(FIELD_MAC_ADDRESS, macAddress);
    json.put(FIELD_DEVICE_ID, deviceId);

    return json.toString();
  }

  public long getProfileId() {
    return profileId;
  }

  public long getFwVersion() {
    return fwVersion;
  }

  public long getGruVersion() {
    return gruVersion;
  }

  public String getSerial() {
    return serial;
  }

  public long getHwVersion() {
    return hwVersion;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getMacAddress() {
    return macAddress;
  }
}
