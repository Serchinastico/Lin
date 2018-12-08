package com.serchinastico.lin.rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Rule
import com.serchinastico.lin.dsl.createIssue
import com.serchinastico.lin.dsl.issue
import com.serchinastico.lin.dsl.rule
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UImportStatement
import java.util.*

/**
 *  file {
 *      anyImport { suchThat { isFrameworkLibraryImport } }
 *
 *      anyType {
 *          suchThat {
 *              uastSuperTypes.any { it.isAndroidFrameworkType }
 *          }
 *      }
 *  }
 */


@Rule
fun noDataFrameworksRule() = rule(
    issue(
        "NoDataFrameworksFromAndroidClass",
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

class NoDataFrameworksFromAndroidClassDetector : Detector(), Detector.UastScanner {

    companion object {
        private val DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE

        val ISSUE =
            createIssue<NoDataFrameworksFromAndroidClassDetector>(
                "NoDataFrameworksFromAndroidClass",
                DETECTOR_SCOPE,
                "Framework classes to get or store data should never be called from Activities, Fragments or any other" +
                        " Android related view.",
                "Your Android classes should not be responsible for retrieving or storing information, that should be " +
                        "responsibility of another classes.",
                Category.INTEROPERABILITY
            )
    }

    override fun getApplicableFiles(): EnumSet<Scope> =
        DETECTOR_SCOPE

    override fun getApplicableUastTypes(): List<Class<out UElement>>? =
        listOf(UImportStatement::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? =
        LinElementHandler(context)

    private class LinElementHandler(private val context: JavaContext) : UElementHandler() {
        override fun visitImportStatement(node: UImportStatement) {
            if (!node.isFrameworkLibraryImport) {
                return
            }

            val fileContainsActivityClass = node.classesInSameFile
                .flatMap { it.uastSuperTypes }
                .any { it.isAndroidFrameworkType }

            if (fileContainsActivityClass) {
                context.report(ISSUE)
            }
        }
    }
}