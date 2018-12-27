package com.serchinastico.lin.detectors

import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Detector
import com.serchinastico.lin.dsl.Quantifier.Companion.moreThan
import com.serchinastico.lin.dsl.RuleSet.Companion.anyOf
import com.serchinastico.lin.dsl.detector
import com.serchinastico.lin.dsl.file
import com.serchinastico.lin.dsl.issue
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.util.isConstructorCall

@Detector
fun noMoreThanOneDateInstance() = detector(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "Date should only be initialized only once",
        """Creating multiple instances of Date is an indicator of not injecting your time on your code. That's a
            | classic issue when testing date/time related code. Centralize the creation of date objects on a single
            | class to be able to replace it in testing time.
        """.trimMargin()
    ),
    anyOf(
        file(moreThan(1)) { callExpression { suchThat { it.isDateConstructor } } },
        file { callExpression(moreThan(1)) { suchThat { it.isDateConstructor } } }
    )
)

private inline val UCallExpression.isDateConstructor: Boolean
    get() {
        val returnTypeCanonicalName = returnType?.canonicalText ?: return false
        return isConstructorCall() && returnTypeCanonicalName == "java.util.Date"
    }