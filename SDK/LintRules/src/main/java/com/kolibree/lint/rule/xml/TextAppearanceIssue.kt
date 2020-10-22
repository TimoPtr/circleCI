package com.kolibree.lint.rule.xml

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Element

object TextAppearanceIssue {

    private const val ID = "MissingTextAppearance"
    private const val DESCRIPTION = "textAppearance attribute is missing"
    private const val EXPLANATION =
        "We should use textAppearance to style a TextView in order to provide consistent design"

    private val CATEGORY = Category.TYPOGRAPHY
    private const val PRIORITY = 4
    private val SEVERITY = Severity.WARNING

    val ISSUE = Issue.create(
        ID,
        DESCRIPTION,
        EXPLANATION,
        CATEGORY,
        PRIORITY,
        SEVERITY,
        Implementation(
            TextViewStyleDetector::class.java,
            Scope.RESOURCE_FILE_SCOPE)
    )

    private class TextViewStyleDetector : ResourceXmlDetector() {

        override fun getApplicableElements() = listOf(TEXTVIEW)

        override fun visitElement(context: XmlContext, element: Element) {
            if (!element.hasAttributeNS(SCHEMA, TEXT_APPEARANCE)) {
                context.report(ISSUE, context.getLocation(element), EXPLANATION)
            }
        }

        companion object {
            private const val SCHEMA = "http://schemas.android.com/apk/res/android"
            private const val TEXT_APPEARANCE = "textAppearance"
            private const val TEXTVIEW = "TextView"
        }
    }
}
