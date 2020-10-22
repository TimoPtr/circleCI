package com.kolibree.android.sdk.connection.detectors.listener;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.detectors.data.WeightedMouthZone;
import java.util.List;

/**
 * Created by aurelien on 10/08/17.
 *
 * <p>RNN movement detector output listener
 */
@Keep
public interface RNNDetectorListener {

  /**
   * Called on every new RNN movement detector packet
   *
   * <p>The output ArrayList's length is always 6
   *
   * @param source non null KLTBConnection event source
   * @param data non null WeightedMouthZone list
   */
  void onRNNData(@NonNull KLTBConnection source, @NonNull List<WeightedMouthZone> data);
}
