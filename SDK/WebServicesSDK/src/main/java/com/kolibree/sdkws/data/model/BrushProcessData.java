package com.kolibree.sdkws.data.model;

import androidx.annotation.Keep;
import java.util.ArrayList;

/** Created by mdaniel on 04/02/2016. */
@Keep
public class BrushProcessData {

  private final ArrayList<BrushZonePasses> zonepasses;

  public BrushProcessData() {
    zonepasses = new ArrayList<>(16);
  }

  public void addZonePass(BrushZonePasses zonePass) {
    synchronized (zonepasses) {
      if (!containsBrushZone(zonePass.getZoneName())) {
        this.zonepasses.add(zonePass);
      }
    }
  }

  public boolean containsBrushZone(String zoneName) {
    synchronized (zonepasses) {
      if (!zonepasses.isEmpty()) {
        for (BrushZonePasses zone : zonepasses) {
          if (zone.getZoneName().equals(zoneName)) {
            return true;
          }
        }
      }
      return false;
    }
  }

  public BrushZonePasses getZonePass(String zoneName) {
    synchronized (zonepasses) {
      if (!zonepasses.isEmpty()) {
        for (BrushZonePasses zone : zonepasses) {
          if (zone.getZoneName().equals(zoneName)) {
            return zone;
          }
        }
      }
      return null;
    }
  }

  public ArrayList<BrushZonePasses> getZonepasses() {
    synchronized (zonepasses) {
      return zonepasses;
    }
  }
}
