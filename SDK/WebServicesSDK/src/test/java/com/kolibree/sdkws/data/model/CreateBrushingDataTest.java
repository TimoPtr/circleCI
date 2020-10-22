package com.kolibree.sdkws.data.model;

import static com.kolibree.android.commons.BrushingConstantsKt.MIN_BRUSHING_DURATION_SECONDS;
import static com.kolibree.sdkws.data.model.CreateBrushingData.NO_DATA_COVERAGE;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import com.google.gson.JsonParser;
import com.kolibree.android.app.test.CommonBaseTest;
import com.kolibree.android.clock.TrustedClock;
import java.util.UUID;
import org.junit.Test;

/** Created by Kornel on 4/12/2018. */
public class CreateBrushingDataTest extends CommonBaseTest {

  private final String processedData = "{'test' : 42}";

  @Test
  public void valueOf_MIN_VALID_BRUSHING_DURATION_SECONDS_is10() {
    assertEquals(10, MIN_BRUSHING_DURATION_SECONDS);
  }

  @Test
  public void valueOf_NO_DATA_COVERAGE_isNull() {
    assertNull(NO_DATA_COVERAGE);
  }

  @Test
  public void noProcessedData_noCoverage() {
    CreateBrushingData data = createBrushingData();
    assertEquals(NO_DATA_COVERAGE, data.getCoverage());
  }

  @Test
  public void setProcessData_string_getProcessData_return_same() {
    CreateBrushingData brushingData = createBrushingData();
    brushingData.setProcessedData(processedData);

    assertEquals(new JsonParser().parse(processedData).toString(), brushingData.getProcessedData());
  }

  @Test
  public void setProcessData_jsonObject_getProcessData_return_same() {
    CreateBrushingData brushingData = createBrushingData();
    brushingData.setProcessedData(new JsonParser().parse(processedData).getAsJsonObject());

    assertEquals(new JsonParser().parse(processedData).toString(), brushingData.getProcessedData());
  }

  /*
  safeCoverage
   */

  @Test
  public void safeCoverage_returnsNO_DATA_COVERAGE_whenCoverageIsStrictlyNegative() {
    assertEquals(NO_DATA_COVERAGE, createBrushingData().safeCoverage(-1));
  }

  @Test
  public void safeCoverage_returns0_whenCoverageIs0() {
    assertEquals(0, (int) createBrushingData().safeCoverage(0));
  }

  @Test
  public void safeCoverage_returns100_whenCoverageIs100() {
    assertEquals(100, (int) createBrushingData().safeCoverage(100));
  }

  @Test
  public void safeCoverage_returns100_whenCoverageIsOver100() {
    assertEquals(100, (int) createBrushingData().safeCoverage(101));
  }

  @Test
  public void safeCoverage_returnsCoverage_whenCoverageValueIsBetween0And100Included() {
    final int coverage = 87;
    assertEquals(coverage, (int) createBrushingData().safeCoverage(coverage));
  }

  @Test
  public void
      isValidDuration_returnFalse_when_duration_less_than_MIN_VALID_BRUSHING_DURATION_SECONDS() {
    assertFalse(createBrushingData().isDurationValid());
  }

  @Test
  public void
      isValidDuration_returnTrue_when_duration_greater_than_MIN_VALID_BRUSHING_DURATION_SECONDS() {
    assertTrue(
        new CreateBrushingData(
                "game",
                MIN_BRUSHING_DURATION_SECONDS + 1,
                0,
                TrustedClock.getNowOffsetDateTime(),
                0,
                UUID.randomUUID(),
                false)
            .isDurationValid());
  }

  private CreateBrushingData createBrushingData() {
    return new CreateBrushingData(
        "game", 0L, 0, TrustedClock.getNowOffsetDateTime(), 0, UUID.randomUUID(), false);
  }
}
