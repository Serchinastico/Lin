package com.serchinastico.lin.detectors

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class LinIssueRegistry : IssueRegistry() {
    override val api: Int
        get() = CURRENT_API

    override val minApi: Int
        get() = -1

    override val issues: List<Issue> = listOf(
        NoDataFrameworksFromAndroidClassDetector.issue,
        NoElseInSwitchWithEnumOrSealedDetector.issue,
        NoFindViewByIdCallsDetector.issue,
        NoMoreThanOneDateInstanceDetector.issue,
        NoMoreThanOneGsonInstanceDetector.issue,
        NoPrintStackTraceCallsDetector.issue,
        NoPublicViewPropertiesDetector.issue,
        NoSetOnClickListenerCallsDetector.issue,
        OnlyConstantsInTypeOrFileDetector.issue,
        WrongSyntheticViewReferenceDetector.issue
    )
}