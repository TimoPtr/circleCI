package com.kolibree.android.sdk.connection.detectors.listener;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.kml.MouthZone16;
import java.util.List;

/**
 * Created by aurelien on 10/08/17.
 *
 * <p>SVM movement detector output listener
 */
@Keep
public interface SVMDetectorListener {

  /**
   * Called on every new RNN movement detector packet
   *
   * @param source non null KLTBConnection event source
   * @param data non null MouthZone16 list
   */
  void onSVMData(@NonNull KLTBConnection source, @NonNull List<MouthZone16> data);
}
