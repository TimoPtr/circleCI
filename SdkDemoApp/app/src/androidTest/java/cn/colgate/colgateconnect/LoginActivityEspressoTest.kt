package cn.colgate.colgateconnect

import android.content.Intent
import cn.colgate.colgateconnect.login.LoginActivity
import com.kolibree.android.test.KLBaseActivityCommonEspressoTest
import com.kolibree.android.test.KLBaseActivityTestRule
import org.junit.Test

class LoginActivityEspressoTest : KLBaseActivityCommonEspressoTest<LoginActivity>() {
    @Test
    fun openLoginActivity() {
        activityTestRule.launchActivity(createLaunchMainActivityIntent())
    }

    override fun createRuleForActivity(): KLBaseActivityTestRule<LoginActivity> {
        return KLBaseActivityTestRule(LoginActivity::class.java, false)
    }

    private fun createLaunchMainActivityIntent(): Intent {
        return Intent()
    }
}
