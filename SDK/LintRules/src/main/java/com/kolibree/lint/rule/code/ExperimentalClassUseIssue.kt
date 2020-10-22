/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.lint.rule.code

import com.android.SdkConstants
import com.android.tools.lint.checks.AnnotationDetector
import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.UastLintUtils
import com.android.tools.lint.detector.api.isKotlin
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiCompiledElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.UTypeReferenceExpression
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.util.isArrayInitializer

/**
 * Sorry for such a messy codebase... It is based on [RestrictToDetector]
 * @author lookash
 */
@Suppress("LongParameterList", "LargeClass", "ComplexMethod", "ComplexMethod", "ReturnCount", "NestedBlockDepth")
object ExperimentalClassUseIssue {

    private const val ID = "ExperimentalClassUse"

    private const val DESCRIPTION = "This is an experimental component and is not ready for use by wider audience."
    private const val MESSAGE = "Please use this class only when you know what you're doing or contact the author."
    private const val EXPLANATION = "This is an experimental component and is not ready for use by wider audience."

    private val CATEGORY = Category.CORRECTNESS
    private const val PRIORITY = 10
    private val SEVERITY = Severity.WARNING

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

    private const val EXPERIMENTAL_ANNOTATION = "com.kolibree.android.KolibreeExperimental"

    internal class IssueDetector : Detector(), Detector.UastScanner {

        override fun applicableAnnotations(): List<String> = listOf(EXPERIMENTAL_ANNOTATION)

        override fun inheritAnnotation(annotation: String): Boolean {
            return true
        }

        override fun visitAnnotationUsage(
            context: JavaContext,
            usage: UElement,
            type: AnnotationUsageType,
            annotation: UAnnotation,
            qualifiedName: String,
            method: PsiMethod?,
            referenced: PsiElement?,
            annotations: List<UAnnotation>,
            allMemberAnnotations: List<UAnnotation>,
            allClassAnnotations: List<UAnnotation>,
            allPackageAnnotations: List<UAnnotation>
        ) {
            if (type == AnnotationUsageType.EXTENDS && usage is UTypeReferenceExpression) {
                val sourcePsi = usage.sourcePsi
                if (isKotlin(sourcePsi) && sourcePsi?.parent?.toString() == "CONSTRUCTOR_CALLEE") {
                    return
                }
            }

            val member = method ?: referenced as? PsiMember
            when (qualifiedName) {
                EXPERIMENTAL_ANNOTATION -> {
                    if (member != null) {
                        checkIfApplicable(
                            context,
                            usage,
                            member,
                            annotation,
                            allMemberAnnotations,
                            allClassAnnotations
                        )
                    }
                }
            }
        }

        private fun checkIfApplicable(
            context: JavaContext,
            node: UElement,
            method: PsiMember,
            annotation: UAnnotation,
            allMethodAnnotations: List<UAnnotation>,
            allClassAnnotations: List<UAnnotation>
        ) {

            val visibility = getVisibilityForTesting(annotation)
            if (visibility == VISIBILITY_NONE) { // not the default
                checkRestrictTo(
                    context, node, method, annotation, allMethodAnnotations,
                    allClassAnnotations, RESTRICT_TO_TESTS
                )
            } else {
                // Check that the target method is available
                // (1) private is available in the same compilation unit
                // (2) package private is available in the same package
                // (3) protected is available either from subclasses or in same package

                val uFile = node.getContainingUFile()
                val containingFile1 = UastLintUtils.getPsiFile(uFile)
                val containingFile2 = UastLintUtils.getContainingFile(method)
                if (containingFile1 == containingFile2 || containingFile2 == null) {
                    // Same compilation unit
                    return
                }

                // Sanity check (since Kotlin UAST creates several light classes around
                // PSI files that sometimes fail equality tests)
                if (containingFile1?.virtualFile == containingFile2.virtualFile) {
                    return
                }

                if (visibility == VISIBILITY_PRIVATE) {
                    reportError(context, node, "private")
                    return
                }

                val evaluator = context.evaluator
                val pkg = evaluator.getPackage(node)
                val methodPackage = evaluator.getPackage(method)
                if (pkg == methodPackage) {
                    // Same package
                    return
                }
                if (visibility == VISIBILITY_PACKAGE_PRIVATE) {
                    reportError(context, node, "package private")
                    return
                }

                assert(visibility == VISIBILITY_PROTECTED)

                val methodClass = method.containingClass
                val thisClass = node.getParentOfType<UClass>(UClass::class.java, true)
                if (thisClass == null || methodClass == null) {
                    return
                }
                val qualifiedName = methodClass.qualifiedName
                if (qualifiedName == null || evaluator.inheritsFrom(
                        thisClass,
                        qualifiedName,
                        false
                    )
                ) {
                    return
                }

                reportError(context, node, "protected")
            }
        }

