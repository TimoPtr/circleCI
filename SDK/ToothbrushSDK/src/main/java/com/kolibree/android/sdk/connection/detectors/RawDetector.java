package com.kolibree.android.sdk.connection.detectors;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.detectors.listener.RawDetectorListener;

/**
 * Created by aurelien on 24/08/17.
 *
 * <p>Raw sensor data detector
 */
@Keep
public interface RawDetector {

  /**
   * Register a listener to get notified on toothbrush detector data changes
   *
   * @param listener non null RawDetectorListener implementation
   */
  void register(@NonNull RawDetectorListener listener);

  /**
   * Unregister from detector events
   *
   * @param listener non null RawDetectorListener implementation
   */
  void unregister(@NonNull RawDetectorListener listener);
}
