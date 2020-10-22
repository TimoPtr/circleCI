package com.kolibree.android.rewards.synchronization.challenges

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.kolibree.android.rewards.models.CategoryEntity
import com.kolibree.android.synchronizator.models.SynchronizableCatalog

/**
 * Representation of catalog_list.json
 *
 * See https://confluence.kolibree.com/x/cwjp
 */
@Keep
internal data class ChallengesCatalogApi(
    @SerializedName("catalog") val categories: List<CategoryEntity>,
    val language: String
) : SynchronizableCatalog
