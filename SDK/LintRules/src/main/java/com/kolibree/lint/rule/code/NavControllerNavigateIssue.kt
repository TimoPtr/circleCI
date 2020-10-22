/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.lint.rule.code

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

@Suppress("UnstableApiUsage")
internal object NavControllerNavigateIssue {

    private const val NAV_CONTROLLER_CLASS_NAME = "androidx.navigation.NavController"
    private const val NAVIGATE_METHOD_NAME = "navigate"
    private const val PRIORITY = 10
    private const val EXPLANATION = """
        When trying to navigate to the same destiny as the current one NavController
        can throw an exception. To make sure that it won't happen we need to check
        what's the current destination and compare it with the new one.
        
        Additionally NavController::navigate requires the main thread 
        and NavigateSafe can be executed from any thread. 
    """
    const val DESCRIPTION =
        "Unsafe usage of NavController::navigate. Please use navigateSafe instead."

    val ISSUE = Issue.create(
        "UnsafeNavigate",
        DESCRIPTION,
        EXPLANATION,
        Category.CORRECTNESS,
        PRIORITY,
        Severity.ERROR,
        Implementation(
            IssueDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )
    )

    internal class IssueDetector : Detector(), SourceCodeScanner {

        override fun getApplicableMethodNames() = listOf(NAVIGATE_METHOD_NAME)

        override fun visitMethodCall(
            context: JavaContext,
            node: UCallExpression,
            method: PsiMethod
        ) {
            super.visitMethodCall(context, node, method)
            if (context.isFromNavController(method)) {
                context.report(ISSUE, node, context.getNameLocation(node), DESCRIPTION)
            }
        }

        private fun JavaContext.isFromNavController(method: PsiMethod): Boolean {
            return evaluator.isMemberInClass(method, NAV_CONTROLLER_CLASS_NAME)
        }
    }
}
