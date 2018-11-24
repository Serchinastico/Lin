package com.serchinastico.rules

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class NoPrintStackTraceCallsDetectorTest {
    @Test
    fun inJavaNonThrowableClass_whenCallIsPrintStackTrace_detectsNoErrors() {
        lint()
            .files(
                java(
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
                )
            )
            .issues(NoPrintStackTraceCallsDetector.ISSUE)
            .run()
            .expect(
                """
                    |No warnings.
                """.trimMargin()
            )
    }

    @Test
    fun inJavaThrowableChildClass_whenCallIsPrintStackTrace_detectsErrors() {
        lint()
            .files(
                java(
                    """
                        |package foo;
                        |
                        |class TestClass extends java.lang.Throwable {
                        |   public static void main(String[] args) {
                        |       new TestClass().printStackTrace();
                        |   }
                        |}
                    """.trimMargin()
                )
            )
            .issues(NoPrintStackTraceCallsDetector.ISSUE)
            .run()
            .expect(
                """
                    |src/foo/TestClass.java: Error: There should not be calls to the printStackTrace method in Throwable instances [NoPrintStackTraceCalls]
                    |1 errors, 0 warnings
                """.trimMargin()
            )
    }

    @Test
    fun inJavaThrowableClass_whenCallIsPrintStackTrace_detectsErrors() {
        lint()
            .files(
                java(
                    """
                        |package foo;
                        |
                        |class TestClass {
                        |   public static void main(String[] args) {
                        |       new java.lang.Throwable().printStackTrace();
                        |   }
                        |}
                    """.trimMargin()
                )
            )
            .issues(NoPrintStackTraceCallsDetector.ISSUE)
            .run()
            .expect(
                """
                    |src/foo/TestClass.java: Error: There should not be calls to the printStackTrace method in Throwable instances [NoPrintStackTraceCalls]
                    |1 errors, 0 warnings
                """.trimMargin()
            )
    }

    @Test
    fun inKotlinNonThrowableClass_whenCallIsPrintStackTrace_detectsNoErrors() {
        lint()
            .files(
                kotlin(
                    """
                        |package foo;
                        |
                        |class TestClass {
                        |   fun main(args: Array<String>) {
                        |       TestClass().printStackTrace();
                        |   }
                        |
                        |   private fun printStackTrace() {}
                        |}
                    """.trimMargin()
                )
            )
            .issues(NoPrintStackTraceCallsDetector.ISSUE)
            .run()
            .expect(
                """
                    |No warnings.
                """.trimMargin()
            )
    }

    @Test
    fun inKotlinThrowableChildClass_whenCallIsPrintStackTrace_detectsErrors() {
        lint()
            .files(
                kotlin(
                    """
                        |package foo;
                        |
                        |class TestClass: kotlin.Throwable() {
                        |   fun main(args: Array<String>) {
                        |       TestClass().printStackTrace();
                        |   }
                        |}
                    """.trimMargin()
                )
            )
            .issues(NoPrintStackTraceCallsDetector.ISSUE)
            .run()
            .expect(
                """
                    |src/foo/TestClass.kt: Error: There should not be calls to the printStackTrace method in Throwable instances [NoPrintStackTraceCalls]
                    |1 errors, 0 warnings
                """.trimMargin()
            )
    }

    @Test
    fun inKotlinThrowableClass_whenCallIsPrintStackTrace_detectsErrors() {
        lint()
            .files(
                kotlin(
                    """
                        |package foo;
                        |
                        |class TestClass {
                        |   fun main(args: Array<String>) {
                        |       kotlin.Throwable().printStackTrace();
                        |   }
                        |}
                    """.trimMargin()
                )
            )
            .issues(NoPrintStackTraceCallsDetector.ISSUE)
            .run()
            .expect(
                """
                    |src/foo/TestClass.kt: Error: There should not be calls to the printStackTrace method in Throwable instances [NoPrintStackTraceCalls]
                    |1 errors, 0 warnings
                """.trimMargin()
            )
    }
}