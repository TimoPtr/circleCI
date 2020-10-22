import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.kolibree.lint.rule.code.NavControllerNavigateIssue
import org.junit.Test

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

@Suppress("UnstableApiUsage")
internal class NavControllerNavigateIssueTest : LintDetectorTest() {

    override fun getDetector(): Detector {
        return NavControllerNavigateIssue.IssueDetector()
    }

    override fun getIssues(): List<Issue> {
        return listOf(NavControllerNavigateIssue.ISSUE)
    }

    private val testNavController = """
        package androidx.navigation

        class NavController {
            fun navigate(res: Int) = Unit
        }
    """.trimIndent()

    @Test
    fun `test detects unsafe navigator usages`() {
        val testFile = """
            import androidx.navigation.NavController
            
            val navController: NavController = TODO()
            
            fun test() = navController.navigate(0)
        """.trimIndent()

        lint().files(kotlin(testFile), kotlin(testNavController))
            .run()
            .expectMatches(NavControllerNavigateIssue.DESCRIPTION)
    }

    @Test
    fun `test allows other navigate methods`() {
        val testFile = """
            import androidx.navigation.NavController

            fun test() = navigate(0)
            fun navigate(res: Int) = Unit
        """.trimIndent()

        lint().files(kotlin(testFile), kotlin(testNavController))
            .run()
            .expectErrorCount(0)
    }
}
