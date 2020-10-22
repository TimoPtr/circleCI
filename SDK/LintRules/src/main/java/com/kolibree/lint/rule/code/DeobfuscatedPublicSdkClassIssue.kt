/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.lint.rule.code

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.kolibree.lint.extension.hasKeep
import com.kolibree.lint.extension.hasVisibleForApp
import com.kolibree.lint.extension.isDaggerRelated
import com.kolibree.lint.extension.isExposed
import com.kolibree.lint.extension.isParentInternal
import com.kolibree.lint.extension.parentHasKeep
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UastVisibility
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.getIoFile
import org.jetbrains.uast.java.JavaUAnonymousClass
import org.jetbrains.uast.kotlin.KotlinUAnonymousClass

object DeobfuscatedPublicSdkClassIssue {

    private const val ID = "DeobfuscatedPublicSdkClass"

    private const val DESCRIPTION = "Public classes and interfaces are not proguarded"
    private const val MESSAGE = "Please make it `internal`, or add `@Keep` if you want to keep it public " +
        "or `@VisibleForApp` if you want to keep it public for the app only"
    private const val EXPLANATION = "\n" +
        "Public classes and interfaces in the SDK will not be proguarded during release.\n" +
        "Please be careful with exposing things that should remain obfuscated.\n" +
        "Safest way to fix this issue is to declare the class/interface as `internal`."

    private val CATEGORY = Category.SECURITY
    private const val PRIORITY = 5
    private val SEVERITY = Severity.WARNING

    @JvmField
    val ISSUE = Issue.create(
        ID,
        DESCRIPTION,
        EXPLANATION,
        CATEGORY,
        PRIORITY,
        SEVERITY,
        Implementation(
            IssueDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )
    )

    internal class IssueDetector : Detector(), Detector.UastScanner {

        override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UClass::class.java)

        override fun createUastHandler(context: JavaContext): UElementHandler = NodeVisitor(context)

        internal class NodeVisitor(private val context: JavaContext) : UElementHandler() {

            @Suppress("ComplexCondition")
            override fun visitClass(node: UClass) {
                if (shouldBeSkipped(node)) {
                    // no-op
                } else if (shouldReportAnIssue(node)) {
                    reportAnIssue(node)
                }
            }

            private fun reportAnIssue(node: UClass) {
                context.report(ISSUE, node, context.getNameLocation(node), MESSAGE)
            }

            private fun shouldReportAnIssue(node: UClass): Boolean =
                when (node.visibility) {
                UastVisibility.PROTECTED -> {
                    isProtectedIllegal(node)
                }
                UastVisibility.PUBLIC -> {
                    isPublicIllegal(node)
                }
                else -> {
                    false
                }
            }

            private fun shouldBeSkipped(node: UClass) = when {
                node.getContainingUFile()?.getIoFile()?.path?.contains("/SDK/") == false -> true
                node is KotlinUAnonymousClass || node is JavaUAnonymousClass -> true
                node.originalElement == null || node.originalElement?.text.isNullOrBlank() -> true
                node.isDaggerRelated() -> true
                else -> false
            }

            private fun isProtectedIllegal(node: UClass): Boolean = !node.hasKeep() &&
                !node.hasVisibleForApp()

            private fun isPublicIllegal(node: UClass): Boolean = node.isExposed() &&
                !node.isParentInternal() &&
                !node.parentHasKeep() &&
                !node.hasKeep() &&
                !node.hasVisibleForApp()
        }
    }
}
