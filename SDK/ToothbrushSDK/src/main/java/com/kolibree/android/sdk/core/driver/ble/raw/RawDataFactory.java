package com.kolibree.android.sdk.core.driver.ble.raw;

import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState;
import com.kolibree.android.sdk.core.binary.PayloadReader;
import com.kolibree.android.sdk.math.Matrix;
import com.kolibree.android.sdk.math.Vector;
import java.util.concurrent.atomic.AtomicReference;
import timber.log.Timber;

/**
 * Created by aurelien on 20/07/17.
 *
 * <p>Ara toothbrush raw data aggregator Applies conversions and offsets to the toothbrush's sensors
 * values
 */
public class RawDataFactory {

  private final RawDataFactoryCallback callback;

  // Sensor sensitivities
  private float accelerometerSensitivity;
  private float gyroscopeSensitivity;
  private float magnetometerSensitivity;

  // Magnetometer rotation matrix
  private final AtomicReference<Matrix> magnetometerRotation = new AtomicReference<>();

  // Magnetometer offsets
  private Vector magnetometerOffset = new Vector(0f, 0f, 0f);
  private Vector accelerometerOffset = new Vector(0f, 0f, 0f);
  private Vector gyroscopeOffset = new Vector(0f, 0f, 0f);

  public RawDataFactory(@NonNull RawDataFactoryCallback callback) {
    this.callback = callback;
  }

  /**
   * Set sensors sensitivities
   *
   * <p>These values will be used to calibrate the raw values
   *
   * @param accelerometer accelerometer sensitivity
   * @param gyroscope gyroscope sensitivity
   * @param magnetometer magnetometer sensitivity
   */
  public void setSensitivities(float accelerometer, float gyroscope, float magnetometer) {
    accelerometerSensitivity = accelerometer;
    gyroscopeSensitivity = gyroscope;
    magnetometerSensitivity = magnetometer;
  }

  /**
   * Set the toothbrush's magnetometer rotation matrix
   *
   * <p>This matrix will be used to compute raw data output
   *
   * @param rotationMatrix non null 3*3 Matrix
   * @param offset non null offset Vector
   */
  public void setMagnetometerCalibration(@NonNull Matrix rotationMatrix, @NonNull Vector offset) {
    magnetometerRotation.set(rotationMatrix);
    magnetometerOffset = offset;
  }

  /**
   * Set the toothbrush's accelerometer offset vector
   *
   * <p>This matrix will be used to compute raw data output
   *
   * @param offset non null offset Vector
   */
  public void setAccelerometerOffset(@NonNull Vector offset) {
    accelerometerOffset = offset;
  }

  /**
   * Set the toothbrush's gyroscope offset vector
   *
   * <p>This matrix will be used to compute raw data output
   *
   * @param offset non null offset Vector
   */
  public void setGyroscopeOffset(@NonNull Vector offset) {
    gyroscopeOffset = offset;
  }

  /**
   * Raw data packet handling
   *
   * @param payload raw data packet
   */
  public void onRawDataPacket(@NonNull byte[] payload) {
    final Matrix rotationMatrix = magnetometerRotation.get();

    /*
    This test fixes a bug on the Vivo Y79, for some dark reason the magnetometer rotation matrix is
    nullified. The fix has been validated by Yann
     */
    if (rotationMatrix == null) {
      Timber.w("Magnetometer rotation matrix is null, ignoring raw data packet!");
      return;
    }

    final PayloadReader reader = new PayloadReader(payload);
    final float timestamp = (float) reader.readUnsignedInt16() / 50; // 50 Hz default value

    // Accelerometer vector (no offset anymore)
    final Vector accelerationVector =
        new Vector(reader.readInt16(), reader.readInt16(), reader.readInt16())
            .scalar(accelerometerSensitivity)
            .subtract(accelerometerOffset);

    // Gyroscope vector
    final Vector gyroscopeVector =
        new Vector(reader.readInt16(), reader.readInt16(), reader.readInt16())
            .scalar(gyroscopeSensitivity)
            .subtract(gyroscopeOffset);

    // Magnetometer vector
    final Vector magnetometerVector =
        new Vector(reader.readInt16(), reader.readInt16(), reader.readInt16())
            .scalar(magnetometerSensitivity)
            .subtract(magnetometerOffset)
            .product(rotationMatrix);

    callback.onSensorState(
        new RawSensorState(timestamp, accelerationVector, gyroscopeVector, magnetometerVector));
  }

  public interface RawDataFactoryCallback {

    /**
     * Called when a new raw data packet has been parsed
     *
     * @param state non null RawSensorState
     */
    void onSensorState(@NonNull RawSensorState state);
  }
}
