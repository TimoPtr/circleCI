package com.kolibree.sdkws.account

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.synchronizator.models.BundleCreator
import com.kolibree.sdkws.account.sync.AccountBundleCreator
import com.kolibree.sdkws.account.sync.AccountSynchronizedVersions
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import retrofit2.Retrofit

@Module
internal abstract class AccountNetworkModule {

    companion object {
        @Provides
        @AppScope
        internal fun providesAccountApiService(retrofit: Retrofit): AccountApi {
            return retrofit.create(AccountApi::class.java)
        }
    }

    @Binds
    internal abstract fun bindsAccountManager(accountManager: AccountManagerImpl): AccountManager

    @Binds
    internal abstract fun bindsInternalAccountManager(accountManager: AccountManagerImpl): InternalAccountManager

    @Binds
    internal abstract fun bindsMagik6P0Manager(magik6P0Manager: Magik6P0ManagerImpl): Magik6P0Manager

    @Binds
    @IntoSet
    internal abstract fun bindsAccountBundleCreator(accoutBundleCreator: AccountBundleCreator): BundleCreator

    @Binds
    @IntoSet
    internal abstract fun bindsAccountSynchronizedVersions(impl: AccountSynchronizedVersions): Truncable
}
