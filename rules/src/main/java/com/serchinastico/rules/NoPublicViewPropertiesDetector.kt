package com.serchinastico.rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UField
import java.util.*

class NoPublicViewPropertiesDetector : Detector(), Detector.UastScanner {

    companion object {
        private val DETECTOR_CLASS = NoPublicViewPropertiesDetector::class.java
        private val DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE
        private val IMPLEMENTATION = Implementation(DETECTOR_CLASS, DETECTOR_SCOPE)
        private const val ISSUE_ID = "NoPublicViewProperties"
        private const val ISSUE_DESCRIPTION =
            "View properties should always be private"
        private const val ISSUE_EXPLANATION =
            "Exposing views to other classes, be it from activities or custom views is leaking too much information to other classes and is prompt to break if the inner implementation of the layout changes, the only exception is if those views are part of an implemented interface/superclass"
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
        listOf(UField::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? =
        LinElementHandler(context)

    private class LinElementHandler(private val context: JavaContext) : UElementHandler() {
        override fun visitField(node: UField) {
            val isPrivateField = node.isPrivate
            val isViewType = node.isClassOrSubclassOf("android.view.View")

            if (!isPrivateField && isViewType) {
                context.report(ISSUE, Location.create(context.file), ISSUE.getBriefDescription(TextFormat.TEXT))
            }
        }
    }
}