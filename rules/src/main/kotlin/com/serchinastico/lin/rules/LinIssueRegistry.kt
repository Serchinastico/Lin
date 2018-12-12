package com.serchinastico.lin.rules

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue

class LinIssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = listOf(
        NoDataFrameworksFromAndroidClassDetector.issue,
        NoElseInSwitchWithEnumOrSealedDetector.issue,
        NoPrintStackTraceCallsDetector.issue,
        NoPublicViewPropertiesDetector.issue,
        NoSetOnClickListenerCallsDetector.issue
    )
}