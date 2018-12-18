package com.serchinastico.lin

import com.serchinastico.lin.rules.NoDataFrameworksFromAndroidClassDetector
import com.serchinastico.lin.test.LintTest
import com.serchinastico.lin.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.test.LintTest.Expectation.SomeError
import org.junit.Test

class NoDataFrameworksFromAndroidClassDetectorTest : LintTest {

    override val issue = NoDataFrameworksFromAndroidClassDetector.issue

    @Test
    fun inJavaNonAndroidClass_whenFileHasFrameworkImport_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |import com.squareup.retrofit2.*;
            """.inJava
        ) toHave NoErrors
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
            """.inJava
        ) toHave NoErrors
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
            """.inJava
        ) toHave SomeError("src/foo/TestClass.java")
    }

    @Test
    fun inKotlinNonAndroidClass_whenFileHasFrameworkImport_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |import com.squareup.retrofit2.*
            """.inKotlin
        ) toHave NoErrors
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
            """.inKotlin
        ) toHave NoErrors
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
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }
}