package com.serchinastico.rules

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class NoPublicViewPropertiesDetectorTest {
    @Test
    fun inJavaClass_whenViewFieldIsPrivate_detectsNoErrors() {
        lint()
            .files(
                java(
                    """
                        |package foo;
                        |
                        |import android.view.View;
                        |
                        |class TestClass {
                        |   private View view;
                        |}
                    """.trimMargin()
                )
            )
            .issues(NoPublicViewPropertiesDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun inJavaClass_whenViewFieldIsNonPrivate_detectsErrors() {
        lint()
            .files(
                java(
                    """
                        |package foo;
                        |
                        |import android.view.View;
                        |
                        |class TestClass {
                        |   View view;
                        |}
                    """.trimMargin()
                )
            )
            .issues(NoPublicViewPropertiesDetector.ISSUE)
            .run()
            .expect(
                """
                    |src/foo/TestClass.java: Error: View properties should always be private [NoPublicViewProperties]
                    |1 errors, 0 warnings
                """.trimMargin()
            )
    }


    @Test
    fun inKotlinClass_whenViewFieldIsPrivate_detectsNoErrors() {
        lint()
            .files(
                kotlin(
                    """
                        |package foo
                        |
                        |import android.view.View
                        |
                        |class TestClass {
                        |   private val view: View = View()
                        |}
                    """.trimMargin()
                )
            )
            .issues(NoPublicViewPropertiesDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun inKotlinClass_whenViewFieldIsNotPrivate_detectsErrors() {
        lint()
            .files(
                kotlin(
                    """
                        |package foo
                        |
                        |import android.view.View
                        |
                        |class TestClass {
                        |   public val view: android.view.View = android.view.View(null)
                        |}
                    """.trimMargin()
                )
            )
            .issues(NoPublicViewPropertiesDetector.ISSUE)
            .run()
            .expect(
                """
                    |src/foo/TestClass.kt: Error: View properties should always be private [NoPublicViewProperties]
                    |1 errors, 0 warnings
                """.trimMargin()
            )
    }
}