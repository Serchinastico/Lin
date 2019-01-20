package com.serchinastico.lin.detectors

import com.serchinastico.lin.test.LintTest
import com.serchinastico.lin.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.test.LintTest.Expectation.SomeError
import org.junit.Test

class NoMoreThanOneDateInstanceDetectorTest : LintTest {

    override val issue = NoMoreThanOneDateInstanceDetector.issue

    @Test
    fun inJavaClass_whenDateIsNotInstantiated_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |import java.util.Date;
                |
                |class TestClass {
                |   public void main(String[] args) {}
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inJavaClass_whenDateIsInstantiatedOnce_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |import java.util.Date;
                |
                |class TestClass {
                |   public void main(String[] args) {
                |       new Date();
                |   }
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inJavaClass_whenDateIsInstantiatedTwice_detectsError() {
        expect(
            """
                |package foo;
                |
                |import java.util.Date;
                |
                |class TestClass {
                |   public void main(String[] args) {
                |       new Date();
                |       new Date();
                |   }
                |}
            """.inJava
        ) toHave SomeError("src/foo/TestClass.java")
    }

    @Test
    fun inJavaClass_whenDateIsInstantiatedTwiceInDifferentFiles_detectsErrors() {
        expect(
            """
                |package foo;
                |
                |import java.util.Date;
                |
                |class TestClass1 {
                |   public void main(String[] args) {
                |       new Date();
                |   }
                |}
            """.inJava,
            """
                |package foo;
                |
                |import java.util.Date;
                |
                |class TestClass2 {
                |   public void main(String[] args) {
                |       new Date();
                |   }
                |}
            """.inJava
        ) toHave SomeError("project0")
    }

    @Test
    fun inKotlinClass_whenDateIsNotInstantiated_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |import java.util.Date;
                |
                |class TestClass {
                |   public fun main(args: Array<String>) {}
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenDateIsInstantiatedOnce_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |import java.util.Date;
                |
                |class TestClass {
                |   public fun main(args: Array<String>) {
                |       Date()
                |   }
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenDateIsInstantiatedTwice_detectsError() {
        expect(
            """
                |package foo
                |
                |import java.util.Date;
                |
                |class TestClass {
                |   public fun main(args: Array<String>) {
                |       val date1 = Date()
                |       val date2 = Date()
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }

    @Test
    fun inKotlinClass_whenDateIsInstantiatedTwiceInTwoFiles_detectsErrors() {
        expect(
            """
                |package foo
                |
                |import java.util.Date;
                |
                |class TestClass1 {
                |   public fun main(args: Array<String>) {
                |       Date()
                |   }
                |}
            """.inKotlin,
            """
                |package foo
                |
                |import java.util.Date;
                |
                |class TestClass2 {
                |   public fun main(args: Array<String>) {
                |       Date()
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("project0")
    }
}