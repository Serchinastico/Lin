package com.serchinastico.lin.rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import org.jetbrains.uast.UElement
import org.jetbrains.uast.USwitchExpression
import java.util.*

class NoElseInSwitchWithEnumOrSealedDetector : Detector(), Detector.UastScanner {

    companion object {
        private val DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE

        val ISSUE =
            createIssue<NoElseInSwitchWithEnumOrSealedDetector>(
                "NoElseInSwitchWithEnum",
                DETECTOR_SCOPE,
                "There should not be else/default branches on a switch statement checking for enum/sealed class values",
                "Adding an else/default branch breaks extensibility because it won't let you know if there is a missing " +
                        "implementation when adding new types to the enum/sealed class",
                Category.CORRECTNESS
            )
    }

    override fun getApplicableFiles(): EnumSet<Scope> =
        DETECTOR_SCOPE

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
                    context.report(ISSUE)
                }
            }
        }
    }
}