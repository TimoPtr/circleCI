/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.google.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.crypto.KolibreeGuard
import com.kolibree.crypto.extractHexToByteArray
import com.kolibree.sdkws.data.request.CreateAccountData
import javax.inject.Inject
import timber.log.Timber

/**
 * Google sign-in logic wrapper.
 */
@VisibleForApp
interface GoogleSignInWrapper {

    /**
     * Get the [Intent] used for logging/signIn with the Google Clients
     * The sign-in Intent works for both login and sign-up states
     */
    fun getSignInIntent(): Intent

    /**
     * Parses intent data and attempts to fill [accountBuilder] with Google account data needed
     * for login
     * @param data result intent from Google Sign in screen
     * @param accountBuilder builder that will be filled with account data if they were present
     * @return true if account builder was filled with data, false otherwise
     */
    fun maybeFillDataForLogin(
        data: Intent,
        accountBuilder: CreateAccountData.Builder
    ): Boolean

    /**
     * Parses intent data and attempts to fill [accountBuilder] with Google account data needed
     * for new account creation
     * @param data result intent from Google Sign in screen
     * @param accountBuilder builder that will be filled with account data if they were present
     * @return true if account builder was filled with data, false otherwise
     */
    fun maybeFillDataForAccountCreation(
        data: Intent,
        accountBuilder: CreateAccountData.Builder
    ): Boolean

    /**
     * Unpairs the app from current Google account upon logout. If user tries to sign in
     * with Google again, Google account choose dialog will be shown again.
     *
     * If there's not Google account associated with the app, this is a no-op operation.
     *
     * @see https://developers.google.com/identity/sign-in/android/disconnect#sign_out_users
     */
    fun unpairApp()

    /**
     * It is highly recommended that you provide users that signed in with Google the ability
     * to disconnect their Google account from the app. If the user deletes their account,
     * we must delete the information that app obtained from the Google APIs.
     *
     * If there's not Google account associated with the app, this is a no-op operation.
     *
     * @see https://developers.google.com/identity/sign-in/android/disconnect#disconnect_accounts
     */
    fun revokeAccess()
}

@SuppressLint("ExperimentalClassUse")
internal class GoogleSignInWrapperImpl @Inject constructor(
    private val context: ApplicationContext,
    kolibreeGuard: KolibreeGuard,
    credentials: GoogleSignInCredentials
) : GoogleSignInWrapper {

    private val options: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId()
            .requestIdToken(revealWebClientId(context, kolibreeGuard, credentials))
            .requestProfile()
            .requestEmail()
            .build()
    }

    private val client: GoogleSignInClient by lazy { GoogleSignIn.getClient(context, options) }

    override fun getSignInIntent(): Intent = client.signInIntent

    override fun maybeFillDataForLogin(
        data: Intent,
        accountBuilder: CreateAccountData.Builder
    ): Boolean = withValidGoogleAccount(data) { account ->
        fillAccountBuilderForLogin(accountBuilder, account)
    }

    override fun maybeFillDataForAccountCreation(
        data: Intent,
        accountBuilder: CreateAccountData.Builder
    ): Boolean = withValidGoogleAccount(data) { account ->
        fillAccountBuilderForAccountCreation(accountBuilder, account)
    }

    override fun unpairApp() {
        client.signOut()
    }

    override fun revokeAccess() {
        client.revokeAccess()
    }

    private fun withValidGoogleAccount(
        data: Intent,
        fillAccountBlock: (GoogleSignInAccount) -> Unit
    ): Boolean {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account = task.getResult(ApiException::class.java)
                ?: throw ApiException(Status.RESULT_INTERNAL_ERROR)
            fillAccountBlock(account)
            true
        } catch (e: ApiException) {
            Timber.w(e, "Google sign-in failed!")
            false
        }
    }

    @VisibleForTesting
    fun fillAccountBuilderForLogin(
        accountBuilder: CreateAccountData.Builder,
        account: GoogleSignInAccount
    ) {
        account.id?.let { accountBuilder.setGoogleId(it) }
        account.idToken?.let { accountBuilder.setGoogleIdToken(it) }
        account.email?.let { accountBuilder.setEmail(it) }
    }

    @VisibleForTesting
    fun fillAccountBuilderForAccountCreation(
        accountBuilder: CreateAccountData.Builder,
        account: GoogleSignInAccount
    ) {
        fillAccountBuilderForLogin(accountBuilder, account)
        // Let's not save name for now, we will rely on param sent by the user
        // account.givenName?.let { accountBuilder.setFirstName(it) }
        account.photoUrl?.let { accountBuilder.setGoogleAvatarUrl(it.toString()) }
    }

    companion object {

        private fun revealWebClientId(
            context: Context,
            kolibreeGuard: KolibreeGuard,
            credentials: GoogleSignInCredentials
        ): String {
            val encryptedWebClientId = context.getString(credentials.encryptedWebClientId)

            // Should be remove when credential for PROD will be available
            if (encryptedWebClientId.isEmpty()) {
                Timber.w("webClientID is empty")
                return ""
            }

            return kolibreeGuard.reveal(
                encryptedWebClientId,
                context.getString(credentials.iv).extractHexToByteArray()
            )
        }
    }
}
