package com.kolibree.sdkws.data.model;

import androidx.annotation.Keep;
import com.google.gson.JsonObject;

/** Created by mdaniel on 11/11/2015. */
@Keep
public final class BrushPass {

  private static final String FIELD_PASSE_DATETIME = "pass_datetime";
  private static final String FIELD_PASSE_EFFECTIVE_TIME = "effective_time";

  private int pass_datetime;
  private int pass_effective_time;

  public BrushPass(int pass_datetime, int pass_effective_time) {
    this.pass_datetime = pass_datetime;
    this.pass_effective_time = pass_effective_time;
  }

  public int getPass_datetime() {
    return pass_datetime;
  }

  public void setPass_datetime(int pass_datetime) {
    this.pass_datetime = pass_datetime;
  }

  public int getPass_effective_time() {
    return pass_effective_time;
  }

  public void setPass_effective_time(int pass_effective_time) {
    this.pass_effective_time = pass_effective_time;
  }

  public JsonObject toJSON() {
    final JsonObject json = new JsonObject();
    json.addProperty(FIELD_PASSE_DATETIME, pass_datetime);
    json.addProperty(FIELD_PASSE_EFFECTIVE_TIME, pass_effective_time);
    return json;
  }
}
