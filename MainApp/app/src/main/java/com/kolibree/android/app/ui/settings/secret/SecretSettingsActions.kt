/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret

import com.kolibree.android.app.base.BaseAction

interface SecretSettingsBaseAction : BaseAction
internal sealed class SecretSettingsAction : SecretSettingsBaseAction

internal data class ShowFeatureEditDialog(
    val descriptor: FeatureToggleDescriptor<*>
) : SecretSettingsAction()

internal object ShowAppRestartAction : SecretSettingsAction()

internal object ShowOperationFailedAction : SecretSettingsAction()
