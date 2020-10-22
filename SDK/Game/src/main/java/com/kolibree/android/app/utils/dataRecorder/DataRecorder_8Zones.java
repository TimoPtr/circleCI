package com.kolibree.android.app.utils.dataRecorder;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.kml.MouthZone8;

/** Created by mdaniel on 15/12/2015. */
@Deprecated
public class DataRecorder_8Zones extends DataRecorder {

  private static final String zones[] = {
    MouthZone8.LoLeExt.name(),
    MouthZone8.LoRiExt.name(),
    MouthZone8.UpLeExt.name(),
    MouthZone8.UpRiExt.name(),
    MouthZone8.LoLeInt.name(),
    MouthZone8.LoRiInt.name(),
    MouthZone8.UpLeInt.name(),
    MouthZone8.UpRiInt.name()
  };

  @Keep
  public DataRecorder_8Zones(int targetBrushingTime) {
    super(targetBrushingTime);
  }

  @NonNull
  @Override
  protected String[] zoneNames() {
    return zones;
  }

  @Override
  int expectedTimeForZone(int zoneId, long goal) {
    if (zoneId < 4) {
      return (int) (goal * 10 / 6);
    }

    return (int) (goal * 10 / 12);
  }
}
