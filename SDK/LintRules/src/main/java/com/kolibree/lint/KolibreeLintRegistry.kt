package com.kolibree.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.kolibree.lint.rule.code.DeobfuscatedPublicSdkClassIssue
import com.kolibree.lint.rule.code.ExperimentalClassUseIssue
import com.kolibree.lint.rule.code.NavControllerNavigateIssue
import com.kolibree.lint.rule.code.RetrofitBodyWithoutSerializedNamesIssue
import com.kolibree.lint.rule.code.SdkPublicClassInNonKolibreePackageIssue
import com.kolibree.lint.rule.code.SdkPublicExtensionMethodWithoutKeepIssue
import com.kolibree.lint.rule.code.UnsafeCompositeDisposableIssue

/**
 * Registry of all Kolibree lint rules, that will be executed by lint gradle tasks
 */
@Suppress("all")
class KolibreeLintRegistry : IssueRegistry() {

    override val issues = listOf(
        // NOTE: this rule is kept only for demonstration purposes, so show how to write rules for XML files
        // TextAppearanceIssue.ISSUE
        UnsafeCompositeDisposableIssue.ISSUE,
        DeobfuscatedPublicSdkClassIssue.ISSUE,
        SdkPublicExtensionMethodWithoutKeepIssue.ISSUE,
        ExperimentalClassUseIssue.ISSUE,
        SdkPublicClassInNonKolibreePackageIssue.ISSUE,
        RetrofitBodyWithoutSerializedNamesIssue.ISSUE,
        NavControllerNavigateIssue.ISSUE
    )
}
