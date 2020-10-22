/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate

import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.kolibree.sdkws.data.model.gopirate.UpdateGoPirateData
import io.reactivex.Observable

@VisibleForApp
interface BasePirateFragmentViewModel {
    fun viewStateObservable(): Observable<PirateFragmentViewState>
    fun updatePirateData(pirateData: UpdateGoPirateData?)
    fun onBrushingCompleted(brushingData: CreateBrushingData, pirateData: UpdateGoPirateData?)
}

internal interface BasePirateFragmentViewModelFactory : ViewModelProvider.Factory
