package com.kolibree.sdkws.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Created by miguelaragues on 24/8/17. */
public class InstructionsResponse {

  @JsonProperty("text_list")
  private List<String> texts;

  @JsonProperty("total_time")
  private long totalDuration;

  @JsonProperty("end_times")
  private List<Integer> times;

  public List<String> getTexts() {
    return texts;
  }

  public long getTotalDuration() {
    return totalDuration;
  }

  public List<Integer> getTimes() {
    return times;
  }
}
