package com.serchinastico.lintools

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import org.jetbrains.uast.UCallExpression

open class LinDetector : Detector() {
    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return LinElementHandler()
    }

    private class LinElementHandler : UElementHandler() {
        override fun visitCallExpression(node: UCallExpression) {
            super.visitCallExpression(node)
        }
    }
}