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
import com.kolibree.lint.extension.isDaggerRelated
import com.kolibree.lint.extension.isExposed
import com.kolibree.lint.extension.parentHasKeep
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.getIoFile

object SdkPublicClassInNonKolibreePackageIssue {

    private const val ID = "SdkPublicClassInNonKolibreePackage"

    private const val DESCRIPTION = "Public SDK class is not in `com.kolibree.*` package."
    private const val MESSAGE =
        "Please make sure this class is in `com.kolibree.*` package, add `@Keep` or change its visibility."
    private const val EXPLANATION = "Our proguard config keeps all com.kolibree.* classes by default:\n\n" +
        "```" +
        "# Keep public Kolibree classes and interfaces\n" +
        "-keep, includedescriptorclasses public class com.kolibree.** {\n" +
        "    public <fields>;\n" +
        "    protected <fields>;\n" +
        "    public <methods>;\n" +
        "    protected <methods>;\n" +
        " }\n" +
        "\n" +
        " -keep public interface com.kolibree.**  {*;}" +
        "```\n" +
        "\n" +
        "If you have a class or interface in different package, it won't be kept by this rule.\n" +
        "This may lead to crashes on SDK consumer side."

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

        override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UClass::class.java)

        override fun createUastHandler(context: JavaContext): UElementHandler = NodeVisitor(context)

        internal class NodeVisitor(private val context: JavaContext) : UElementHandler() {

            @Suppress("ComplexCondition")
            override fun visitClass(node: UClass) {
                if (node.getContainingUFile()?.getIoFile()?.path?.contains("/SDK/") == true &&
                    node.isExposed() &&
                    !node.isDaggerRelated() &&
                    !node.hasKeep() &&
                    !node.parentHasKeep() &&
                    node.qualifiedName?.startsWith("com.kolibree") == false
                ) {
                    val location = context.getNameLocation(node)
                    if (location.start != null && location.end != null) {
                        context.report(ISSUE, node, location, MESSAGE)
                    }
                }
            }
        }
    }
}
