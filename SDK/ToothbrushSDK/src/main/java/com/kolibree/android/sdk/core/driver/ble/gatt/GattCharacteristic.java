package com.kolibree.android.sdk.core.driver.ble.gatt;

import androidx.annotation.NonNull;
import java.util.UUID;

/**
 * Created by aurelien on 15/11/16.
 *
 * <p>Kolibree GATT characteristics definition
 */
public enum GattCharacteristic {
  /** Device parameters characteristic */
  DEVICE_PARAMETERS(GattService.DEVICE, "02"),

  /** Hardware, firmware versions and bootloader state characteristic */
  DEVICE_VERSIONS(GattService.DEVICE, "01"),

  /** Current connection interval. Only present in bootloader */
  CONNECTION_INTERVAL(GattService.DEVICE, "03"),

  /** Sensors calibration data and GRU info */
  SENSORS_INFO(GattService.SENSORS, "06"),

  /** Enable or disable movement detectors and sensors output */
  SENSORS_STREAMING_CONTROL(GattService.SENSORS, "04"),

  /** Movement detectors stream updates on this characteristic (notify only) */
  SENSORS_DETECTIONS(GattService.SENSORS, "03"),

  /** Sensors raw data (notify) */
  SENSOR_RAW_DATA(GattService.SENSORS, "01"),

  /** Gravity sensor (quaternion, notify) */
  SENSOR_FUSION_DATA(GattService.SENSORS, "02"),

  /** Brushing record count */
  BRUSHING_RECORDS_STATUS(GattService.BRUSHING, "01"),

  /** Brushing records data are streamed over this char (notify only) */
  BRUSHING_RECORD_IND(GattService.BRUSHING, "02"),

  /** Brushing operations write command are done on this characteristic */
  BRUSHING_POP_RECORD(GattService.BRUSHING, "03"),

  /** OTA update status */
  OTA_UPDATE_STATUS_NOTIFICATION(GattService.OTA_UPDATE, "01"),

  /** OTA control */
  OTA_UPDATE_START(GattService.OTA_UPDATE, "02"),

  /** Write firmware image file */
  OTA_UPDATE_WRITE_CHUNK(GattService.OTA_UPDATE, "03"),

  /** Tell the toothbrush the update process is finished */
  OTA_UPDATE_VALIDATE(GattService.OTA_UPDATE, "04"),

  /** OTA control */
  FILES_COMMAND_CHAR(GattService.FILES, "01"),

  /** Write firmware image file */
  FILES_DATA_CHAR(GattService.FILES, "02"),

  /** Timestamped plaqless raw data. NOTIFY only */
  PLAQLESS_DETECTOR_CHAR(GattService.PLAQLESS, "01"),

  /** Timestamped kolibree raw data. NOTIFY only */
  PLAQLESS_IMU_CHAR(GattService.PLAQLESS, "02"),

  /** Register to Plaqless and of Kolibree raw data. WRITE / NOTIFY */
  PLAQLESS_CONTROL_CHAR(GattService.PLAQLESS, "03");

  /** SENSORS_INFO GRU data set */
  public static final byte SENSORS_INFO_GRU_DATA_SET = 0x04;

  /** Serial number command ID */
  public static final byte DEVICE_PARAMETERS_SERIAL_NUMBER = 0x30;

  /** Current time command ID */
  public static final byte DEVICE_PARAMETERS_CURRENT_TIME = 0x33;

  /** Mode LEDs pattern */
  public static final byte DEVICE_PARAMETERS_MODE_LED_PATTERN = 0x19;

  /** Button events */
  public static final byte DEVICE_PARAMETERS_BUTTON_EVENTS = 0x20;

  /** Brushing events */
  public static final byte DEVICE_PARAMETERS_BRUSHING_EVENTS = 0x21;

  /** BLE events */
  public static final byte DEVICE_PARAMETERS_BLE_EVENTS = 0x23;

  /** Mark current brushing as monitored */
  public static final byte DEVICE_PARAMETERS_MONITOR_CURRENT_BRUSHING = 0x13;

