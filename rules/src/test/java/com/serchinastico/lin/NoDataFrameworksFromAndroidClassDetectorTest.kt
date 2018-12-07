package com.serchinastico.lin

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Scope
import com.serchinastico.lin.dsl.*
import com.serchinastico.lin.rules.NoDataFrameworksFromAndroidClassDetector
import com.serchinastico.lin.test.LintTest
import com.serchinastico.lin.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.test.LintTest.Expectation.SomeError
import org.junit.Test

class NoDataFrameworksFromAndroidClassDetectorTest : LintTest {

    override val issue = NoDataFrameworksFromAndroidClassDetector.ISSUE

    @Test
    fun inJavaNonAndroidClass_whenFileHasFrameworkImport_detectsNoErrors() {
        val issueBuilder = issue(
            "NoDataFrameworksFromAndroidClass",
            Scope.JAVA_FILE_SCOPE,
            "Framework classes to get or store data should never be called from Activities, Fragments or any other" +
                    " Android related view.",
            "Your Android classes should not be responsible for retrieving or storing information, that should be " +
                    "responsibility of another classes.",
            Category.INTEROPERABILITY
        )

        val detector = createDetector(issueBuilder) {
            file {
                import { suchThat { it.isFrameworkLibraryImport } }
                type { suchThat { node -> node.uastSuperTypes.any { it.isAndroidFrameworkType } } }
            }
        }

        TestLintTask.lint()
            .files(
                LintDetectorTest.java(
                    """

                    """.trimIndent()
                )
            )
            .issues(detector.issue)
            .run()
            .expectClean()

        expect(
            """
                |package foo;
                |
                |import com.squareup.retrofit2.*;
            """.trimMargin()
        ).inJava toHave NoErrors
    }

    @Test
    fun inJavaAndroidClass_whenFileHasNoFrameworkImport_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |import android.app.Activity;
                |import android.os.Bundle;
                |import android.view.View;
                |
                |public class TestClass extends Activity {}
            """.trimMargin()
        ).inJava toHave NoErrors
    }

    @Test
    fun inJavaAndroidClass_whenFileHasFrameworkImport_detectsErrors() {
        expect(
            """
                |package foo;
                |
                |import android.app.Activity;
                |import android.os.Bundle;
                |import android.view.View;
                |import com.squareup.retrofit2.*;
                |
                |public class TestClass extends Activity {}
            """.trimMargin()
        ).inJava toHave SomeError
    }

    @Test
    fun inKotlinNonAndroidClass_whenFileHasFrameworkImport_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |import com.squareup.retrofit2.*
            """.trimMargin()
        ).inKotlin toHave NoErrors
    }

    @Test
    fun inKotlinAndroidClass_whenFileHasNoFrameworkImport_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |import android.app.Activity
                |import android.os.Bundle
                |import android.view.View
                |
                |class TestClass : Activity()
            """.trimMargin()
        ).inKotlin toHave NoErrors
    }

    @Test
    fun inKotlinAndroidClass_whenFileHasFrameworkImport_detectsErrors() {
        expect(
            """
                |package foo
                |
                |import android.app.Activity
                |import android.os.Bundle
                |import android.view.View
                |import com.squareup.retrofit2.*;
                |
                |class TestClass : Activity()
            """.trimMargin()
        ).inKotlin toHave SomeError
    }
}