        private fun reportError(
            context: JavaContext,
            node: UElement,
            desc: String
        ) {
            val location: Location = if (node is UCallExpression) {
                context.getCallLocation(node, false, false)
            } else {
                context.getLocation(node)
            }

            context.report(ISSUE, node, location, MESSAGE)
        }

        private fun checkRestrictTo(
            context: JavaContext,
            node: UElement,
            member: PsiMember?,
            annotation: UAnnotation,
            allMethodAnnotations: List<UAnnotation>,
            allClassAnnotations: List<UAnnotation>,
            scope: Int,
            applyClassAnnotationsToMembers: Boolean = true
        ) {

            val containingClass = when {
                node is UTypeReferenceExpression -> PsiTypesUtil.getPsiClass(node.type)
                member != null -> member.containingClass
                node is UCallExpression -> node.classReference?.resolve() as PsiClass?
                node is PsiClass -> node
                else -> null
            }

            containingClass ?: return

            var isClassAnnotation = false
            if (UastLintUtils.containsAnnotation(allMethodAnnotations, annotation)) {
                // Make sure that the annotation is *not* inherited.
                // For example, NavigationView (a public, exposed class) extends ScrimInsetsFrameLayout, which
                // is a restricted class. We don't want to make all uses of NavigationView to suddenly be
                // treated as Restricted just because it inherits code from a restricted API.
                if (member != null && context.evaluator.isInherited(annotation, member)) {
                    return
                }
            } else if (applyClassAnnotationsToMembers) {
                // Found restriction or class or package: make sure we only check on the most
                // specific scope, otherwise we report the same error multiple times
                // or report errors on restrictions that have been redefined
                if (containsRestrictionAnnotation(allMethodAnnotations)) {
                    return
                }
                isClassAnnotation = UastLintUtils.containsAnnotation(allClassAnnotations, annotation)
                if (isClassAnnotation) {
                    if (context.evaluator.isInherited(annotation, containingClass)) {
                        return
                    }
                } else if (containsRestrictionAnnotation(allClassAnnotations)) {
                    return
                }
            } else { // not in member annotations and applyClassAnnotationToMembers is false.
                return
            }

            if (scope and RESTRICT_TO_LIBRARY_GROUP != 0 && member != null) {
                val evaluator = context.evaluator
                val thisCoordinates = evaluator.getLibrary(node) ?: context.project.mavenCoordinates
                val methodCoordinates = evaluator.getLibrary(member) ?: run {
                    if (thisCoordinates != null && member !is PsiCompiledElement) {
                        // Local source?
                        context.evaluator.getProject(member)?.mavenCoordinates
                    } else {
                        null
                    }
                }
                val thisGroup = thisCoordinates?.groupId
                val methodGroup = methodCoordinates?.groupId
                if (thisGroup != methodGroup && methodGroup != null) {
                    val where = String.format(
                        "from within the same library group (groupId=%1\$s)",
                        methodGroup
                    )
                    reportRestriction(
                        where, containingClass, member, context,
                        node, isClassAnnotation
                    )
                }
            }

            if (scope and RESTRICT_TO_LIBRARY != 0 && member != null) {
                val evaluator = context.evaluator
                val thisCoordinates = evaluator.getLibrary(node) ?: context.project.mavenCoordinates
                val methodCoordinates = evaluator.getLibrary(member)
                val thisGroup = thisCoordinates?.groupId
                val methodGroup = methodCoordinates?.groupId
                if (thisGroup != methodGroup && methodGroup != null) {
                    val thisArtifact = thisCoordinates?.artifactId
                    val methodArtifact = methodCoordinates.artifactId
                    if (thisArtifact != methodArtifact) {
                        val where = String.format(
                            "from within the same library (%1\$s:%2\$s)",
                            methodGroup,
                            methodArtifact
                        )
                        reportRestriction(
                            where, containingClass, member, context,
                            node, isClassAnnotation
                        )
                    }
                } else if (member !is PsiCompiledElement) {
                    // If the resolved method is source, make sure they're part
                    // of the same Gradle project
                    val project = context.evaluator.getProject(member)
                    if (project != null && project != context.project) {
                        val coordinates = project.mavenCoordinates
                        val name = if (coordinates != null) {
                            "${coordinates.groupId}:${coordinates.artifactId}"
                        } else {
                            project.name
                        }
                        val where = "from within the same library ($name)"
                        reportRestriction(
                            where, containingClass, member, context,
                            node, isClassAnnotation
                        )
                    }
                }
            }

            if (scope and RESTRICT_TO_TESTS != 0) {
                reportRestriction(
                    "from tests", containingClass, member, context,
                    node, isClassAnnotation
                )
            }

            if (scope and RESTRICT_TO_SUBCLASSES != 0) {
                val qualifiedName = containingClass.qualifiedName
                if (qualifiedName != null) {
                    val evaluator = context.evaluator

                    var outer: UClass?
                    var isSubClass = false
                    var prev = node

                    while (true) {
                        outer = prev.getParentOfType(UClass::class.java, true)
                        if (outer == null) {
                            break
                        }
                        if (evaluator.inheritsFrom(outer, qualifiedName, false)) {
                            isSubClass = true
                            break
                        }

                        if (evaluator.isStatic(outer)) {
                            break
                        }
                        prev = outer
                    }

                    if (!isSubClass) {
                        reportRestriction(
                            "from subclasses", containingClass, member,
                            context, node, isClassAnnotation
                        )
                    }
                }
            }
        }

