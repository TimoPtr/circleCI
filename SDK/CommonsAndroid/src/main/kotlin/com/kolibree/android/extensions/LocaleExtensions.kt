package com.kolibree.android.extensions

import java.util.Locale

@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun getCountry(): String = Locale.getDefault().country

/**
 * Get the system default country code
 *
 * @return A String representation of the default country code
 */
@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun getLanguage(): String =
    Locale.getDefault().language.substring(0, 2).toLowerCase(Locale.getDefault())

@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun getAcceptedLanguageHeader(): String = Locale.getDefault().toLanguageTag()
