package com.kolibree.android.app.dagger

import com.kolibree.android.app.async.AppClearUserContentJobService
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.ui.game.StartNonUnityGameModule
import com.kolibree.android.app.ui.settings.secret.SecretSettingsActivity
import com.kolibree.android.app.ui.settings.secret.badges.BadgesPlaygroundActivity
import com.kolibree.android.app.ui.settings.secret.dialogs.DialogsPlaygroundActivity
import com.kolibree.android.app.ui.settings.secret.environment.ChangeEnvironmentModule
import com.kolibree.android.app.ui.settings.secret.fakebrushings.CreateFakeBrushingBindingModule
import com.kolibree.android.unity.di.GameMiddlewareBindingModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [GameMiddlewareBindingModule::class, CreateFakeBrushingBindingModule::class])
internal abstract class BindingModule {

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindClearUserContentJobService(): AppClearUserContentJobService

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            SecretSettingsNavigatorModule::class,
            ChangeEnvironmentModule::class,
            StartNonUnityGameModule::class
        ]
    )
    abstract fun bindSecretSettingsActivity(): SecretSettingsActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindDialogsPlaygroundActivity(): DialogsPlaygroundActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindBadgesPlaygroundActivity(): BadgesPlaygroundActivity
}
