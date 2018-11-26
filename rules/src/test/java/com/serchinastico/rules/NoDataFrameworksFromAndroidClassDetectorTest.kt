package com.serchinastico.rules

import com.serchinastico.rules.test.LintTest
import com.serchinastico.rules.test.LintTest.Expectation.NoErrors
import com.serchinastico.rules.test.LintTest.Expectation.SomeError
import org.junit.Test

class NoDataFrameworksFromAndroidClassDetectorTest : LintTest {

    override val issue = NoDataFrameworksFromAndroidClassDetector.ISSUE

    @Test
    fun inJavaNonAndroidClass_whenFileHasFrameworkImport_detectsNoErrors() {
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