package com.serchinastico.lin.detectors

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Detector
import com.serchinastico.lin.dsl.CustomParameters
import com.serchinastico.lin.dsl.detector
import com.serchinastico.lin.dsl.issue
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UImportStatement

private val KOTLINX_SYNTHETIC_VIEW_IMPORT = """kotlinx\.android\.synthetic\.main\.(.*)""".toRegex()
private val LAYOUT_EXPRESSION = """R\.layout\.(.*)""".toRegex()

@Detector
fun wrongSyntheticViewReference() = detector(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "References to synthetic views not directly defined in this class or its ancestors layout",
        """Imports to kotlinx synthetic views other than the ones defined in the layout referenced in this
            | or its ancestor classes is mostly a typo. If you want to reference views from custom views abstract them
            | with methods in order to keep a low coupling with its specific implementation.
        """.trimMargin(),
        Category.CORRECTNESS
    )
) {
    import {
        suchThat { it.isSyntheticViewImport }
        mappingParameters(::storeImportedLayout)
    }

    expression {
        suchThatParams(::isDifferentThanImportedLayout)
    }
}

private val UImportStatement.isSyntheticViewImport: Boolean
    get() {
        val importedString = importReference?.asRenderString() ?: return false
        return KOTLINX_SYNTHETIC_VIEW_IMPORT.matches(importedString)
    }

private fun storeImportedLayout(node: UImportStatement, params: CustomParameters): CustomParameters {
    val importedString = node.importReference?.asRenderString() ?: return params
    val importedLayout = KOTLINX_SYNTHETIC_VIEW_IMPORT
        .matchEntire(importedString)
        ?.groups
        ?.get(1)
        ?.value ?: return params

    return params.plus("Imported Layout" to importedLayout)
}

private fun isDifferentThanImportedLayout(
    node: UExpression,
    params: CustomParameters
): Boolean {
    val importedLayout = params["Imported Layout"] ?: return false
    val usedLayout = LAYOUT_EXPRESSION.matchEntire(node.asRenderString())
        ?.groups
        ?.get(1)
        ?.value ?: return false
    return usedLayout != importedLayout
}
