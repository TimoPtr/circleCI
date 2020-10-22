package com.kolibree.android.pirate

import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.game.GameScope
import com.kolibree.android.pirate.controller.kml.KMLPirateModule
import com.kolibree.android.pirate.tuto.TutoRepositoryModule
import com.kolibree.android.pirate.tuto.persistence.room.TutoRoomModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(
    includes = [
        TutoRepositoryModule::class,
        TutoRoomModule::class
    ]
)
abstract class PirateModule {

    @ActivityScope
    @GameScope
    @ContributesAndroidInjector(modules = [PirateFragmentModule::class])
    internal abstract fun bindPirateCompatActivity(): PirateCompatActivity

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector(modules = [PirateFragmentModule::class, KMLPirateModule::class])
    internal abstract fun bindPirateFragment(): PirateFragment
}
