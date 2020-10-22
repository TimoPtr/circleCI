package com.kolibree.android.app.utils.dataRecorder;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.kml.MouthZone12;

/** Created by mdaniel on 15/12/2015. */
@Deprecated
public class DataRecorder_12Zones extends DataRecorder {

  private static final String zones[] = {
    MouthZone12.UpMolLeExt12.name(),
    MouthZone12.UpMolLeInt12.name(),
    MouthZone12.UpIncExt12.name(),
    MouthZone12.UpIncInt12.name(),
    MouthZone12.UpMolRiExt12.name(),
    MouthZone12.UpMolRiInt12.name(),
    MouthZone12.LoMolRiExt12.name(),
    MouthZone12.LoMolRiInt12.name(),
    MouthZone12.LoIncExt12.name(),
    MouthZone12.LoIncInt12.name(),
    MouthZone12.LoMolLeExt12.name(),
    MouthZone12.LoMolLeInt12.name()
  };

  @Keep
  public DataRecorder_12Zones(int brushingDuration) {
    super(brushingDuration);

    for (int i = 0; i < zones.length; i++) {
      zones[i] = MouthZone12.values()[i].toString();
    }
  }

  @NonNull
  @Override
  protected String[] zoneNames() {
    return zones;
  }

  @Override
  int expectedTimeForZone(int zoneId, long goal) {
    switch (zoneId) {
      case 0:
      case 1:
      case 6:
      case 7:
        return (int) ((goal * 10 / (12 * 2 / 3)));
      case 2:
      case 3:
      case 4:
      case 5:
      case 8:
      case 9:
      case 10:
      case 11:
        return (int) ((goal * 10 / (12 * 4 / 3)));
    }
    return 1;
  }
}
