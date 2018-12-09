package com.serchinastico.lin.rules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Rule
import com.serchinastico.lin.dsl.isAndroidFrameworkType
import com.serchinastico.lin.dsl.isFrameworkLibraryImport
import com.serchinastico.lin.dsl.issue
import com.serchinastico.lin.dsl.rule

@Rule
fun noDataFrameworksFromAndroidClass() = rule(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "Framework classes to get or store data should never be called from Activities, Fragments or any other" +
                " Android related view.",
        "Your Android classes should not be responsible for retrieving or storing information, that should be " +
                "responsibility of another classes.",
        Category.INTEROPERABILITY
    )
) {
    file {
        import { suchThat { it.isFrameworkLibraryImport } }
        type { suchThat { node -> node.uastSuperTypes.any { it.isAndroidFrameworkType } } }
    }
}