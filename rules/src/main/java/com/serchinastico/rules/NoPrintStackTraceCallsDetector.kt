package com.serchinastico.rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import java.util.*

class NoPrintStackTraceCallsDetector : Detector(), Detector.UastScanner {

    companion object {
        private val DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE

        val ISSUE = createIssue<NoPrintStackTraceCallsDetector>(
            "NoPrintStackTraceCalls",
            DETECTOR_SCOPE,
            "There should not be calls to the printStackTrace method in Throwable instances",
            "Errors should be logged with a configured logger or sent to the backend for faster response",
            Category.CORRECTNESS
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

            val isReceiverChildOfThrowable = receiverType.isClassOrSubclassOf("java.lang.Throwable", "kotlin.Throwable")
            val isMethodPrintStackTrace = node.methodIdentifier?.name == "printStackTrace"

            if (isReceiverChildOfThrowable && isMethodPrintStackTrace) {
                context.report(ISSUE)
            }
        }
    }
}
