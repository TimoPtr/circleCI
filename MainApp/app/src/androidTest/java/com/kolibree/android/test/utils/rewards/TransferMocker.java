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

import com.kolibree.android.rewards.synchronization.transfer.TransferData;
import com.kolibree.android.rewards.synchronization.transfer.TransferNetworkService;
import com.kolibree.android.test.mocks.rewards.TransferBuilder;

@SuppressWarnings("KotlinInternalInJava")
public class TransferMocker {
  public static void mockTransferSmiles(
      TransferNetworkService transferNetworkService, TransferBuilder transferBuilder) {
    when(transferNetworkService.transferSmiles(any(TransferData.class)))
        .thenReturn(transferBuilder.build());
  }
}
