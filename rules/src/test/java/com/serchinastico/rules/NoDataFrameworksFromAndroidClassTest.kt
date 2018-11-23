package com.serchinastico.rules

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class NoDataFrameworksFromAndroidClassTest {
    @Test
    fun detectsZeroErrorsInFileWithImportButNoAndroidClass() {
        lint()
            .files(
                java(
                    """
                        |package foo;
                        |
                        |import com.squareup.retrofit2.*;
                    """.trimMargin()
                )
            )
            .issues(NoDataFrameworksFromAndroidClass.ISSUE)
            .run()
            .expect(
                """
                    |No warnings.
                """.trimMargin()
            )
    }

    @Test
    fun detectsZeroErrorsInFileWithNoFrameworkImportsButAndroidClass() {
        lint()
            .files(
                kotlin(
                    """
                        |package foo;
                        |
                        |import android.app.Activity
                        |import android.os.Bundle
                        |import android.view.View
                        |
                        |class SampleActivity : Activity()
                    """.trimMargin()
                )
            )
            .issues(NoDataFrameworksFromAndroidClass.ISSUE)
            .run()
            .expect(
                """
                    |No warnings.
                """.trimMargin()
            )
    }
}