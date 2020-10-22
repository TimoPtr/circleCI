/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.environment

import com.kolibree.android.app.ui.settings.secret.SecretSettingsBaseAction
import com.kolibree.android.network.environment.Environment

internal sealed class ChangeEnvironmentAction : SecretSettingsBaseAction

internal data class ShowConfirmChangeEnvironmentAction(val environment: Environment) : ChangeEnvironmentAction()

internal object ShowCustomEnvironmentMissingFieldErrorAction : ChangeEnvironmentAction()
internal object ShowCustomEnvironmentUrlExistsAction : ChangeEnvironmentAction()
internal object ShowCustomEnvironmentSomethingWrongAction : ChangeEnvironmentAction()
