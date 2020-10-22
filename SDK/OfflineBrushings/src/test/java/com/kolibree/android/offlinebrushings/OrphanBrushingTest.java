/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.processedbrushings.CheckupCalculator;
import com.kolibree.android.processedbrushings.CheckupData;
import com.kolibree.android.utils.KolibreeAppVersions;
import com.kolibree.sdkws.data.model.CreateBrushingData;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.threeten.bp.Duration;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.temporal.ChronoUnit;

/** Created by miguelaragues on 28/11/17. */
public class OrphanBrushingTest extends BaseUnitTest {

  @Mock Context context;
  @Mock CheckupCalculator checkupCalculator;

  @Test
  public void toCreateBrushingData() {
    OffsetDateTime date = TrustedClock.getNowOffsetDateTime();
    String mac = "aa:bb";
    String serial = "1234";
    String processedData = "{}";
    int goalDuration = 78;
    long duration = 6576L;
    OrphanBrushing orphanBrushing =
        spy(OrphanBrushing.create(duration, goalDuration, processedData, date, serial, mac));

    when(context.getPackageManager())
        .thenAnswer(
            (Answer<PackageManager>)
                invocation -> {
                  throw new NameNotFoundException();
                });

    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

    /*doAnswer(
        invocation -> {
          CreateBrushingData realBrushingData =
              (CreateBrushingData) invocation.callRealMethod();

          CreateBrushingData spyBrushingData = spy(realBrushingData);

          doNothing().when(spyBrushingData).addProcessedData(any());

          return spyBrushingData;
        })
    .when(orphanBrushing)
    .innerCreateBrushingData();*/
    CheckupData checkupData = mock(CheckupData.class);
    when(checkupCalculator.calculateCheckup(eq(processedData), anyLong(), any(Duration.class)))
        .thenReturn(checkupData);
    String expectedProcessedData = "{}";
    Integer expectedCoverage = 32;
    when(checkupData.getSurfacePercentage()).thenReturn(expectedCoverage);

    CreateBrushingData brushingData =
        orphanBrushing.toCreateBrushingData(new KolibreeAppVersions(context), checkupCalculator);

    assertEquals(duration, brushingData.getDuration());
    assertEquals(goalDuration, brushingData.getGoalDuration());

    assertEquals(date.truncatedTo(ChronoUnit.SECONDS), brushingData.getDate());
    assertEquals(serial, brushingData.getSerial());
    assertEquals(mac, brushingData.getMac());
    assertEquals(expectedProcessedData, brushingData.getProcessedData());
    assertEquals(expectedCoverage, brushingData.getCoverage());
  }
}
