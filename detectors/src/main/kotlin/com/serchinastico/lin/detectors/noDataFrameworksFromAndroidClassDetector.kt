package com.serchinastico.lin.detectors

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.annotations.Detector
import com.serchinastico.lin.dsl.detector
import com.serchinastico.lin.dsl.isAndroidFrameworkType
import com.serchinastico.lin.dsl.isFrameworkLibraryImport
import com.serchinastico.lin.dsl.issue

@Detector
fun noDataFrameworksFromAndroidClass() = detector(
    issue(
        Scope.JAVA_FILE_SCOPE,
        """Framework classes to get or store data should never be called from Activities, Fragments or any other
                | Android related view.""".trimMargin(),
        """Your Android classes should not be responsible for retrieving or storing information, that should be
                | responsibility of another classes.""".trimMargin(),
        Category(null, "Lin", 5)
    )
) {
    import { suchThat { it.isFrameworkLibraryImport } }
    type { suchThat { node -> node.uastSuperTypes.any { it.isAndroidFrameworkType } } }
}