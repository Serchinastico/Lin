package com.serchinastico.rules

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class NoHttpFromAndroidClassTest {
    @Test
    fun detectsZeroErrorsInEmptyJavaFile() {
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
            .issues(NoHttpFromAndroidClass.ISSUE)
            .run()
            .expect(
                """
                    |No warnings.
                """.trimMargin()
            )
    }
}