package com.serchinastico.rules

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class NoDataFrameworksFromAndroidClassDetectorTest {
    @Test
    fun inJavaNonAndroidClass_whenFileHasFrameworkImport_detectsNoErrors() {
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
            .issues(NoDataFrameworksFromAndroidClassDetector.ISSUE)
            .run()
            .expect(
                """
                    |No warnings.
                """.trimMargin()
            )
    }

    @Test
    fun inJavaAndroidClass_whenFileHasNoFrameworkImport_detectsNoErrors() {
        lint()
            .files(
                java(
                    """
                        |package foo;
                        |
                        |import android.app.Activity;
                        |import android.os.Bundle;
                        |import android.view.View;
                        |
                        |public class TestActivity extends Activity {}
                    """.trimMargin()
                )
            )
            .issues(NoDataFrameworksFromAndroidClassDetector.ISSUE)
            .run()
            .expect(
                """
                    |No warnings.
                """.trimMargin()
            )
    }

    @Test
    fun inJavaAndroidClass_whenFileHasFrameworkImport_detectsErrors() {
        lint()
            .files(
                java(
                    """
                        |package foo;
                        |
                        |import android.app.Activity;
                        |import android.os.Bundle;
                        |import android.view.View;
                        |import com.squareup.retrofit2.*;
                        |
                        |public class TestActivity extends Activity {}
                    """.trimMargin()
                )
            )
            .issues(NoDataFrameworksFromAndroidClassDetector.ISSUE)
            .run()
            .expect(
                """
                    |src/foo/TestActivity.java: Error: Framework classes to get or store data should never be called from Activities, Fragments or any other Android related view. [NoDataFrameworksFromAndroidClass]
                    |1 errors, 0 warnings
                """.trimMargin()
            )
    }

    @Test
    fun inKotlinNonAndroidClass_whenFileHasFrameworkImport_detectsNoErrors() {
        lint()
            .files(
                kotlin(
                    """
                        |package foo
                        |
                        |import com.squareup.retrofit2.*
                    """.trimMargin()
                )
            )
            .issues(NoDataFrameworksFromAndroidClassDetector.ISSUE)
            .run()
            .expect(
                """
                    |No warnings.
                """.trimMargin()
            )
    }

    @Test
    fun inKotlinAndroidClass_whenFileHasNoFrameworkImport_detectsNoErrors() {
        lint()
            .files(
                kotlin(
                    """
                        |package foo
                        |
                        |import android.app.Activity
                        |import android.os.Bundle
                        |import android.view.View
                        |
                        |class TestActivity : Activity()
                    """.trimMargin()
                )
            )
            .issues(NoDataFrameworksFromAndroidClassDetector.ISSUE)
            .run()
            .expect(
                """
                    |No warnings.
                """.trimMargin()
            )
    }

    @Test
    fun inKotlinAndroidClass_whenFileHasFrameworkImport_detectsErrors() {
        lint()
            .files(
                kotlin(
                    """
                        |package foo
                        |
                        |import android.app.Activity
                        |import android.os.Bundle
                        |import android.view.View
                        |import com.squareup.retrofit2.*;
                        |
                        |class TestActivity : Activity()
                    """.trimMargin()
                )
            )
            .issues(NoDataFrameworksFromAndroidClassDetector.ISSUE)
            .run()
            .expect(
                """
                    |src/foo/TestActivity.kt: Error: Framework classes to get or store data should never be called from Activities, Fragments or any other Android related view. [NoDataFrameworksFromAndroidClass]
                    |1 errors, 0 warnings
                """.trimMargin()
            )
    }
}