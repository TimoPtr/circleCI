package com.kolibree.android.sdk.connection.detectors.listener;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState;

/**
 * Created by aurelien on 10/08/17.
 *
 * <p>Raw sensor data output listener
 */
@Keep
public interface RawDetectorListener {

  /**
   * Called on every new rawData
   *
   * @param source non null KLTBConnection event source
   * @param sensorState non null RawSensorState
   */
  void onRawData(@NonNull KLTBConnection source, @NonNull RawSensorState sensorState);
}
