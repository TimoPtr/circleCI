/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * WARNING: This package should never change or you will have to update the DeobfuscatedPublicSdkClassIssue in the
 * lint module and it's the same for the annotation name
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */
package com.kolibree.android.annotation

/**
 * This annotation is used to tell when a resource (class, extension, object, function) has to be public to be consumed
 * by an Application that builds the module.
 *
 * Use @Keep if it should also be visible for consumers of the module in packaged format (.aar, maven)
 *
 * see for more details:
 * https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755140/Android+Best+Practices#AndroidBestPractices-PrivateoverPublic,VisibleForAppoverKeep
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.LOCAL_VARIABLE, AnnotationTarget.FIELD)
annotation class VisibleForApp
