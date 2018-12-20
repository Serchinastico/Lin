package com.serchinastico.lin.rules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Rule
import com.serchinastico.lin.dsl.isClassOrSubclassOf
import com.serchinastico.lin.dsl.issue
import com.serchinastico.lin.dsl.rule


@Rule
fun noSetOnClickListenerCalls() = rule(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "There should not be calls to setOnClickListener",
        """Nowadays there are better ways to synthetize these calls into a more concise declaration with tools
            | like ButterKnife or Data Binding. See https://github.com/JakeWharton/butterknife or
            | https://developer.android.com/topic/libraries/data-binding/""".trimMargin(),
        Category.CORRECTNESS
    )
) {
    callExpression {
        suchThat { node ->
            val receiverType = node.receiverType ?: (return@suchThat false)

            val isReceivedChildOfAndroidView = receiverType.isClassOrSubclassOf("android.view.View")
            val isMethodNameSetOnClickListener = node.methodIdentifier?.name == "setOnClickListener"

            isReceivedChildOfAndroidView && isMethodNameSetOnClickListener
        }
    }
}
