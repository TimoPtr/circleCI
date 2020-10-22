package com.kolibree.android.app.sdkwrapper

import com.kolibree.R
import com.kolibree.android.network.environment.Environment
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object CredentialsModule {

    @Provides
    @Named(KolibreeModule.DI_CLIENT_ID_RES)
    fun providesCliendIdRes(endpoint: Environment?): Int = when (endpoint) {
        Environment.PRODUCTION -> R.string.kolibree_client_id_production
        Environment.DEV -> R.string.kolibree_client_id_dev
        Environment.CUSTOM -> 0
        Environment.STAGING -> R.string.kolibree_client_id
        else -> R.string.kolibree_client_id
    }

    @Provides
    @Named(KolibreeModule.DI_CLIENT_SECRET_RES)
    fun providesClientSecretRes(endpoint: Environment?): Int = when (endpoint) {
        Environment.PRODUCTION -> R.string.kolibree_client_secret_production
        Environment.DEV -> R.string.kolibree_client_secret_dev
        Environment.CUSTOM -> 0
        Environment.STAGING -> R.string.kolibree_client_secret
        else -> R.string.kolibree_client_secret
    }

    @Provides
    @Named(KolibreeModule.DI_CLIENT_SECRET_IV_RES)
    fun providesClientSecretIvRes(endpoint: Environment?): Int = when (endpoint) {
        Environment.PRODUCTION -> R.string.kolibree_client_secret_iv_production
        Environment.DEV -> R.string.kolibree_client_secret_iv_dev
        Environment.CUSTOM -> 0
        Environment.STAGING -> R.string.kolibree_client_secret_iv
        else -> R.string.kolibree_client_secret_iv
    }
}
