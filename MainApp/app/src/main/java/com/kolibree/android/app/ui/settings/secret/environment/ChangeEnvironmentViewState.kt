package com.kolibree.android.app.ui.settings.secret.environment

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.network.environment.Environment
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ChangeEnvironmentViewState(
    val selectedEnvironment: Environment,
    val customEndpointUrl: String? = "",
    val customClientId: String? = "",
    val customClientSecret: String? = ""
) : BaseViewState
