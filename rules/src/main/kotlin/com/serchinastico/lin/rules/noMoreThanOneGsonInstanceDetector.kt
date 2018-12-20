package com.serchinastico.lin.rules

import com.android.tools.lint.detector.api.Category
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
fun noMoreThanOneGsonInstance() = detector(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "Gson should only be initialized only once",
        """Creating multiple instances of Gson may hurt performance and it's a common mistake to instantiate it for
            | simple serialization/deserialization. Use a single instance, be it with a classic singleton pattern or
            | other mechanism your dependency injector framework provides. This way you can also share the common
            | type adapters.
        """.trimMargin(),
        Category.PERFORMANCE
    ),
    anyOf(
        file(moreThan(1)) { callExpression { suchThat { it.isGsonConstructor } } },
        file { callExpression(moreThan(1)) { suchThat { it.isGsonConstructor } } }
    )
)

private inline val UCallExpression.isGsonConstructor: Boolean
    get() {
        val returnTypeCanonicalName = returnType?.canonicalText ?: return false
        return isConstructorCall() && returnTypeCanonicalName == "com.google.gson.Gson"
    }