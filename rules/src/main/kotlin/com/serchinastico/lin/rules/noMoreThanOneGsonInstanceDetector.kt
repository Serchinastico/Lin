package com.serchinastico.lin.rules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Rule
import com.serchinastico.lin.dsl.Quantifier
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
    callExpression(quantifier = Quantifier.MoreThan(1)) {
        suchThat { node ->
            val classReference = node.classReference ?: return@suchThat false
            val resolvedName = classReference.resolvedName ?: return@suchThat false

            node.isConstructorCall() && resolvedName == "com.google.gson.Gson"
        }
    }
}