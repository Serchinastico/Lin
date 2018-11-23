package com.serchinastico.rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.serchinastico.lintools.LinDetector
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UImportStatement
import org.jetbrains.uast.getContainingUFile


class NoHttpFromAndroidClass : LinDetector(), Detector.UastScanner {

    companion object {
        private val DETECTOR_CLASS = NoHttpFromAndroidClass::class.java
        private val DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE
        private val IMPLEMENTATION = Implementation(DETECTOR_CLASS, DETECTOR_SCOPE)
        private const val ISSUE_ID = "NoSwitchAllowed"
        private const val ISSUE_DESCRIPTION = "Avoid Using Switch statements"
        private const val ISSUE_EXPLANATION =
            "Kony compiler doesn't fully work with switch statements so they should be replaced by if-else-if statements."
        private val ISSUE_CATEGORY = Category.INTEROPERABILITY
        private const val ISSUE_PRIORITY = 5
        private val ISSUE_SEVERITY = Severity.ERROR
        val ISSUE = Issue.create(
            ISSUE_ID, ISSUE_DESCRIPTION, ISSUE_EXPLANATION, ISSUE_CATEGORY, ISSUE_PRIORITY,
            ISSUE_SEVERITY, IMPLEMENTATION
        )
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf(UImportStatement::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return LinElementHandler(context)
    }

    private class LinElementHandler(private val context: JavaContext) : UElementHandler() {
        override fun visitImportStatement(node: UImportStatement) {
            val renderString = node.importReference?.asRenderString() ?: return

            val fileContainsActivityClass = node.classesInSameFile
                .flatMap { it.uastSuperTypes }
                .any { it.getQualifiedName() == "android.app.Activity" }

            val importsHttpClass = renderString.contains("com.squareup.retrofit2")

            if (fileContainsActivityClass && importsHttpClass) {
                context.report(ISSUE, Location.create(context.file), ISSUE.getBriefDescription(TextFormat.TEXT))
            }
        }
    }
}

val UElement.classesInSameFile: List<UClass>
    get() = getContainingUFile()?.classes ?: emptyList()