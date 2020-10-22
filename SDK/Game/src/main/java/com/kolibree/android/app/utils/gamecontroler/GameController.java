package com.kolibree.android.app.utils.gamecontroler;

import static com.kolibree.android.commons.GameApiConstants.GAME_GO_PIRATE;

import androidx.annotation.Keep;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.app.utils.dataRecorder.DataRecorder;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.processedbrushings.CheckupCalculator;
import com.kolibree.android.processedbrushings.CheckupData;
import com.kolibree.kml.MouthZone16;
import com.kolibree.sdkws.data.model.CreateBrushingData;
import java.util.List;
import org.threeten.bp.Duration;
import org.threeten.bp.OffsetDateTime;

/** Created by mdaniel on 03/11/2015. */
@Keep
@Deprecated
public abstract class GameController {

  protected final CheckupCalculator checkupCalculator;
  protected final boolean isRightHand;

  @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
  public DataRecorder dataRecorder;

  // Result infos
  protected OffsetDateTime brushdate;
  protected int quality = -1;

  @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
  public int gold = 0;

  protected int time;

  public GameController(boolean isRightHand, CheckupCalculator checkupCalculator) {
    this.isRightHand = isRightHand;
    this.checkupCalculator = checkupCalculator;
  }

  public abstract void init(int targetBrushingTime);

  public abstract void shouldChangeLane();

  public abstract void setPrescribedZoneId(int prescribedZoneId);

  public abstract void setCurrentPossibleMouthZones(List<MouthZone16> mouthZone);

  public abstract void run();

  public abstract void pause();

  public abstract void stop();

  public CreateBrushingData getBrushingData(int targetBrushingTime) {
    brushdate = TrustedClock.getNowOffsetDateTime();
    Duration duration = Duration.ofSeconds(time);
    final CreateBrushingData data =
        new CreateBrushingData(
            GAME_GO_PIRATE, duration, targetBrushingTime, brushdate, gold, false);
    data.setProcessedData(dataRecorder.getProcessData());
    CheckupData checkupData =
        checkupCalculator.calculateCheckup(
            data.getProcessedData(), brushdate.toEpochSecond(), duration);
    data.setCoverage(checkupData.getSurfacePercentage());
    return data;
  }

  public abstract int getQuality(int targetBrushingTime);

  public abstract void addGoldEarned(int gold);

  public abstract void setCompleteTime(int time);
}
