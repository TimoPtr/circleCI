/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils

import android.content.ClipData
import android.content.ClipboardManager
import com.kolibree.android.annotation.VisibleForApp
import javax.inject.Inject

@VisibleForApp
interface CopyToClipboardUseCase {
    fun copy(text: String, label: String = text)
}

/*
This class can't be tested easily

Android Q prints error

`Denying clipboard access to com.kolibree.android.headspace.test, application is not in focus
neither is a system service for user 0`

And it doesn't add value to mock static newPlainText
 */
internal class CopyToClipboardUseCaseImpl
@Inject constructor(private val clipboardManager: ClipboardManager) : CopyToClipboardUseCase {
    override fun copy(text: String, label: String) {
        val clip = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clip)
    }
}
