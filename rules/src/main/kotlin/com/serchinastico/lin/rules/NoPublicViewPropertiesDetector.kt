package com.serchinastico.lin.rules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Rule
import com.serchinastico.lin.dsl.issue
import com.serchinastico.lin.dsl.rule

@Rule
fun noPublicViewProperties() = rule(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "View properties should always be private",
        "Exposing views to other classes, be it from activities or custom views is leaking too much" +
                " information to other classes and is prompt to break if the inner implementation of" +
                " the layout changes, the only exception is if those views are part of an implemented interface",
        Category.CORRECTNESS
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