package com.serchinastico.lin.rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.dsl.createIssue
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UField
import java.util.*

class NoPublicViewPropertiesDetector : Detector(), Detector.UastScanner {

    companion object {
        private val DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE

        val ISSUE = createIssue<NoPublicViewPropertiesDetector>(
            "NoPublicViewProperties",
            DETECTOR_SCOPE,
            "View properties should always be private",
            "Exposing views to other classes, be it from activities or custom views is leaking too much" +
                    " information to other classes and is prompt to break if the inner implementation of" +
                    " the layout changes, the only exception is if those views are part of an implemented interface",
            Category.CORRECTNESS
        )
    }

    override fun getApplicableFiles(): EnumSet<Scope> =
        DETECTOR_SCOPE

    override fun getApplicableUastTypes(): List<Class<out UElement>>? =
        listOf(UField::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? =
        LinElementHandler(context)

    private class LinElementHandler(private val context: JavaContext) : UElementHandler() {
        override fun visitField(node: UField) {
            val isPrivateField = node.isPrivate
            val isViewType = node.isClassOrSubclassOf("android.view.View")

            if (!isPrivateField && isViewType) {
                context.report(ISSUE)
            }
        }
    }
}