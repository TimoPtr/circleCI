/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.dialogs

import com.kolibree.android.app.base.BaseAction

internal sealed class DialogsPlaygroundActions : BaseAction {
    object AlertWithStrings : DialogsPlaygroundActions()
    object AlertWithStringIds : DialogsPlaygroundActions()
    object TextInputDialog : DialogsPlaygroundActions()
    object AlertWithTintedFeatureImage : DialogsPlaygroundActions()
    object AlertWithFeatureImageId : DialogsPlaygroundActions()
    object AlertWithStylizedHeadline : DialogsPlaygroundActions()
    object SingleSelectWithButtons : DialogsPlaygroundActions()
    object SingleSelectWithoutButtons : DialogsPlaygroundActions()
    object MultiSelectWithStrings : DialogsPlaygroundActions()
    object DurationDialog : DialogsPlaygroundActions()
    object AlertWithIcon : DialogsPlaygroundActions()
    object Carousel : DialogsPlaygroundActions()
}
