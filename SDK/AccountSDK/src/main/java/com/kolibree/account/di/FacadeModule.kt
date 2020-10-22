package com.kolibree.account.di

import com.kolibree.account.AccountFacade
import com.kolibree.account.AccountFacadeImpl
import com.kolibree.account.ProfileFacade
import com.kolibree.account.ProfileFacadeImpl
import com.kolibree.account.profile.ForgetDeletedProfileToothbrushesHook
import com.kolibree.account.profile.ProfileDeletedHook
import com.kolibree.account.profile.ReassignSharedToothbrushHook
import com.kolibree.account.profile.ResetActiveProfileHook
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module(includes = [ProfileDeletedHooksModule::class])
internal abstract class FacadeModule {

    @Binds
    internal abstract fun bindsAccountFacader(impl: AccountFacadeImpl): AccountFacade

    @Binds
    internal abstract fun bindsProfileFacader(impl: ProfileFacadeImpl): ProfileFacade
}

@Module
internal abstract class ProfileDeletedHooksModule {
    @Binds
    @IntoSet
    abstract fun bindsForgetDeletedProfileToothbrushesHook(impl: ForgetDeletedProfileToothbrushesHook): ProfileDeletedHook

    @Binds
    @IntoSet
    abstract fun bindsResetActiveProfileHook(impl: ResetActiveProfileHook): ProfileDeletedHook

    @Binds
    @IntoSet
    abstract fun bindsReassignSharedToothbrushHook(impl: ReassignSharedToothbrushHook): ProfileDeletedHook
}
