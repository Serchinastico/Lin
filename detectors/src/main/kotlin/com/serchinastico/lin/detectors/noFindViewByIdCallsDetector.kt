package com.serchinastico.lin.detectors

import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Detector
import com.serchinastico.lin.dsl.detector
import com.serchinastico.lin.dsl.isClassOrSubclassOf
import com.serchinastico.lin.dsl.issue
import org.jetbrains.uast.UCallExpression

@Detector
fun noFindViewByIdCalls() = detector(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "There should not be calls to findViewById",
        """Nowadays there are better ways to synthetize these calls into a more concise declaration with tools
            | like ButterKnife. See https://github.com/JakeWharton/butterknife or
            | https://kotlinlang.org/docs/tutorials/android-plugin.html#view-binding""".trimMargin()
    )
) {
    callExpression { suchThat { it.isFindViewByIdCall } }
}

private inline val UCallExpression.isFindViewByIdCall: Boolean
    get() {
        val receiverType = receiverType ?: return false

        val isReceiverChildOfActivityOrView =
            receiverType.isClassOrSubclassOf("android.app.Activity", "android.view.View")
        val isMethodFindViewById = methodIdentifier?.name == "findViewById"

        return isReceiverChildOfActivityOrView && isMethodFindViewById
    }
