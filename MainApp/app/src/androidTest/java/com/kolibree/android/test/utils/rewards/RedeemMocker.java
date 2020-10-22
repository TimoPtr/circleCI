/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils.rewards;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.kolibree.android.rewards.synchronization.redeem.RedeemData;
import com.kolibree.android.rewards.synchronization.redeem.RedeemNetworkService;
import com.kolibree.android.test.mocks.rewards.RedeemBuilder;

@SuppressWarnings("KotlinInternalInJava")
public class RedeemMocker {
  public static void mockClaimRedeem(
      RedeemNetworkService redeemNetworkService, RedeemBuilder builder) {
    when(redeemNetworkService.claimRedeem(any(RedeemData.class))).thenReturn(builder.build());
  }
}
