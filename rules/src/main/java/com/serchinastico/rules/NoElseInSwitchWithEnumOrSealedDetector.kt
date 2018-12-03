package com.serchinastico.rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.USwitchExpression
import java.util.*

class NoElseInSwitchWithEnumOrSealedDetector : Detector(), Detector.UastScanner {
    companion object {
        private val DETECTOR_CLASS = NoElseInSwitchWithEnumOrSealedDetector::class.java
        private val DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE
        private val IMPLEMENTATION = Implementation(DETECTOR_CLASS, DETECTOR_SCOPE)
        private const val ISSUE_ID = "NoElseInSwitchWithEnum"
        private const val ISSUE_DESCRIPTION =
            "There should not be else/default branches on a switch statement checking for enum/sealed class values"
        private const val ISSUE_EXPLANATION =
            "Adding an else/default branch breaks extensibility because it won't let you know if there is a missing implementation when adding new types to the enum/sealed class"
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
        listOf(USwitchExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? =
        LinElementHandler(context)

    private class LinElementHandler(private val context: JavaContext) : UElementHandler() {
        override fun visitSwitchExpression(node: USwitchExpression) {
            val classReferenceType = node.expression?.getExpressionType() ?: return

            if (!classReferenceType.isEnum && !classReferenceType.isSealed) {
                return
            }

            node.clauses.forEach { clause ->
                if (clause.isElseBranch) {
                    context.report(
                        ISSUE,
                        Location.create(context.file),
                        ISSUE.getBriefDescription(TextFormat.TEXT)
                    )
                }
            }

        }

        override fun visitCallExpression(node: UCallExpression) {
            val receiverType = node.receiverType ?: return

            val isReceiverChildOfThrowable = receiverType.isClassOrSubclassOf("java.lang.Throwable", "kotlin.Throwable")
            val isMethodPrintStackTrace = node.methodIdentifier?.name == "printStackTrace"

            if (isReceiverChildOfThrowable && isMethodPrintStackTrace) {
                context.report(ISSUE, Location.create(context.file), ISSUE.getBriefDescription(TextFormat.TEXT))
            }
        }
    }
}