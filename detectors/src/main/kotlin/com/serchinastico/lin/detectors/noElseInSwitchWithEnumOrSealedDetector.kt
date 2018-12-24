package com.serchinastico.lin.detectors

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Detector
import com.serchinastico.lin.dsl.*
import org.jetbrains.uast.USwitchExpression

@Detector
fun noElseInSwitchWithEnumOrSealed() = detector(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "There should not be else/default branches on a switch statement checking for enum/sealed class values",
        """Adding an else/default branch breaks extensibility because it won't let you know if there is a missing
                | implementation when adding new types to the enum/sealed class""".trimMargin(),
        Category(null, "Lin", 5)
    )
) {
    switchExpression { suchThat { it.hasElseWithEnumOrSealedExpression } }
}

private inline val USwitchExpression.hasElseWithEnumOrSealedExpression: Boolean
    get() {
        val classReferenceType = expression?.getExpressionType() ?: return false

        if (!classReferenceType.isEnum && !classReferenceType.isSealed) {
            return false
        }

        return clauses.any { clause -> clause.isElseBranch }
    }