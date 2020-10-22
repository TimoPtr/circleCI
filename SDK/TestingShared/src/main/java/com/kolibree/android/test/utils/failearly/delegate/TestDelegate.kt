package com.kolibree.android.test.utils.failearly.delegate

import androidx.annotation.VisibleForTesting
import com.kolibree.android.failearly.FailEarlyDelegate

/**
 * Created by lookashc on 13/02/19.
 *
 * Default FailEarly delegate for all kinds of tests.
 */
@VisibleForTesting
object TestDelegate : FailEarlyDelegate {

    override fun invoke(error: Throwable, fallback: () -> Unit) {
        assert(false, object : () -> Any {
            override fun invoke(): Any {
                return "\n" +
                    "FailEarly TestDelegate was invoked!\n" +
                    "Please check the stacktrace below to find the root cause.\n" +
                    "\n" +
                    "If you need to override this behaviour in this test, " +
                    "please put this in the @Before section:\n" +
                    "\n" +
                    "   FailEarly.overrideDelegateWith(NoopTestDelegate)\n" +
                    "\n" +
                    "and in @After section:\n" +
                    "\n" +
                    "   FailEarly.overrideDelegateWith(TestDelegate)" +
                    "\n"
            }
        })
    }
}
