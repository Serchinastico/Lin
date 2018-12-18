package com.serchinastico.lin

import com.serchinastico.lin.rules.NoMoreThanOneGsonInstanceDetector
import com.serchinastico.lin.test.LintTest
import com.serchinastico.lin.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.test.LintTest.Expectation.SomeError
import org.junit.Test

class NoMoreThanOneGsonInstanceDetectorTest : LintTest {

    override val issue = NoMoreThanOneGsonInstanceDetector.issue

    @Test
    fun inJavaClass_whenGsonIsNotInstantiated_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |import com.google.gson.Gson;
                |
                |class TestClass {
                |   public void main(String[] args) {}
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inJavaClass_whenGsonIsInstantiatedOnce_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |import com.google.gson.Gson;
                |
                |class TestClass {
                |   public void main(String[] args) {
                |       new Gson();
                |   }
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inJavaClass_whenGsonIsInstantiatedTwice_detectsError() {
        expect(
            """
                |package foo;
                |
                |import com.google.gson.Gson;
                |
                |class TestClass {
                |   public void main(String[] args) {
                |       new Gson();
                |       new Gson();
                |   }
                |}
            """.inJava
        ) toHave SomeError("src/foo/TestClass.java")
    }

    @Test
    fun inJavaClass_whenGsonIsInstantiatedTwiceInDifferentFiles_detectsErrors() {
        expect(
            """
                |package foo;
                |
                |import com.google.gson.Gson;
                |
                |class TestClass {
                |   public void main(String[] args) {
                |       new Gson();
                |   }
                |}
            """.inJava,
            """
                |package foo;
                |
                |import com.google.gson.Gson;
                |
                |class TestClass {
                |   public void main(String[] args) {
                |       new Gson();
                |   }
                |}
            """.inJava
        ) toHave SomeError("src/foo/TestClass.java")
    }

    @Test
    fun inKotlinClass_whenGsonIsNotInstantiated_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |import com.google.gson.Gson
                |
                |class TestClass {
                |   public fun main(args: Array<String>) {}
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenGsonIsInstantiatedOnce_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |import com.google.gson.Gson
                |
                |class TestClass {
                |   public fun main(args: Array<String>) {
                |       Gson()
                |   }
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenGsonIsInstantiatedTwice_detectsError() {
        expect(
            """
                |package foo
                |
                |import com.google.gson.Gson
                |
                |class TestClass {
                |   public fun main(args: Array<String>) {
                |       Gson()
                |       Gson()
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }

    @Test
    fun inKotlinClass_whenGsonIsInstantiatedTwiceInTwoFiles_detectsErrors() {
        expect(
            """
                |package foo
                |
                |import com.google.gson.Gson
                |
                |class TestClass {
                |   public fun main(args: Array<String>) {
                |       Gson()
                |   }
                |}
            """.inKotlin,
            """
                |package foo
                |
                |import com.google.gson.Gson
                |
                |class TestClass {
                |   public fun main(args: Array<String>) {
                |       Gson()
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }
}