package com.serchinastico.rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.serchinastico.lintools.LinDetector
import org.jetbrains.uast.*


class NoDataFrameworksFromAndroidClass : LinDetector(), Detector.UastScanner {

    companion object {
        private val DETECTOR_CLASS = NoDataFrameworksFromAndroidClass::class.java
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
            if (!node.isFrameworkLibraryImport) {
                return
            }

            val fileContainsActivityClass = node.classesInSameFile
                .flatMap { it.uastSuperTypes }
                .any { it.isAndroidFrameworkType }

            if (fileContainsActivityClass) {
                context.report(ISSUE, Location.create(context.file), ISSUE.getBriefDescription(TextFormat.TEXT))
            }
        }
    }
}

private val UTypeReferenceExpression.isAndroidFrameworkType: Boolean
    get() = getQualifiedName()?.let { name ->
        listOf(
            "android.app.",
            "android.support.v4.app"
        ).any { name.startsWith(it) }
    } ?: false

private val UImportStatement.isFrameworkLibraryImport: Boolean
    get() = asRenderString().let { name ->
        listOf(
            "com.squareup.retrofit",
            "com.squareup.retrofit2",
            "com.squareup.okhttp",
            "com.android.volley",
            "com.mcxiaoke.volley",
            "androidx.room",
            "android.arch.persistence.room",
            "android.content.SharedPreferences",
            "android.database",
            "java.net"
        ).any { name.startsWith(it) }
    }

val UElement.classesInSameFile: List<UClass>
    get() = getContainingUFile()?.classes ?: emptyList()