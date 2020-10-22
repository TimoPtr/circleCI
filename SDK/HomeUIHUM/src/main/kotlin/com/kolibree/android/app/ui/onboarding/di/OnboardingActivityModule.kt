/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.di

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.app.ui.onboarding.OnboardingActivity
import com.kolibree.android.app.ui.onboarding.OnboardingActivityViewModel
import com.kolibree.android.app.ui.onboarding.OnboardingSharedViewModel
import com.kolibree.android.app.ui.onboarding.emailcheck.EmailCheckFragment
import com.kolibree.android.app.ui.onboarding.enteremail.EnterEmailFragment
import com.kolibree.android.app.ui.onboarding.getready.GetReadyFragment
import com.kolibree.android.app.ui.onboarding.login.LoginFragment
import com.kolibree.android.app.ui.onboarding.navigator.LoginNavigator
import com.kolibree.android.app.ui.onboarding.navigator.LoginNavigatorViewModel
import com.kolibree.android.app.ui.onboarding.navigator.SignUpNavigator
import com.kolibree.android.app.ui.onboarding.navigator.SignUpNavigatorViewModel
import com.kolibree.android.app.ui.onboarding.signup.SignUpFragment
import com.kolibree.android.app.ui.pairing.PairingFlowHost
import com.kolibree.android.app.ui.pairing.PairingFlowModule
import com.kolibree.android.app.ui.welcome.InstallationSourceModule
import com.kolibree.android.google.auth.GoogleSignInModule
import com.kolibree.pairing.PairingModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module(
    includes = [
        OnboardingActivityBindingModule::class,
        OnboardingFragmentModule::class,
        GoogleSignInModule::class,
        InstallationSourceModule::class,
        PairingModule::class,
        PairingFlowModule::class
    ]
)
abstract class OnboardingActivityModule

@Module
abstract class OnboardingActivityBindingModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: OnboardingActivity): AppCompatActivity

    internal companion object {

        @Provides
        internal fun provideActivityViewModel(
            activity: OnboardingActivity,
            viewModelFactory: OnboardingActivityViewModel.Factory
        ): OnboardingSharedViewModel {
            return ViewModelProvider(
                activity,
                viewModelFactory
            ).get(OnboardingActivityViewModel::class.java)
        }
    }

    @Binds
    internal abstract fun bindsPairingHost(onboardingSharedViewModel: OnboardingSharedViewModel): PairingFlowHost
}

@Module
abstract class OnboardingFragmentModule {

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeGetReadyFragment(): GetReadyFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [LoginNavigationModule::class])
    internal abstract fun contributeLoginFragment(): LoginFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeEmailCheckFragment(): EmailCheckFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [SignUpNavigationModule::class])
    internal abstract fun contributeSignUpFragment(): SignUpFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeEnterEmailFragment(): EnterEmailFragment
}

@Module
class LoginNavigationModule {
    @Provides
    internal fun providesLoginNavigator(fragment: LoginFragment): LoginNavigator {
        return fragment.createNavigatorAndBindToLifecycle(LoginNavigatorViewModel::class)
    }
}

@Module
class SignUpNavigationModule {
    @Provides
    internal fun providesSignUpNavigator(fragment: SignUpFragment): SignUpNavigator {
        return fragment.createNavigatorAndBindToLifecycle(SignUpNavigatorViewModel::class)
    }
}
