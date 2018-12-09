package com.serchinastico.lin.rules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Rule
import com.serchinastico.lin.dsl.issue
import com.serchinastico.lin.dsl.rule

@Rule
fun noElseInSwitchWithEnumOrSealed() = rule(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "There should not be else/default branches on a switch statement checking for enum/sealed class values",
        "Adding an else/default branch breaks extensibility because it won't let you know if there is a missing " +
                "implementation when adding new types to the enum/sealed class",
        Category.CORRECTNESS
    )
) {
    switch {
        suchThat { node ->
            val classReferenceType = node.expression?.getExpressionType() ?: (return@suchThat false)

            if (!classReferenceType.isEnum && !classReferenceType.isSealed) {
                return@suchThat false
            }

            node.clauses.any { clause -> clause.isElseBranch }
        }
    }
}