package com.kolibree.android.commons

import com.kolibree.sdkws.brushing.wrapper.IBrushing

const val DEFAULT_BRUSHING_GOAL = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS

const val MIN_BRUSHING_DURATION_SECONDS = 10L

const val RAW_DATA_WINDOW_SIZE = 25 // TODO maybe move this into KML

/**
 * ... with 80% (or more) surface coverage for a given day.
 *
 * @see [https://kolibree.atlassian.net/wiki/spaces/PROD/pages/2736003/Calendar]
 */
const val MINIMAL_AVERAGE_SURFACE_PER_BRUSHING_FOR_PERFECTION = 80