        private fun reportRestriction(
            where: String?,
            containingClass: PsiClass,
            member: PsiMember?,
            context: JavaContext,
            node: UElement,
            isClassAnnotation: Boolean
        ) {
            var api: String
            api = if (member == null || member is PsiMethod && member.isConstructor) {
                member?.name ?: containingClass.name + " constructor"
            } else if (containingClass == member) {
                member.name ?: "class"
            } else {
                containingClass.name + "." + member.name
            }

            var locationNode = node
            if (node is UCallExpression) {
                val nameElement = node.methodIdentifier
                if (nameElement != null) {
                    locationNode = nameElement
                }

                // If the annotation was reported on the class, and the left hand side
                // expression is that class, use it as the name node?
                if (isClassAnnotation) {
                    val qualifier = node.receiver
                    val className = containingClass.name
                    if (qualifier != null && className != null && qualifier.asSourceString() == className) {
                        locationNode = qualifier
                        api = className
                    }
                }
            }

            val location: Location
            location = if (locationNode is UCallExpression) {
                context.getCallLocation(locationNode, false, false)
            } else {
                context.getLocation(locationNode)
            }
            context.report(
                ISSUE, node, location,
                MESSAGE
            )
        }

        private fun containsRestrictionAnnotation(list: List<UAnnotation>): Boolean {
            return UastLintUtils.containsAnnotation(
                list,
                AnnotationDetector.RESTRICT_TO_ANNOTATION.oldName()
            ) || UastLintUtils.containsAnnotation(list, AnnotationDetector.RESTRICT_TO_ANNOTATION.newName())
        }

