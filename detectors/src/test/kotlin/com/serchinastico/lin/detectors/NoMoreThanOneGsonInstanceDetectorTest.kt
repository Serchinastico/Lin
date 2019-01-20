package com.serchinastico.lin.detectors

import com.serchinastico.lin.test.LintTest
import com.serchinastico.lin.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.test.LintTest.Expectation.SomeError
import org.junit.Test

class NoMoreThanOneGsonInstanceDetectorTest : LintTest {

    override val issue = NoMoreThanOneGsonInstanceDetector.issue

    private val gsonLibrary =
        """|package com.google.gson
           |
           |class Gson
        """.inKotlin

    @Test
    fun inJavaClass_whenGsonIsNotInstantiated_detectsNoErrors() {
        expect(
            gsonLibrary,
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
            gsonLibrary,
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
            gsonLibrary,
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
            gsonLibrary,
            """
                |package foo;
                |
                |import com.google.gson.Gson;
                |
                |class TestClass1 {
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
                |class TestClass2 {
                |   public void main(String[] args) {
                |       new Gson();
                |   }
                |}
            """.inJava
        ) toHave SomeError("project0")
    }

    @Test
    fun inKotlinClass_whenGsonIsNotInstantiated_detectsNoErrors() {
        expect(
            gsonLibrary,
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
            gsonLibrary,
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
            gsonLibrary,
            """
                |package foo
                |
                |import com.google.gson.Gson
                |
                |class TestClass {
                |   public fun main(args: Array<String>) {
                |       val gson1 = Gson()
                |       val gson2 = Gson()
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }

    @Test
    fun inKotlinClass_whenGsonIsInstantiatedTwiceInTwoFiles_detectsErrors() {
        expect(
            gsonLibrary,
            """
                |package foo
                |
                |import com.google.gson.Gson
                |
                |class TestClass1 {
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
                |class TestClass2 {
                |   public fun main(args: Array<String>) {
                |       Gson()
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("project0")
    }
}