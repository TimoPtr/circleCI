package com.kolibree.android

import androidx.annotation.VisibleForTesting

/** Kolibree cross modules constant definitions - don't add @Keep */
/*
 * 23/10/17 - maragues
 *
 * <p>User Id that identifies a toothbrush in multi user mode.
 *
 * <p>This hack is introduced as part of Family Mode feature
 * http://jira.kolibree.com/browse/KLTB002-1210
 *
 * <p>We want to store offline brushings even if the user is in multimode, but currently a
 * multi user Ara doesn't store offline brushings due to firmware design. For some reason,
 * we decided to go this way instead of updating the firmware.
 *
 * <p>So, if we want to activate multi user mode or check if the toothbrush is in multiuser
 * mode, this is the user id to assign.
 *
 * <p>We have checked and it's not an active account, so there shouldn't be issues
 */
@VisibleForTesting
const val SHARED_MODE_PROFILE_ID = 1L

/**
 * kolibree's day start at 4am, for historical reasons
 */
const val KOLIBREE_DAY_START_HOUR = 4
