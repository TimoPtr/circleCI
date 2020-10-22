/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks.rewards;

import com.kolibree.android.rewards.models.Redeem;

@SuppressWarnings("KotlinInternalInJava")
public class RedeemBuilder {

  private String redeemUrl;
  private String result;
  private Long rewardsId;

  private RedeemBuilder(Long rewardsId) {
    this.rewardsId = rewardsId;
  }

  public static RedeemBuilder create(Long rewardsId) {
    return new RedeemBuilder(rewardsId);
  }

  public RedeemBuilder withRedeemUrl(String redeemUrl) {
    this.redeemUrl = redeemUrl;
    return this;
  }

  public RedeemBuilder withResult(String result) {
    this.result = result;
    return this;
  }

  public Redeem build() {
    return new Redeem(redeemUrl, result, rewardsId);
  }
}
