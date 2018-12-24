package com.serchinastico.lin.detectors

import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Detector
import com.serchinastico.lin.dsl.detector
import com.serchinastico.lin.dsl.isClassOrSubclassOf
import com.serchinastico.lin.dsl.issue
import org.jetbrains.uast.UCallExpression

@Detector
fun noPrintStackTraceCalls() = detector(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "There should not be calls to the printStackTrace method in Throwable instances",
        "Errors should be logged with a configured logger or sent to the backend for faster response"
    )
) {
    callExpression { suchThat { it.isPrintStackTraceCall } }
}

private inline val UCallExpression.isPrintStackTraceCall: Boolean
    get() {
        val receiverType = receiverType ?: return false

        val isReceiverChildOfThrowable = receiverType.isClassOrSubclassOf("java.lang.Throwable", "kotlin.Throwable")
        val isMethodPrintStackTrace = methodIdentifier?.name == "printStackTrace"

        return isReceiverChildOfThrowable && isMethodPrintStackTrace
    }
