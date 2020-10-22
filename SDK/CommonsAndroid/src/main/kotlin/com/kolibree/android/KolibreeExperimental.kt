/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android

import android.annotation.SuppressLint

/**
 * This annotation marks components that are not yet ready to be used by wider audience.
 * You need to be 100% sure what you're doing when dealing with [KolibreeExperimental] classes.
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class KolibreeExperimental
