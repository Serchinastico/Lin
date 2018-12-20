package com.serchinastico.lin.detectors

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Detector
import com.serchinastico.lin.dsl.detector
import com.serchinastico.lin.dsl.isClassOrSubclassOf
import com.serchinastico.lin.dsl.issue
import org.jetbrains.uast.UCallExpression


@Detector
fun noSetOnClickListenerCalls() = detector(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "There should not be calls to setOnClickListener",
        """Nowadays there are better ways to synthetize these calls into a more concise declaration with tools
            | like ButterKnife or Data Binding. See https://github.com/JakeWharton/butterknife or
            | https://developer.android.com/topic/libraries/data-binding/""".trimMargin(),
        Category.CORRECTNESS
    )
) {
    callExpression { suchThat { it.isSetOnClickListenerCall } }
}

private inline val UCallExpression.isSetOnClickListenerCall: Boolean
    get() {
        val receiverType = receiverType ?: return false

        val isReceivedChildOfAndroidView = receiverType.isClassOrSubclassOf("android.view.View")
        val isMethodNameSetOnClickListener = methodIdentifier?.name == "setOnClickListener"

        return isReceivedChildOfAndroidView && isMethodNameSetOnClickListener
    }
