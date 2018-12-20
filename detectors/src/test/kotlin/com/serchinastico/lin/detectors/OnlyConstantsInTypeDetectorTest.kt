package com.serchinastico.lin.detectors

import com.serchinastico.lin.detectors.test.LintTest
import com.serchinastico.lin.detectors.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.detectors.test.LintTest.Expectation.SomeError
import org.junit.Test

class OnlyConstantsInTypeDetectorTest : LintTest {

    override val issue = OnlyConstantsInTypeDetector.issue

    @Test
    fun inJavaClass_whenClassHasMethods_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |class TestClass {
                |   public static final String str = "";
                |
                |   public void main(String[] args) {}
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inJavaClass_whenClassHasNonStaticFields_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |class TestClass {
                |   public static final String str = "";
                |   private String nonStaticStr;
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inJavaClass_whenClassHasNoFieldsNorMethods_detectsError() {
        expect(
            """
                |package foo;
                |
                |class TestClass {
                |   public static final String str;
                |}
            """.inJava
        ) toHave SomeError("src/foo/TestClass.java")
    }

    @Test
    fun inKotlinClass_whenClassHasMethods_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |
                |   companion object {
                |     const val str: String = ""
                |   }
                |
                |   public fun main(args: Array<String>) {}
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenClassHasNonStaticFields_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |
                |   companion object {
                |     const val str: String = ""
                |   }
                |
                |   val anotherStr: String = ""
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenClassHasNoFieldsNorMethods_detectsErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |   companion object {
                |     const val str: String = ""
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }

    @Test
    fun inKotlinObject_whenItHasNoFieldsNorMethods_detectsErrors() {
        expect(
            """
                |package foo
                |
                |object TestClass {
                |   const val str: String = ""
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }
}