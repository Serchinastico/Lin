package com.serchinastico.lin.detectors

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Detector
import com.serchinastico.lin.dsl.Storage
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
        suchThat {
            val importedLayout = storeImportedLayout(it) ?: return@suchThat false
            storage["Imported Layout"] = importedLayout
            it.isSyntheticViewImport
        }
    }

    expression {
        suchThat {
            it.isDifferentThanImportedLayout(storage) }
    }
}

private val UImportStatement.isSyntheticViewImport: Boolean
    get() {
        val importedString = importReference?.asRenderString() ?: return false
        return KOTLINX_SYNTHETIC_VIEW_IMPORT.matches(importedString)
    }

private fun storeImportedLayout(node: UImportStatement): String? {
    val importedString = node.importReference?.asRenderString() ?: return null
    return KOTLINX_SYNTHETIC_VIEW_IMPORT
        .matchEntire(importedString)
        ?.groups
        ?.get(1)
        ?.value
}

private fun UExpression.isDifferentThanImportedLayout(
    storage: Storage
): Boolean {
    val importedLayout = storage["Imported Layout"] ?: return false
    val usedLayout = LAYOUT_EXPRESSION.matchEntire(asRenderString())
        ?.groups
        ?.get(1)
        ?.value ?: return false
    return usedLayout != importedLayout
}
