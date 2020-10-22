package com.kolibree.android.tracker

import androidx.annotation.Keep

/**
 * Some fragments should not be tracked in {code}Fragment#onResume{code} method e.g. fragments
 * inside ViewPager object are created before they are visible. In such cases, this interface can be
 * used.
 *
 * Created by Kornel on 3/16/2018.
 */
@Keep
interface NonTrackableScreen
