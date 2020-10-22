package com.kolibree.lint.rule.code

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

object UnsafeCompositeDisposableIssue {

    private const val ID = "UnsafeCompositeDisposableIssue"
    private const val CLASS_NAME = "io.reactivex.disposables.CompositeDisposable"
    private const val METHOD_NAME = "add"

    private const val DESCRIPTION = "Disposable added to composite in an unsafe way"
    private const val EXPLANATION = "To make sure disposables are added to the composite in a safe way, please use:\n" +
        "\n" +
        "Java:\n" +
        "\t`DisposableUtils.addSafely(composite, <your disposable>);`\n" +
        "\n" +
        "Kotlin:\n" +
        "\t`composite.addSafely(<your disposable>)`\n" +
        "or\n" +
        "\t`composite += <your disposable>`."

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
            Scope.JAVA_FILE_SCOPE)
    )

    class IssueDetector : Detector(), Detector.UastScanner {

        override fun getApplicableMethodNames() = listOf(METHOD_NAME)

        override fun visitMethodCall(
            context: JavaContext,
            node: UCallExpression,
            method: PsiMethod
        ) {
            val evaluator = context.evaluator
            if (!evaluator.isMemberInClass(method, CLASS_NAME)) {
                return
            }
            if (getApplicableMethodNames().contains(method.name)) {
                val location = context.getLocation(node)
                context.report(ISSUE, node, location, EXPLANATION)
            }
        }
    }
}
