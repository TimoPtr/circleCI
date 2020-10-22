package com.kolibree.android.network

import com.kolibree.android.network.core.CancelHttpRequestsUseCase
import com.kolibree.android.network.core.CancelHttpRequestsUseCaseImpl
import com.kolibree.android.network.core.capabilities.AcceptCapabilitiesHeaderProvider
import com.kolibree.android.network.core.capabilities.AcceptCapabilitiesHeaderProviderImpl
import com.kolibree.android.network.core.useragent.UserAgentHeaderProvider
import com.kolibree.android.network.core.useragent.UserAgentHeaderProviderImpl
import com.kolibree.android.network.environment.Endpoint
import com.kolibree.android.network.environment.Environment
import com.kolibree.android.network.environment.EnvironmentManager
import com.kolibree.android.network.errorhandler.NetworkErrorHandler
import com.kolibree.android.network.errorhandler.RemoteAccountDoesNotExistDetector
import com.kolibree.android.network.errorhandler.RemoteAccountDoesNotExistDetectorImpl
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.crypto.di.CryptoModule
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [NetworkLogToggleModule::class, CryptoModule::class])
abstract class NetworkModule {

    internal companion object {

        @Provides
        internal fun providesEndpoint(environmentManager: EnvironmentManager): Endpoint {
            return environmentManager.endpoint()
        }

        @Provides
        internal fun providesEnvironment(environmentManager: EnvironmentManager): Environment {
            return environmentManager.environment()
        }
    }

    @Binds
    internal abstract fun bindsNetworkChecker(networkChecker: NetworkCheckerImpl): NetworkChecker

    @Binds
    internal abstract fun bindsUserAgentHeaderProvider(userAgentHeaderProvider: UserAgentHeaderProviderImpl):
        UserAgentHeaderProvider

    @Binds
    internal abstract fun bindsAcceptCapabilitiesHeaderProvider(impl: AcceptCapabilitiesHeaderProviderImpl):
        AcceptCapabilitiesHeaderProvider

    @Binds
    internal abstract fun bindsErrorHandler(impl: RemoteAccountDoesNotExistDetectorImpl): NetworkErrorHandler

    @Binds
    internal abstract fun bindsRemoteAccountDoesNotExistDetector(impl: RemoteAccountDoesNotExistDetectorImpl):
        RemoteAccountDoesNotExistDetector

    @Binds
    internal abstract fun bindsCancelHttpRequestsUseCase(
        impl: CancelHttpRequestsUseCaseImpl
    ): CancelHttpRequestsUseCase
}
