package com.kolibree.android.offlinebrushings;

import static com.kolibree.android.commons.ApiConstants.DATETIME_FORMATTER;
import static com.kolibree.android.test.mocks.OfflineBrushingKt.createOfflineBrushing;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import com.kolibree.android.accountinternal.profile.models.Profile;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.processedbrushings.CheckupCalculator;
import com.kolibree.android.processedbrushings.LegacyProcessedBrushingFactory;
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing;
import com.kolibree.android.utils.KolibreeAppVersions;
import com.kolibree.sdkws.core.IKolibreeConnector;
import com.kolibree.sdkws.data.model.CreateBrushingData;
import org.junit.Test;
import org.mockito.Mock;
import org.threeten.bp.Duration;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.temporal.ChronoUnit;

/** Created by miguelaragues on 25/10/17. */
@SuppressWarnings("KotlinInternalInJava")
public class OfflineBrushingsDataMapperTest extends BaseUnitTest {

  @Mock OfflineBrushing processedBrushing;

  @Mock IKolibreeConnector connector;

  @Mock LegacyProcessedBrushingFactory processedBrushingFactory;
  @Mock CheckupCalculator checkupCalculator;

  private static final String MAC = "AA:BB:CC";
  private static final String SERIAL = "1234";

  private OfflineBrushingsDataMapper mapper;

  /*
  CREATE BRUSHING DATA
   */

  @Test
  public void createBrushingData_translatesRecordedSessionDateToUserTimeZone() {
    ZoneOffset systemTimezone = ZoneOffset.ofHours(3);
    TrustedClock.setSystemZone(systemTimezone);

    OffsetDateTime now = TrustedClock.getNowOffsetDateTime();

    when(processedBrushing.getDatetime()).thenReturn(now.toLocalDateTime());
    when(processedBrushing.getDuration()).thenReturn(Duration.of(1, ChronoUnit.SECONDS));

    mapper = builderInstance().offlineBrushing(processedBrushing).build();

    mockProcessedData(processedBrushing, "");

    CreateBrushingData returnedData = mapper.createBrushingData(mock(KolibreeAppVersions.class));

    assertEquals(DATETIME_FORMATTER.format(now), returnedData.getDateTime());
  }

  /*
  OWNER ID
   */

  @Test
  public void ownerId_multiUserTrue_returnsMainProfileId() {
    long expectedOwnerId = 545L;

    mapper =
        builderInstance()
            .offlineBrushing(processedBrushing)
            .isMultiUserMode(true)
            .userId(1)
            .toothbrushMac(MAC)
            .toothbrushSerial(SERIAL)
            .build();

    Profile ownerProfile = mock(Profile.class);
    when(ownerProfile.getId()).thenReturn(expectedOwnerId);
    when(connector.getOwnerProfile()).thenReturn(ownerProfile);

    assertEquals(expectedOwnerId, mapper.ownerId());
  }

  @NonNull
  private OfflineBrushingsDataMapper.Builder builderInstance() {
    return new OfflineBrushingsDataMapper.Builder(
        connector, processedBrushingFactory, checkupCalculator);
  }

  @Test
  public void ownerId_multiUserFalse_returnsMainProfileId() {
    long expectedOwnerId = 545L;

    mapper =
        builderInstance()
            .offlineBrushing(processedBrushing)
            .isMultiUserMode(false)
            .userId(expectedOwnerId)
            .toothbrushMac(MAC)
            .toothbrushSerial(SERIAL)
            .build();

    assertEquals(expectedOwnerId, mapper.ownerId());
  }

  /*
  TARGET BRUSHING TIME
   */
  @Test
  public void targetBrushingTime_noMultimode_returnsTargetForUserIdProfile() {
    long ownerId = 545L;

    int expectedTargetDuration = 98;

    mapper =
        builderInstance()
            .offlineBrushing(processedBrushing)
            .isMultiUserMode(false)
            .userId(ownerId)
            .toothbrushMac(MAC)
            .toothbrushSerial(SERIAL)
            .build();

    Profile userProfile = mock(Profile.class);
    when(userProfile.getBrushingGoalTime()).thenReturn(expectedTargetDuration);
    when(connector.getProfileWithId(eq(ownerId))).thenReturn(userProfile);

    assertEquals(expectedTargetDuration, mapper.targetBrushingTime());
  }

  @Test
  public void targetBrushingTime_withMultimode_returnsTargetForMainProfile() {
    int expectedTargetDuration = 28;

    mapper =
        builderInstance()
            .offlineBrushing(processedBrushing)
            .isMultiUserMode(true)
            .userId(1L)
            .toothbrushMac(MAC)
            .toothbrushSerial(SERIAL)
            .build();

    Profile ownerProfile = mock(Profile.class);
    when(ownerProfile.getBrushingGoalTime()).thenReturn(expectedTargetDuration);
    when(connector.getOwnerProfile()).thenReturn(ownerProfile);

    assertEquals(expectedTargetDuration, mapper.targetBrushingTime());
  }

  /*
  PROCESSED DATA
   */
  @Test
  public void processedData_returnsValueFromLegacyProcessedBrushing() {
    String expectedProcessedBrushing = "my expected processed data";
    mockProcessedData(processedBrushing, expectedProcessedBrushing);

    mapper =
        spy(
            builderInstance()
                .offlineBrushing(processedBrushing)
                .isMultiUserMode(false)
                .userId(2L)
                .toothbrushMac(MAC)
                .toothbrushSerial(SERIAL)
                .build());

    assertEquals(expectedProcessedBrushing, mapper.processedData());
  }

  /*
  QUALITY
   */
  @Test
  public void quality_emptyProcessedData_returns50() {
    mapper =
        spy(
            builderInstance()
                .offlineBrushing(processedBrushing)
                .isMultiUserMode(true)
                .toothbrushMac(MAC)
                .userId(2L)
                .toothbrushSerial(SERIAL)
                .build());

    doReturn("").when(mapper).processedData();

    assertEquals(50, mapper.quality());
  }

  /*
  CREATE ORPHAN BRUSHING
   */

  @Test
  public void createOrphanBrushing() {
    OffsetDateTime dateTime = TrustedClock.getNowOffsetDateTime();
    long expectedDurationInSeconds = 180L;

    processedBrushing = createOfflineBrushing(expectedDurationInSeconds, dateTime);

    mapper =
        spy(
            builderInstance()
                .offlineBrushing(processedBrushing)
                .isMultiUserMode(true)
                .toothbrushMac(MAC)
                .userId(2L)
                .toothbrushSerial(SERIAL)
                .build());

    int expectedGoalTime = 9;
    doReturn(expectedGoalTime).when(mapper).targetBrushingTime();
    String expectedProcessedData = "dasdsad asdsa";
    doReturn(expectedProcessedData).when(mapper).processedData();

    OrphanBrushing orphanBrushing = mapper.createOrphanBrushing();

    assertEquals(expectedDurationInSeconds, orphanBrushing.getDuration());
    assertEquals(dateTime.truncatedTo(ChronoUnit.SECONDS), orphanBrushing.getDateTime());
    assertEquals(MAC, orphanBrushing.getToothbrushMac());
    assertEquals(SERIAL, orphanBrushing.getToothbrushSerial());

    assertEquals(expectedGoalTime, orphanBrushing.getGoalDuration());
    assertEquals(expectedProcessedData, orphanBrushing.getProcessedData());
  }

  /*
  UTILS
   */

  private void mockProcessedData(OfflineBrushing offlineBrushing, String processedData) {
    when(offlineBrushing.getProcessedData()).thenReturn(processedData);
  }
}
