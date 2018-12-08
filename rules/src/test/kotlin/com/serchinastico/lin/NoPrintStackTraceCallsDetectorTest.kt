package com.serchinastico.lin

import com.serchinastico.lin.rules.NoPrintStackTraceCallsDetector
import com.serchinastico.lin.test.LintTest
import com.serchinastico.lin.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.test.LintTest.Expectation.SomeError
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
            """.trimMargin()
        ).inJava toHave NoErrors
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
            """.trimMargin()
        ).inJava toHave SomeError
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
            """.trimMargin()
        ).inJava toHave SomeError
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
            """.trimMargin()
        ).inKotlin toHave NoErrors
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
            """.trimMargin()
        ).inKotlin toHave SomeError
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
            """.trimMargin()
        ).inKotlin toHave SomeError
    }
}