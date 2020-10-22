/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.environment

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.kolibree.BR
import com.kolibree.R
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.settings.secret.SecretSettingsNavigator
import com.kolibree.android.network.environment.Credentials
import com.kolibree.android.network.environment.CustomCredentialsManager
import com.kolibree.android.network.environment.CustomEnvironment
import com.kolibree.android.network.environment.Environment
import com.kolibree.android.network.environment.EnvironmentManager
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import javax.inject.Inject
import me.tatarka.bindingcollectionadapter2.ItemBinding

internal interface ChangeEnvironment {
    val environments: List<Environment>
    val environmentBinding: ItemBinding<Environment>
    val environmentPosition: MediatorLiveData<Int>
    val customEnvironmentVisibility: LiveData<Int>
    val customEndpointUrl: MediatorLiveData<String>
    val customClientId: MediatorLiveData<String>
    val customClientSecret: MediatorLiveData<String>

    fun onChangeEnvironmentConfirmed(environment: Environment)
    fun onSetCustomEnvironment()
    fun onChangeEnvironmentCancelled()
}

internal class ChangeEnvironmentViewModel(
    initialViewState: ChangeEnvironmentViewState,
    private val environmentManager: EnvironmentManager,
    private val customCredentialsManager: CustomCredentialsManager,
    private val secretSettingsNavigator: SecretSettingsNavigator,
    private val jobScheduler: JobScheduler,
    private val clearUserContentJobInfo: JobInfo
) : BaseViewModel<ChangeEnvironmentViewState, ChangeEnvironmentAction>(initialViewState),
    ChangeEnvironment {

    // Hack: we want to ignore the value until we are resumed
    private var reactOnEnvironmentChange = false

    // Hack: when the spinner is init the first value is emitted we want to ignore it
    private var shouldIgnoreEnvironmentChange = true

    override val environments: List<Environment> = Environment.values().toList()
    override val environmentBinding: ItemBinding<Environment> =
        ItemBinding.of<Environment>(BR.item, R.layout.item_spinner_environment)

    override val environmentPosition: MediatorLiveData<Int> = twoWayMap(
        viewStateLiveData,
        mapper = { state -> environments.indexOf(state?.selectedEnvironment) },
        updateHandler = {
            if (shouldIgnoreEnvironmentChange) {
                // Hack to ignore the first state of the spinner where it select the first value
                shouldIgnoreEnvironmentChange = false
                return@twoWayMap
            }

            if (reactOnEnvironmentChange) {
                it?.let {
                    val environment = environments[it]
                    onSelectedEnvironment(environment)
                }
            }
        }
    )

    override val customEnvironmentVisibility: LiveData<Int> =
        mapNonNull(viewStateLiveData, View.GONE) { viewState ->
            if (Environment.CUSTOM == viewState.selectedEnvironment) View.VISIBLE else View.GONE
        }

    override val customEndpointUrl: MediatorLiveData<String> = twoWayMap(
        viewStateLiveData,
        mapper = { state -> state?.customEndpointUrl },
        updateHandler = { endpoint -> updateViewState { copy(customEndpointUrl = endpoint) } }
    )

    override val customClientId: MediatorLiveData<String> = twoWayMap(
        viewStateLiveData,
        mapper = { state -> state?.customClientId },
        updateHandler = { appId -> updateViewState { copy(customClientId = appId) } }
    )

    override val customClientSecret: MediatorLiveData<String> = twoWayMap(
        viewStateLiveData,
        mapper = { state -> state?.customClientSecret },
        updateHandler = { appSecret -> updateViewState { copy(customClientSecret = appSecret) } }
    )

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        reactOnEnvironmentChange = true
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        reactOnEnvironmentChange = false
    }

    override fun onChangeEnvironmentConfirmed(environment: Environment) {
        if (environment != Environment.CUSTOM) {
            setPredefinedEnvironment(environment)
        } else {
            setCustomEnvironment()
        }
    }

    override fun onChangeEnvironmentCancelled() {
        selectEnvironment(environmentManager.environment())
    }

    override fun onSetCustomEnvironment() {
        getViewState()?.run {
            if (customEndpointUrl.isNullOrBlank() ||
                customClientId.isNullOrBlank() ||
                customClientSecret.isNullOrBlank()
            ) {
                pushAction(ShowCustomEnvironmentMissingFieldErrorAction)
                return
            }
            if (environmentManager.endpointUrlAlreadyExists(CustomEnvironment(customEndpointUrl))) {
                pushAction(ShowCustomEnvironmentUrlExistsAction)
                return
            }
            pushAction(ShowConfirmChangeEnvironmentAction(Environment.CUSTOM))
        }
    }

    @VisibleForApp
    fun onSelectedEnvironment(environment: Environment) {
        selectEnvironment(environment)
        if (environment != environmentManager.environment()) {
            val isPredefinedEnvironment = environment != Environment.CUSTOM
            if (isPredefinedEnvironment) {
                pushAction(ShowConfirmChangeEnvironmentAction(environment))
            }
        }
    }

    private fun setPredefinedEnvironment(environment: Environment) {
        environmentManager.setEnvironment(environment)
        onEnvironmentSuccessfullyChanged()
    }

    @Suppress("LongMethod")
    private fun setCustomEnvironment() {
        getViewState()?.run {
            if (customEndpointUrl.isNullOrBlank() || customClientId.isNullOrBlank() ||
                customClientSecret.isNullOrBlank()
            ) {
                pushAction(ShowCustomEnvironmentMissingFieldErrorAction)
                return
            }
            val customEnvironment = CustomEnvironment(customEndpointUrl)
            val customCredentials = Credentials(customClientId, customClientSecret)
            if (environmentManager.setCustomEnvironment(customEnvironment)) {
                customCredentialsManager.setCustomCredentials(
                    customCredentials.clientId(),
                    customCredentials.clientSecret()
                )
                onEnvironmentSuccessfullyChanged()
            } else {
                pushAction(ShowCustomEnvironmentSomethingWrongAction)
            }
        }
    }

    private fun onEnvironmentSuccessfullyChanged() {
        jobScheduler.schedule(clearUserContentJobInfo)
        secretSettingsNavigator.navigateToStartScreen()
    }

    private fun selectEnvironment(environment: Environment) {
        updateViewState {
            copy(selectedEnvironment = environment)
        }
    }

    class Factory @Inject constructor(
        private val environmentManager: EnvironmentManager,
        private val customCredentialsManager: CustomCredentialsManager,
        private val secretSettingsNavigator: SecretSettingsNavigator,
        private val jobScheduler: JobScheduler,
        private val clearUserContentJobInfo: JobInfo
    ) : BaseViewModel.Factory<ChangeEnvironmentViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChangeEnvironmentViewModel(
                viewState
                    ?: ChangeEnvironmentViewState(selectedEnvironment = environmentManager.environment()),
                environmentManager,
                customCredentialsManager,
                secretSettingsNavigator,
                jobScheduler,
                clearUserContentJobInfo
            ) as T
    }
}
