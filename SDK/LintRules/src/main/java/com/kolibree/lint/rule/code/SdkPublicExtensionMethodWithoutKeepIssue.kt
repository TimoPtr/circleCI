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
import com.kolibree.lint.extension.isExtensionFunction
import com.kolibree.lint.extension.isNotExposed
import com.kolibree.lint.extension.isRealFunction
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UastVisibility
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.getIoFile

object SdkPublicExtensionMethodWithoutKeepIssue {

    private const val ID = "SdkPublicExtensionMethodWithoutKeep"

    private const val DESCRIPTION = "Public SDK extension functions need to have `@Keep` annotation"
    private const val MESSAGE = "Please add `@Keep` to this extension function or make it non-public"
    private const val EXPLANATION = "Proguard struggles with obfuscation of Kotlin extension methods.\n\n" +
        "If you want to make sure nothing will happen with your code when " +
        "SDK consumers will use proguarded version of it, " +
        "please add `@Keep` annotation to this extension function.\n\n" +
        "Check if this function can be non-public (for ex. `internal`). " +
        "Also please remember that some extension functions have to be kept (for ex. binding adapters)."

    private val CATEGORY = Category.CORRECTNESS
    private const val PRIORITY = 10
    private val SEVERITY = Severity.ERROR

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

        override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UMethod::class.java)

        override fun createUastHandler(context: JavaContext): UElementHandler = NodeVisitor(context)

        internal class NodeVisitor(private val context: JavaContext) : UElementHandler() {

            @Suppress("ComplexCondition")
            override fun visitMethod(node: UMethod) {
                if (node.getContainingUFile()?.getIoFile()?.path?.contains("/SDK/") == true &&
                    node.isExtensionFunction() &&
                    node.visibility == UastVisibility.PUBLIC &&
                    !node.isNotExposed() &&
                    node.isRealFunction() &&
                    !node.hasKeep()
                ) {
                    reportAnIssue(node)
                }
            }

            private fun reportAnIssue(node: UMethod) {
                context.report(ISSUE, node, context.getNameLocation(node), MESSAGE)
            }
        }
    }
}
