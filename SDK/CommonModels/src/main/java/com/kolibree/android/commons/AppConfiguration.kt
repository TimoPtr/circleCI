/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons

import com.kolibree.android.annotation.VisibleForApp

/**
 * This configuration allow us to tweak the module based on the actual values
 * of all the fields given in the actual implementation.
 *
 * This should be injected in our dagger graph.
 *
 * Flags should not be confused with FeatureToggle, they should only be used
 * to allow discrepancy between apps.
 *
 * Before adding a flag you need to make sure that it makes sense, we want to have
 * a very few amount of flag to keep the maintenance cost acceptable.
 *
 * This approach allow to only have flavors at the app level and nothing at the sdk level.
 *
 * When adding a new flag please add link to confluence/jira to explain why we need it
 * and the flag is covering
 */
@VisibleForApp
interface AppConfiguration {
/*
    Tags should be simple boolean and name should be explicit as bellow , if one day we need more
    complex variable please check with the team first
    val isMultiBrushSupported: Boolean
*/
    /**
     * At sign up, show receive promotions and updates option for CC, but not for HUM.
     *
     * For details, please check this [JIRA Ticket](https://kolibree.atlassian.net/browse/KLTB002-12734)
     */
    val showPromotionsOptionAtSignUp: Boolean

    /**
     * On Hum we don't want to allow the user to disable the data sharing
     *
     * For details, please check this [JIRA Ticket](https://kolibree.atlassian.net/browse/KLTB002-12878)
     */
    val allowDisablingDataSharing: Boolean

    /**
     * This flag determines whether multi-profiles are supported or not
     */
    val isSelectProfileSupported: Boolean

    /**
     * Determines if battery level monitoring should be enabled
     */
    val isBatteryMonitoringEnabled: Boolean

    /**
     * Determines whether the Multi Toothbrush if enabled in the App
     */
    val isMultiToothbrushesPerProfileEnabled: Boolean

    /**
     * Determines whether we show Headspace-related content in the app
     */
    val showHeadspaceRelatedContent: Boolean

    /**
     * Determines if we show the Guided Brushing Tips in the app
     */
    val showGuidedBrushingTips: Boolean

    /**
     * Determines whether we show the Games card in the Activities tab
     */
    val showGamesCard: Boolean
}
