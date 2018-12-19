package com.serchinastico.lin.rules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Rule
import com.serchinastico.lin.dsl.Quantifier.MoreThan
import com.serchinastico.lin.dsl.issue
import com.serchinastico.lin.dsl.rule
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
    )
) {
    callExpression(quantifier = MoreThan(1)) {
        suchThat { node ->
            val returnTypeCanonicalName = node.returnType?.canonicalText ?: return@suchThat false
            node.isConstructorCall() && returnTypeCanonicalName == "com.google.gson.Gson"
        }
    }
}