        companion object {
            private const val ATTR_OTHERWISE = "otherwise"
            private const val ATTR_PRODUCTION_VISIBILITY = "productionVisibility"

            // Must match constants in @VisibleForTesting:
            private const val VISIBILITY_PRIVATE = 2
            private const val VISIBILITY_PACKAGE_PRIVATE = 3
            private const val VISIBILITY_PROTECTED = 4
            private const val VISIBILITY_NONE = 5
            // TODO Kotlin "module" visibility

            private fun getVisibilityForTesting(annotation: UAnnotation): Int {
                val value = annotation.findDeclaredAttributeValue(ATTR_OTHERWISE)
                // Guava within Google3:
                    ?: annotation.findDeclaredAttributeValue(ATTR_PRODUCTION_VISIBILITY)
                if (value is ULiteralExpression) {
                    val v = value.value
                    if (v is Int) {
                        return (v as Int?)!!
                    }
                } else if (value is UReferenceExpression) {
                    // Not compiled; this is unlikely (but can happen when editing the support
                    // library project itself)
                    val name = value.resolvedName
                    when (name) {
                        "NONE" -> return VISIBILITY_NONE
                        "PRIVATE" -> return VISIBILITY_PRIVATE
                        "PROTECTED" -> return VISIBILITY_PROTECTED
                        "PACKAGE_PRIVATE" -> return VISIBILITY_PACKAGE_PRIVATE
                    }
                }

                return VISIBILITY_PRIVATE // the default
            }

            /** `RestrictTo(RestrictTo.Scope.GROUP_ID`  */
            private const val RESTRICT_TO_LIBRARY_GROUP = 1 shl 0
            /** `RestrictTo(RestrictTo.Scope.GROUP_ID`  */
            private const val RESTRICT_TO_LIBRARY = 1 shl 1
            /** `RestrictTo(RestrictTo.Scope.TESTS`  */
            private const val RESTRICT_TO_TESTS = 1 shl 2
            /** `RestrictTo(RestrictTo.Scope.SUBCLASSES`  */
            private const val RESTRICT_TO_SUBCLASSES = 1 shl 3
            private const val RESTRICT_TO_ALL = 1 shl 4

            private fun getRestrictionScope(annotation: UAnnotation): Int {
                val value = annotation.findDeclaredAttributeValue(SdkConstants.ATTR_VALUE)
                if (value != null) {
                    return getRestrictionScope(value)
                } else if (AnnotationDetector.GMS_HIDE_ANNOTATION == annotation.qualifiedName) {
                    return RESTRICT_TO_ALL
                }
                return 0
            }

            private fun getRestrictionScope(expression: UExpression?): Int {
                var scope = 0
                if (expression != null) {
                    if (expression.isArrayInitializer()) {
                        val initializerExpression = expression as UCallExpression?
                        val initializers = initializerExpression!!.valueArguments
                        for (initializer in initializers) {
                            scope = scope or getRestrictionScope(initializer)
                        }
                    } else if (expression is UReferenceExpression) {
                        val resolved = expression.resolve()
                        if (resolved is PsiField) {
                            val name = resolved.name
                            if ("GROUP_ID" == name || "LIBRARY_GROUP" == name) {
                                scope = scope or RESTRICT_TO_LIBRARY_GROUP
                            } else if ("SUBCLASSES" == name) {
                                scope = scope or RESTRICT_TO_SUBCLASSES
                            } else if ("TESTS" == name) {
                                scope = scope or RESTRICT_TO_TESTS
                            } else if ("LIBRARY" == name) {
                                scope = scope or RESTRICT_TO_LIBRARY
                            }
                        }
                    }
                }

                return scope
            }
        }
    }
}
