package com.kolibree.android.app.ui.settings.secret.persistence

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.app.toothbrush.FlavorToothbrushModels
import com.kolibree.android.app.ui.settings.secret.persistence.repo.ModelsAvailableRepository
import com.kolibree.android.app.ui.settings.secret.persistence.repo.ModelsAvailableRepositoryImpl
import com.kolibree.android.commons.ToothbrushModel
import dagger.Module
import dagger.Provides

@Module(includes = [ModelsAvailableRoomModule::class])
internal object ModelsAvailableModule {

    @Provides
    @AppScope // needs to be a singleton otherwise it will add in duplicate the toothbrushes
    fun providesModelsAvailableRepository(
        dao: ModelsAvailableDao,
        installationFlags: InstallationFlags
    ): ModelsAvailableRepository {
        val repo = ModelsAvailableRepositoryImpl(dao, installationFlags)
        val allModels = ToothbrushModel.values().toList()
        val supportedModels = FlavorToothbrushModels.defaultSupportedModels().toList()
        repo.addModels(allModels, supportedModels)
        return repo
    }
}