  /** Single mode user ID */
  public static final byte DEVICE_PARAMETERS_MULTI_USER_MODE = 0x31;

  /** Single mode user ID */
  public static final byte DEVICE_PARAMETERS_USER_ID = 0x32;

  /** Default brushing duration */
  public static final byte DEVICE_PARAMETERS_DEFAULT_BRUSHING_DURATION = 0x35;

  /** Auto shutdown timeout */
  public static final byte DEVICE_PARAMETERS_AUTO_SHUTDOWN_TIMEOUT = 0x34;

  /** Vibration signals and pattern */
  public static final byte DEVICE_PARAMETERS_VIBRATION_SIGNALS = 0x3E;

  /** Owner device ID */
  public static final byte DEVICE_PARAMETERS_OWNER_DEVICE = 0x3F;

  /** Device bluetooth TTL events */
  public static final byte DEVICE_TTL_EVENTS = 0x24;

  /** Enable / disable vibrator */
  public static final byte DEVICE_PARAMETERS_VIBRATION = 0x11;

  /** Play LED pattern */
  public static final byte DEVICE_PARAMETERS_LED_PATTERN = 0x15;

  /** GRU data set version and validity */
  public static final byte DEVICE_PARAMETERS_GRU_DATA_SET_INFO = 0x44;

  /** Raw data sensors sensitivities */
  public static final byte DEVICE_PARAMETERS_SENSOR_SENSITIVITIES = 0x43;

  /** Magnetometer calibration data */
  public static final byte DEVICE_PARAMETERS_MAGNETOMETER_CALIBRATION = 0x42;

  /**
   * Calibrate Accelerometer and Gyrometer
   *
   * <p>Only M1
   */
  public static final byte DEVICE_PARAMETERS_ACCELEROMETER_OFFSETS = 0x47;

  /**
   * Calibrate Accelerometer and Gyrometer
   *
   * <p>Only M1
   */
  public static final byte DEVICE_PARAMETERS_GYROMETER_OFFSETS = 0x48;

  public static final byte DEVICE_PARAMETERS_BOOTLOADER_VERSION = 0x4D;

  public static final byte DEVICE_PARAMETERS_PLAQLESS_RING_LED_STATE = 0x4E;

  public static final byte DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS = 0x4F;

  public static final byte DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_STATE = 0x26;

  public static final byte DEVICE_PARAMETERS_PUSH_DSP = 0x51;

  public static final byte DEVICE_PARAMETERS_CUSTOM_BRUSHING_MODE_SETTINGS = 0x56;

  public static final byte DEVICE_PARAMETERS_SPECIAL_LED_CONTROL = 0x55;

  public static final byte DEVICE_PARAMETERS_BRUSHING_MODE_SEQUENCE = 0x57;

  public static final byte DEVICE_PARAMETERS_BRUSHING_MODE_PATTERN = 0x58;

  public static final byte DEVICE_PARAMETERS_BRUSHING_MODE_CURVE = 0x59;

  public static final byte DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_CONTROL = 0x60;

  public static final byte DEVICE_PARAMETERS_PICKUP_DETECTION_CONTROL = 0x61;

  /** The GATT service this characteristic belongs to */
  public final GattService SERVICE;

  /** GATT UUID */
  public final UUID UUID;

  GattCharacteristic(GattService service, String identifier) {
    SERVICE = service;
    UUID =
        java.util.UUID.fromString(
            identifier + "0000" + service.IDENTIFIER + "-55d0-4989-b640-cfb64e5c34e0");
  }

  /**
   * Get a {@link GattCharacteristic} from its UUID
   *
   * @param uuid non null UUID string
   * @return non null {@link GattCharacteristic}
   */
  public static final @NonNull GattCharacteristic lookUp(@NonNull UUID uuid) {
    for (GattCharacteristic c : values()) {
      if (c.UUID.equals(uuid)) {
        return c;
      }
    }

    throw new IllegalArgumentException("Unknown Kolibree characteristic UUID : " + uuid);
  }

  @Override
  public String toString() {
    return "GattCharacteristic{" + "SERVICE=" + SERVICE + ", UUID=" + UUID + '}';
  }
}
