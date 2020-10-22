/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks.rewards;

import androidx.annotation.NonNull;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.rewards.models.PrizeEntity;
import com.kolibree.android.rewards.synchronization.prizes.PrizeApi;
import com.kolibree.android.rewards.synchronization.prizes.PrizeDetailsApi;
import com.kolibree.android.rewards.synchronization.prizes.PrizesCatalogApi;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.threeten.bp.LocalDate;

@SuppressWarnings("KotlinInternalInJava")
public class PrizeHelper {

  public static PrizeHelper create() {
    return new PrizeHelper();
  }

  private Set<PrizeEntity> prizeEntities = new HashSet<>();

  private PrizeHelper() {}

  public PrizeHelper addPrize(PrizeEntity prizeEntity) {
    prizeEntities.add(prizeEntity);
    return this;
  }

  public PrizesCatalogApi prizeEntities() {

    List<PrizeApi> prizes = new ArrayList<>();

    for (PrizeEntity prizeEntity : prizeEntities) {
      prizes.add(
          new PrizeApi(
              prizeEntity.getId(),
              prizeEntity.getCategory(),
              new ArrayList<PrizeDetailsApi>() {
                {
                  add(
                      new PrizeDetailsApi(
                          prizeEntity.getSmilesRequired(),
                          prizeEntity.getPurchasable(),
                          prizeEntity.getVoucherDiscount(),
                          prizeEntity.getDescription(),
                          prizeEntity.getTitle(),
                          prizeEntity.getCompany(),
                          prizeEntity.getPictureUrl(),
                          prizeEntity.getCreationTime(),
                          prizeEntity.getId(),
                          prizeEntity.getProductId()));
                }
              }));
    }

    return new PrizesCatalogApi(prizes);
  }

  public static class PrizeBuilder {
    public static final int DEFAULT_ID = 0;
    public static final String DEFAULT_CATEGORY = "category 1";
    public static final String DEFAULT_DESCRIPTION = "description prize";
    public static final String DEFAULT_TITLE = "title";
    public static final int DEFAULT_SMILES_REQUIRED = 100;
    public static final boolean DEFAULT_PURCHASABLE = true;
    public static final double DEFAULT_VOUCHER_DISCOUNT = 10.0;
    public static final String DEFAULT_COMPANY = "Kolibree";
    public static final String DEFAULT_PICTURE_URL = "https://www.google.com/favicon.ico";
    public static final int DEFAULT_PRODUCT_ID = 0;

    private int id = DEFAULT_ID;
    private String category = DEFAULT_CATEGORY;
    private String description = DEFAULT_DESCRIPTION;
    private String title = DEFAULT_TITLE;
    private LocalDate creationTime = TrustedClock.getNowLocalDate();
    private int smilesRequired = DEFAULT_SMILES_REQUIRED;
    private boolean purchasable = DEFAULT_PURCHASABLE;
    private double voucherDiscount = DEFAULT_VOUCHER_DISCOUNT;
    private String company = DEFAULT_COMPANY;
    private String pictureUrl = DEFAULT_PICTURE_URL;
    private int productId = DEFAULT_PRODUCT_ID;

    private PrizeBuilder() {}

    public static PrizeBuilder create() {
      return new PrizeBuilder();
    }

    public PrizeBuilder withId(int id) {
      this.id = id;
      return this;
    }

    @NonNull
    public PrizeBuilder withPictureUrl(String pictureUrl) {
      this.pictureUrl = pictureUrl;
      return this;
    }

    @NonNull
    public PrizeBuilder withSmilesRequired(int smilesRequired) {
      this.smilesRequired = smilesRequired;
      return this;
    }

    @NonNull
    public PrizeBuilder withTitle(String title) {
      this.title = title;
      return this;
    }

    public PrizeEntity buildEntity() {
      return new PrizeEntity(
          id,
          category,
          description,
          title,
          creationTime,
          smilesRequired,
          purchasable,
          voucherDiscount,
          company,
          pictureUrl,
          productId);
    }
  }
}
