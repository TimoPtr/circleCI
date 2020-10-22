/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.app.ui.brushhead.api.BrushHeadInformationApi
import com.kolibree.android.app.ui.brushhead.api.model.request.BrushHeadInformationResponse
import com.kolibree.android.app.ui.brushhead.api.model.request.data.BrushHeadData
import com.kolibree.android.app.ui.brushhead.di.BrushHeadDataModule
import com.kolibree.android.app.ui.brushhead.di.BrushHeadDomainModule
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.models.StrippedMac
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import retrofit2.Response

@Module(
    includes = [
        BrushHeadDomainModule::class,
        BrushHeadDataModule::class
    ]
)
class EspressoBrushHeadModule {

    @Provides
    fun provideFakeApi(): BrushHeadInformationApi = FakeBrushHeadInformationApi
}

/**
 * Not used yet in any test, but allow us to be flexible in future days
 */
object FakeBrushHeadInformationApi : BrushHeadInformationApi {
    override fun updateBrushHead(
        accountId: Long,
        profileId: Long,
        serialNumber: String,
        macAddress: StrippedMac,
        body: BrushHeadData
    ) = Single.just(Response.success(getResponse()))

    override fun getBrushHeadInformation(
        accountId: Long,
        profileId: Long,
        serialNumber: String,
        macAddress: StrippedMac
    ) = Single.just(Response.success(getResponse()))

    private fun getResponse() = BrushHeadInformationResponse(
        firstUsed = TrustedClock.getNowOffsetDateTime(),
        numberOfUses = 0,
        daysSinceLastChange = 0,
        totalBrushingTime = 0,
        percentageLeft = 0
    )
}
