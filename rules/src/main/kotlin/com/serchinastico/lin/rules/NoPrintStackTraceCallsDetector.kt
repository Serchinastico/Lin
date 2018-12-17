package com.serchinastico.lin.rules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Rule
import com.serchinastico.lin.dsl.isClassOrSubclassOf
import com.serchinastico.lin.dsl.issue
import com.serchinastico.lin.dsl.rule

@Rule
fun noPrintStackTraceCalls() = rule(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "There should not be calls to the printStackTrace method in Throwable instances",
        "Errors should be logged with a configured logger or sent to the backend for faster response",
        Category.CORRECTNESS
    )
) {
    callExpression {
        suchThat { node ->
            val receiverType = node.receiverType ?: (return@suchThat false)

            val isReceiverChildOfThrowable = receiverType.isClassOrSubclassOf("java.lang.Throwable", "kotlin.Throwable")
            val isMethodPrintStackTrace = node.methodIdentifier?.name == "printStackTrace"

            isReceiverChildOfThrowable && isMethodPrintStackTrace
        }
    }
}
