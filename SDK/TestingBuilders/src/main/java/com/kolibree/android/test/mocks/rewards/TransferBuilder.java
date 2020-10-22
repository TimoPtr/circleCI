/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks.rewards;

import com.kolibree.android.rewards.models.Transfer;

@SuppressWarnings("KotlinInternalInJava")
public class TransferBuilder {

  private int smiles;
  private Long fromProfileId;
  private Long toProfileId;

  private TransferBuilder(int smiles, Long fromProfileId, Long toProfileId) {
    this.smiles = smiles;
    this.fromProfileId = fromProfileId;
    this.toProfileId = toProfileId;
  }

  public static TransferBuilder create(int smiles, Long fromProfileId, Long toProfileId) {
    return new TransferBuilder(smiles, fromProfileId, toProfileId);
  }

  public Transfer build() {
    return new Transfer(smiles, fromProfileId, toProfileId);
  }
}
