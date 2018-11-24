package com.serchinastico.rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import java.util.*


class NoDataFrameworksFromAndroidClassDetector : Detector(), Detector.UastScanner {

    companion object {
        private val DETECTOR_CLASS = NoDataFrameworksFromAndroidClassDetector::class.java
        private val DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE
        private val IMPLEMENTATION = Implementation(DETECTOR_CLASS, DETECTOR_SCOPE)
        private const val ISSUE_ID = "NoDataFrameworksFromAndroidClass"
        private const val ISSUE_DESCRIPTION =
            "Framework classes to get or store data should never be called from Activities, Fragments or any other Android related view."
        private const val ISSUE_EXPLANATION =
            "Your Android classes should not be responsible for retrieving or storing information, that should be responsibility of another classes."
        private val ISSUE_CATEGORY = Category.INTEROPERABILITY
        private const val ISSUE_PRIORITY = 5
        private val ISSUE_SEVERITY = Severity.ERROR
        val ISSUE = Issue.create(
            ISSUE_ID, ISSUE_DESCRIPTION, ISSUE_EXPLANATION, ISSUE_CATEGORY, ISSUE_PRIORITY,
            ISSUE_SEVERITY, IMPLEMENTATION
        )
    }

    override fun getApplicableFiles(): EnumSet<Scope> = DETECTOR_SCOPE

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
    get() {
        val importedPackageName = importReference?.asRenderString() ?: return false

        return listOf(
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
        ).any { importedPackageName.startsWith(it) }
    }

val UElement.classesInSameFile: List<UClass>
    get() = getContainingUFile()?.classes ?: emptyList()