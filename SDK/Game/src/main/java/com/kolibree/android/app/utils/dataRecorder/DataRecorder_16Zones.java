package com.kolibree.android.app.utils.dataRecorder;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.kml.MouthZone16;

/** Created by mdaniel on 15/12/2015. */
@Deprecated
public class DataRecorder_16Zones extends DataRecorder {

  private static final String zones[] = {
    MouthZone16.UpMolLeExt.name(),
    MouthZone16.LoMolLeExt.name(),
    MouthZone16.UpMolRiExt.name(),
    MouthZone16.LoMolRiExt.name(),
    MouthZone16.UpIncExt.name(),
    MouthZone16.LoIncExt.name(),
    MouthZone16.UpMolLeOcc.name(),
    MouthZone16.UpMolLeInt.name(),
    MouthZone16.LoMolLeInt.name(),
    MouthZone16.LoMolLeOcc.name(),
    MouthZone16.UpMolRiOcc.name(),
    MouthZone16.UpMolRiInt.name(),
    MouthZone16.LoMolRiInt.name(),
    MouthZone16.LoMolRiOcc.name(),
    MouthZone16.UpIncInt.name(),
    MouthZone16.LoIncInt.name()
  };

  @Keep
  public DataRecorder_16Zones(int brushingDuration) {
    super(brushingDuration);
  }

  @NonNull
  @Override
  protected String[] zoneNames() {
    return zones;
  }

  @Override
  int expectedTimeForZone(int zoneId, long goal) {
    return (int) ((goal * 10 / zones.length));
  }
}
