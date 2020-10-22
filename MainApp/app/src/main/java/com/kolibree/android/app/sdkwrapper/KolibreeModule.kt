/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.sdkwrapper

import android.content.Context
import com.kolibree.R
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.google.auth.GoogleSignInCredentials
import com.kolibree.android.network.environment.Credentials
import com.kolibree.android.network.environment.CustomCredentialsManager
import com.kolibree.android.network.environment.Environment
import com.kolibree.crypto.KolibreeGuard
import com.kolibree.sdkws.core.IKolibreeConnector
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Provider
import timber.log.Timber

/** Created by miguelaragues on 14/7/17.  */
@Module(includes = [CredentialsModule::class])
abstract class KolibreeModule {

    @Binds
    @AppScope
    internal abstract fun providesKolibreeFacade(kolibreeFacade: KolibreeFacadeImpl): KolibreeFacade

    companion object {

        internal const val DI_CLIENT_SECRET_RES = "di_client_secret_res"
        internal const val DI_CLIENT_SECRET_IV_RES = "di_client_secret_iv_res"
        internal const val DI_CLIENT_ID_RES = "di_client_id_res"
        const val DI_ACTIVE_PROFILE = "di_active_profile"

        @Provides
        @Named(DI_ACTIVE_PROFILE)
        internal fun providesActiveProfile(kolibreeConnector: IKolibreeConnector): Profile? {
            return kolibreeConnector.currentProfile
        }

        @Provides
        @Suppress("LongParameterList", "TooGenericExceptionCaught")
        internal fun providesCredentials(
            context: Context,
            @Named(DI_CLIENT_ID_RES) clientIdProvider: Provider<Int>,
            @Named(DI_CLIENT_SECRET_RES) clientSecretProvider: Provider<Int>,
            @Named(DI_CLIENT_SECRET_IV_RES) clientSecretIvProvider: Provider<Int>,
            kolibreeGuard: KolibreeGuard,
            environment: Environment,
            customCredentialsManager: CustomCredentialsManager
        ): Credentials {
            var credentials: Credentials? = null
            if (environment === Environment.CUSTOM) {
                credentials = createCustomCredentials(customCredentialsManager)
            }

            if (credentials == null) {
                val clientId = context.getString(clientIdProvider.get())

                try {
                    credentials =
                        Credentials(
                            clientId,
                            String(
                                kolibreeGuard.revealFromStringBase64(
                                    context,
                                    clientSecretProvider.get(),
                                    clientSecretIvProvider.get()
                                )
                            )
                        )
                } catch (e: Exception) {
                    Timber.e(e)
                    throw FailedToRevealClientSecretException(e)
                }
            }

            return credentials
        }

        @Provides
        internal fun providesGoogleSignInCredentials(
            environment: Environment
        ): GoogleSignInCredentials =
            when (environment) {
                Environment.PRODUCTION -> GoogleSignInCredentials(
                    R.string.google_sign_in_production_web_client_id,
                    R.string.google_sign_in_production_web_client_id_iv
                )
                else -> GoogleSignInCredentials(
                    R.string.google_sign_in_staging_web_client_id,
                    R.string.google_sign_in_staging_web_client_id_iv
                )
        }

        @Suppress("TooGenericExceptionCaught")
        private fun createCustomCredentials(
            customCredentialsManager: CustomCredentialsManager
        ): Credentials? {
            val customCredentials = customCredentialsManager.getCustomCredentials()

            try {
                val clientId = customCredentials.clientId()
                val encryptedclientSecret = customCredentials.clientSecret()

                return Credentials(clientId, encryptedclientSecret)
            } catch (e: Exception) {
                Timber.e(e)
            }

            return null
        }
    }
}

class FailedToRevealClientSecretException(override val cause: Throwable?) : RuntimeException()
