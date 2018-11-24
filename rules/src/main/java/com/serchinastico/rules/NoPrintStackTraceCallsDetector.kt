package com.serchinastico.rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import java.util.*

class NoPrintStackTraceCallsDetector : Detector(), Detector.UastScanner {
    companion object {
        private val DETECTOR_CLASS = NoPrintStackTraceCallsDetector::class.java
        private val DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE
        private val IMPLEMENTATION = Implementation(DETECTOR_CLASS, DETECTOR_SCOPE)
        private const val ISSUE_ID = "NoPrintStackTraceCalls"
        private const val ISSUE_DESCRIPTION =
            "There should not be calls to the printStackTrace method in Throwable instances"
        private const val ISSUE_EXPLANATION =
            "Errors should be logged with a configured logger or sent to the backend for faster response"
        private val ISSUE_CATEGORY = Category.CORRECTNESS
        private const val ISSUE_PRIORITY = 5
        private val ISSUE_SEVERITY = Severity.ERROR
        val ISSUE = Issue.create(
            ISSUE_ID, ISSUE_DESCRIPTION, ISSUE_EXPLANATION, ISSUE_CATEGORY, ISSUE_PRIORITY,
            ISSUE_SEVERITY, IMPLEMENTATION
        )
    }

    override fun getApplicableFiles(): EnumSet<Scope> = DETECTOR_SCOPE

    override fun getApplicableUastTypes(): List<Class<out UElement>>? =
        listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? =
        LinElementHandler(context)

    private class LinElementHandler(private val context: JavaContext) : UElementHandler() {
        override fun visitCallExpression(node: UCallExpression) {
            val receiverType = node.receiverType ?: return

            val isReceiverChildOfThrowable = receiverType.isAnyOf("java.lang.Throwable", "kotlin.Throwable")
            val isMethodPrintStackTrace = node.methodIdentifier?.name == "printStackTrace"

            if (isReceiverChildOfThrowable && isMethodPrintStackTrace) {
                context.report(ISSUE, Location.create(context.file), ISSUE.getBriefDescription(TextFormat.TEXT))
            }
        }
    }
}
