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
import com.intellij.psi.PsiParameter
import com.kolibree.lint.extension.declarationContainsKeyword
import com.kolibree.lint.extension.hasKeep
import com.kolibree.lint.model.ComparableLocation
import org.jetbrains.kotlin.asJava.elements.KtLightParameter
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UField
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.toUElement
import toKolibreeClass

object RetrofitBodyWithoutSerializedNamesIssue {

    private const val ID = "RetrofitBodyWithoutSerializedNames"

    private const val DESCRIPTION =
        "Field without `@SerializedName` in class used as request `@Body`"
    private const val MESSAGE =
        "If this field is a part of JSON body, make sure to add `@SerializedName` annotation " +
            "to it or add `@Keep` to the whole class. If it is not - declare it as `@Transient`."
    private const val EXPLANATION =
        "We use proguard/R8 to obfuscate our code. This means that class and field names will be " +
            "removed during obfuscation process. And since GSON uses field name by default, " +
            "without proper annotations it won't be able do find matching field names for your JSON, " +
            "causing hard-to-track bugs.\n" +
            "\n" +
            "Valid cases:\n" +
            "\n" +
            "```\n" +
            "//Field annotated with @SerializedName\n" +
            "data class Structure(\n" +
            "    @SerializedName(\"fieldName\")\n" +
            "    private val field: String\n" +
            ")\n" +
            "```\n" +
            "\n" +
            "```\n" +
            "//Class annotated with @Keep\n" +
            "@Keep\n" +
            "data class Structure(\n" +
            "    private val field: String\n" +
            ")\n" +
            "```\n" +
            "\n" +
            "```\n" +
            "//@Transient field - if it's not a part of JSON body\n" +
            "data class Structure(\n" +
            "    @Transient\n" +
            "    private val fieldNotUsedInJson: String\n" +
            ")\n" +
            "```\n"

    private val CATEGORY = Category.CORRECTNESS
    private const val PRIORITY = 10
    private val SEVERITY = Severity.ERROR

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

        override fun getApplicableUastTypes(): List<Class<out UElement>> =
            listOf(UMethod::class.java)

        override fun createUastHandler(context: JavaContext): UElementHandler = NodeVisitor(context)

        internal class NodeVisitor(private val context: JavaContext) : UElementHandler() {

            private val reportedLocations: MutableList<ComparableLocation> = mutableListOf()

            override fun visitMethod(node: UMethod) {
                node.isQualifiedRetrofitApi()
                    ?.hasBodyParameter()
                    ?.findMatchingKolibreeClass()
                    ?.notKept()
                    ?.findIncorrectlyConfiguredFields()
                    ?.forEach { field -> reportDistinct(field) }
            }

            private fun reportDistinct(field: UField) {
                val location = context.getNameLocation(field)
                val comparableLocation =
                    ComparableLocation(location)
                if (reportedLocations.find { it.isEqualTo(comparableLocation) } != null) return

                context.report(ISSUE, field, location, MESSAGE)
                reportedLocations.add(comparableLocation)
            }
        }
    }
}

private fun UMethod.isQualifiedRetrofitApi(): UMethod? = takeIf { method ->
    method.hasAnnotation("retrofit2.http.POST") ||
        method.hasAnnotation("retrofit2.http.PATCH") ||
        method.hasAnnotation("retrofit2.http.PUT")
}

private fun UMethod.hasBodyParameter(): PsiParameter? = parameterList.parameters
    .firstOrNull { parameter -> parameter.hasAnnotation("retrofit2.http.Body") }

private fun PsiParameter?.findMatchingKolibreeClass(): UClass? =
    if (this is KtLightParameter) {
        val parameter: UParameter? = toUElement() as UParameter?
        parameter?.toKolibreeClass()
    } else null

private fun UClass.notKept(): UClass? = takeIf { it.hasKeep().not() }

private fun UClass.findIncorrectlyConfiguredFields(): List<UField> = fields.filter { field ->
    !field.hasAnnotation("com.google.gson.annotations.SerializedName") &&
        !field.isStatic &&
        !field.hasAnnotation("kotlin.jvm.Transient") &&
        !field.hasAnnotation("androidx.room.Ignore") &&
        !field.declarationContainsKeyword("transient")
}
