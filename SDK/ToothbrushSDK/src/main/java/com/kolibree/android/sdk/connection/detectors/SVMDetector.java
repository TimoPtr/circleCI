package com.kolibree.android.sdk.connection.detectors;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.detectors.listener.SVMDetectorListener;

/**
 * Created by aurelien on 24/08/17.
 *
 * <p>Most Probable Mouth Zones detector
 */
@Keep
public interface SVMDetector {

  /**
   * Register a listener to get notified on toothbrush detector data changes
   *
   * @param listener non null SVMDetectorListener implementation
   */
  void register(@NonNull SVMDetectorListener listener);

  /**
   * Unregister from detector events
   *
   * @param listener non null SVMDetectorListener implementation
   */
  void unregister(@NonNull SVMDetectorListener listener);
}
