/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.async;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.app.ui.kolibree_pro.KolibreeProReminders;
import org.junit.Test;

/** Created by miguelaragues on 7/3/18. */
public class AppClearUserContentJobServiceTest extends BaseUnitTest {

  private FakeClearUserContentJobService clearContentJobService =
      spy(new FakeClearUserContentJobService());

  @Test
  public void clearPreferences_invokesKolibreeProReminders_stopAllPeriodicReminders() {
    Context context = mock(Context.class);

    doReturn(context).when(clearContentJobService).getApplicationContext();
    // prevent super.clearPreferences from doing work
    when(context.getPackageName()).thenReturn("cn.colgate.colgateconnect");

    clearContentJobService.kolibreeProReminders = mock(KolibreeProReminders.class);
    clearContentJobService.clearPreferences();

    verify(clearContentJobService.kolibreeProReminders).stopAllPeriodicReminders();
  }

  private static class FakeClearUserContentJobService extends AppClearUserContentJobService {}
}
