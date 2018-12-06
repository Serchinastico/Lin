package com.serchinastico.lin

import com.serchinastico.lin.rules.NoPublicViewPropertiesDetector
import com.serchinastico.lin.test.LintTest
import com.serchinastico.lin.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.test.LintTest.Expectation.SomeError
import org.junit.Test

class NoPublicViewPropertiesDetectorTest : LintTest {

    override val issue = NoPublicViewPropertiesDetector.ISSUE

    @Test
    fun inJavaClass_whenViewFieldIsPrivate_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |import android.view.View;
                |
                |class TestClass {
                |   private View view;
                |}
            """.trimMargin()
        ).inJava toHave NoErrors
    }

    @Test
    fun inJavaClass_whenViewFieldIsNonPrivate_detectsErrors() {
        expect(
            """
                |package foo;
                |
                |import android.view.View;
                |
                |class TestClass {
                |   View view;
                |}
            """.trimMargin()
        ).inJava toHave SomeError
    }


    @Test
    fun inKotlinClass_whenViewFieldIsPrivate_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |import android.view.View
                |
                |class TestClass {
                |   private val view: View = View()
                |}
            """.trimMargin()
        ).inKotlin toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenViewFieldIsNotPrivate_detectsErrors() {
        expect(
            """
                |package foo
                |
                |import android.view.View
                |
                |class TestClass {
                |   public val view: View = View(null)
                |}
            """.trimMargin()
        ).inKotlin toHave SomeError
    }
}