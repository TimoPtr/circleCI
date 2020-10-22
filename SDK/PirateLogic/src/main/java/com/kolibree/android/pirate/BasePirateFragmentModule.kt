package com.kolibree.android.pirate

import com.kolibree.android.game.BrushingCreator
import com.kolibree.android.game.BrushingCreatorModule
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.core.IKolibreeConnector
import dagger.Module
import dagger.Provides

@Module(includes = [BrushingCreatorModule::class])
object BasePirateFragmentModule {

    @Provides
    internal fun providesViewModelFactory(
        pirateFragment: BasePirateFragment,
        connector: IKolibreeConnector,
        connectionProvider: KLTBConnectionProvider,
        brushingCreator: BrushingCreator
    ): BasePirateFragmentViewModelFactory =
        PirateFragmentViewModel.Factory.Builder(
            connector,
            connectionProvider,
            KolibreeAppVersions(pirateFragment.requireContext()),
            brushingCreator
        )
            .withToothbrushMac(pirateFragment.toothbrushMacSingle)
            .build()
}
