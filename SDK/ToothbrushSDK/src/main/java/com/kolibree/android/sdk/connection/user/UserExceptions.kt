package com.kolibree.android.sdk.connection.user

import androidx.annotation.Keep
import kotlin.Exception

/** Exception thrown when the profile ID is queried when the toothbrush is in shared mode */
@Keep
class ToothbrushInSharedModeException : Exception("Toothbrush in shared mode, no profile ID is set")

/** Exception thrown when trying to enable shared mode on non shareable toothbrushes */
@Keep
class ToothbrushNotShareableException : Exception("This toothbrush model is not shareable")
