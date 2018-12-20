package com.serchinastico.lin.rules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Rule
import com.serchinastico.lin.dsl.Quantifier.Companion.atLeast
import com.serchinastico.lin.dsl.issue
import com.serchinastico.lin.dsl.rule
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.util.isConstructorCall

@Rule
fun noMoreThanOneGsonInstance() = rule(
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
    atLeast(2)
) { callExpression { suchThat { it.isGsonConstructor } } }

private val UCallExpression.isGsonConstructor: Boolean
    get() {
        val returnTypeCanonicalName = returnType?.canonicalText ?: return false
        return isConstructorCall() && returnTypeCanonicalName == "com.google.gson.Gson"
    }