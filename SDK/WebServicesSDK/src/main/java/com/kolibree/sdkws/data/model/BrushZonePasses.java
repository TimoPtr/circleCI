package com.kolibree.sdkws.data.model;

import androidx.annotation.Keep;
import com.google.gson.JsonArray;
import java.util.ArrayList;

/** Created by aurelien on 04/02/16. */
@Keep
public final class BrushZonePasses {

  private String zoneName;
  private ArrayList<BrushPass> passes;
  private int expectedTime;

  public BrushZonePasses(String zoneName, ArrayList<BrushPass> passes, int expectedTime) {
    this.zoneName = zoneName;
    this.passes = passes;
    this.expectedTime = expectedTime;
  }

  public String getZoneName() {
    return zoneName;
  }

  public void setZoneName(String zoneName) {
    this.zoneName = zoneName;
  }

  public ArrayList<BrushPass> getPasses() {
    return passes;
  }

  public void addPass(BrushPass passes) {
    this.passes.add(passes);
  }

  public int getExpectedTime() {
    return expectedTime;
  }

  public void setExpectedTime(int expectedTime) {
    this.expectedTime = expectedTime;
  }

  public JsonArray getPassesAsJsonArray() {
    final JsonArray array = new JsonArray();

    for (BrushPass p : passes) {
      array.add(p.toJSON());
    }
    return array;
  }
}
