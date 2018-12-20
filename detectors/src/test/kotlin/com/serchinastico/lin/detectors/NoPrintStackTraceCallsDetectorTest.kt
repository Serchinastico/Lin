package com.serchinastico.lin.detectors

import com.serchinastico.lin.detectors.test.LintTest
import com.serchinastico.lin.detectors.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.detectors.test.LintTest.Expectation.SomeError
import org.junit.Test

class NoPrintStackTraceCallsDetectorTest : LintTest {

    override val issue = NoPrintStackTraceCallsDetector.issue

    @Test
    fun inJavaNonThrowableClass_whenCallIsPrintStackTrace_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |class TestClass {
                |   public static void main(String[] args) {
                |       new TestClass().printStackTrace();
                |   }
                |
                |   private void printStackTrace() {}
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inJavaThrowableChildClass_whenCallIsPrintStackTrace_detectsErrors() {
        expect(
            """
                |package foo;
                |
                |class TestClass extends java.lang.Throwable {
                |   public static void main(String[] args) {
                |       new TestClass().printStackTrace();
                |   }
                |}
            """.inJava
        ) toHave SomeError("src/foo/TestClass.java")
    }

    @Test
    fun inJavaThrowableClass_whenCallIsPrintStackTrace_detectsErrors() {
        expect(
            """
                |package foo;
                |
                |class TestClass {
                |   public static void main(String[] args) {
                |       new java.lang.Throwable().printStackTrace();
                |   }
                |}
            """.inJava
        ) toHave SomeError("src/foo/TestClass.java")
    }

    @Test
    fun inKotlinNonThrowableClass_whenCallIsPrintStackTrace_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |   fun main(args: Array<String>) {
                |       TestClass().printStackTrace();
                |   }
                |
                |   private fun printStackTrace() {}
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinThrowableChildClass_whenCallIsPrintStackTrace_detectsErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass: kotlin.Throwable() {
                |   fun main(args: Array<String>) {
                |       TestClass().printStackTrace();
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }

    @Test
    fun inKotlinThrowableClass_whenCallIsPrintStackTrace_detectsErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |   fun main(args: Array<String>) {
                |       kotlin.Throwable().printStackTrace();
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }
}