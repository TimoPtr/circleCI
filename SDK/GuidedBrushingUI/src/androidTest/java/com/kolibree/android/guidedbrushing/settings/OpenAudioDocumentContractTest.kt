/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

internal class OpenAudioDocumentContractTest : BaseInstrumentationTest() {

    override fun context(): Context =
        InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun creating_an_intent_add_the_rights_flags() {
        val contract = OpenAudioDocumentContract()

        val intent = contract.createIntent(context(), Unit)

        assertEquals(Intent.ACTION_OPEN_DOCUMENT, intent.action)
        assertEquals("audio/*", intent.type)
        assertEquals(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
            intent.flags
        )
    }

    @Test
    fun when_activity_result_is_not_ok_returns_null() {
        val contract = OpenAudioDocumentContract()

        assertNull(contract.parseResult(Activity.RESULT_CANCELED, null))
    }

    @Test
    fun when_activity_result_is_ok_returns_uri() {
        val contract = OpenAudioDocumentContract()
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        assertEquals(intent.data, contract.parseResult(Activity.RESULT_OK, intent))
    }
}
