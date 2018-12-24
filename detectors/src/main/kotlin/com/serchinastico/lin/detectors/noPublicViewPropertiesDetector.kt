package com.serchinastico.lin.detectors

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Detector
import com.serchinastico.lin.dsl.detector
import com.serchinastico.lin.dsl.isClassOrSubclassOf
import com.serchinastico.lin.dsl.isPrivate
import com.serchinastico.lin.dsl.issue
import org.jetbrains.uast.UField

@Detector
fun noPublicViewProperties() = detector(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "View properties should always be private",
        """Exposing views to other classes, be it from activities or custom views is leaking too much
                | information to other classes and is prompt to break if the inner implementation of
                | the layout changes, the only exception is if those views are part of an implemented
                | interface""".trimMargin(),
        Category.Lin
    )
) {
    field { suchThat { it.isNonPrivateViewField } }
}

private inline val UField.isNonPrivateViewField: Boolean
    get() {
        val isPrivateField = isPrivate
        val isViewType = isClassOrSubclassOf("android.view.View")

        return !isPrivateField && isViewType
    }