package com.serchinastico.lin.rules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Rule
import com.serchinastico.lin.dsl.isClassOrSubclassOf
import com.serchinastico.lin.dsl.isPrivate
import com.serchinastico.lin.dsl.issue
import com.serchinastico.lin.dsl.rule

@Rule
fun noMoreThanOneGsonInstance() = rule(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "Gson should only be initialized once at most",
        """Creating multiple instances of Gson may hurt performance and it's a common mistake to instantiate it for
            | simple serialization/deserialization. Use a single instance, be it with a classic singleton pattern or
            | other mechanism your dependency injector framework provides.
        """.trimMargin(),
        Category.PERFORMANCE
    )
) {
    field {
        suchThat { node ->
            val isPrivateField = node.isPrivate
            val isViewType = node.isClassOrSubclassOf("android.view.View")

            !isPrivateField && isViewType
        }
    }

}