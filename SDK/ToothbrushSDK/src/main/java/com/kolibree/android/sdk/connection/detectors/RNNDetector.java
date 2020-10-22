package com.kolibree.android.sdk.connection.detectors;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.detectors.listener.RNNDetectorListener;
import com.kolibree.android.sdk.version.SoftwareVersion;

/**
 * Created by aurelien on 10/08/17.
 *
 * <p>RNN detector
 */
@Keep
public interface RNNDetector {

  /**
   * Register a listener to get notified on toothbrush detector data changes
   *
   * @param listener non null RNNDetectorListener implementation
   */
  void register(@NonNull RNNDetectorListener listener);

  /**
   * Unregister from detector events
   *
   * @param listener non null RNNDetectorListener implementation
   */
  void unregister(@NonNull RNNDetectorListener listener);

  /**
   * Check if the toothbrush firmware has valid GRU data for RNN detector
   *
   * @return true if the GRU data is valid, false otherwise
   */
  boolean hasValidGruData();

  /**
   * Get Gru data version
   *
   * @return non null GRU data SoftwareVersion (Major.minor.revision)
   */
  @NonNull
  SoftwareVersion getGruDataVersion();
}
