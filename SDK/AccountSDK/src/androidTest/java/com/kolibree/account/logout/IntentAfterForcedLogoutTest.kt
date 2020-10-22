package com.kolibree.account.logout

import android.app.Activity
import android.content.Context
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntentAfterForcedLogoutTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun create_returnsIntentWithExpectedFlags() {
        val intent = IntentAfterForcedLogout.create(context(), DummyActivity::class.java)

        assertEquals(FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK, intent.flags)
    }

    class DummyActivity : Activity()
}
