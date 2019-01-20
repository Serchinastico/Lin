package com.serchinastico.lin.detectors

import com.serchinastico.lin.detectors.test.LintTest
import com.serchinastico.lin.detectors.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.detectors.test.LintTest.Expectation.SomeError
import org.junit.Test

class NoFindViewByIdCallsDetectorTest : LintTest {

    override val issue = NoFindViewByIdCallsDetector.issue

    private val resourcesFile = """
            |package foo;
            |
            |public final class R {
            |   public static final class id {
            |       public static final int my_button=0x7f010000;
            |   }
            |}
        """.inJava


    @Test
    fun inJavaClass_whenCallIsNotFindViewById_detectsNoErrors() {
        expect(
            resourcesFile,
            """
                |package foo;
                |
                |import android.view.View;
                |import foo.R;
                |
                |class TestClass {
                |   private View view;
                |
                |   public void main(String[] args) {
                |       view.doNotFindViewById(R.id.my_button);
                |   }
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inJavaClass_whenCallIsFindViewById_detectsError() {
        expect(
            resourcesFile,
            """
                |package foo;
                |
                |import android.view.View;
                |import foo.R;
                |
                |class TestClass {
                |   private View view;
                |
                |   public void main(String[] args) {
                |       view.findViewById(R.id.my_button);
                |   }
                |}
            """.inJava
        ) toHave SomeError("src/foo/TestClass.java")
    }

    @Test
    fun inJavaClass_whenCallIsNotAndroidViewSetOnClickListener_detectsNoErrors() {
        expect(
            resourcesFile,
            """
                |package foo;
                |
                |import foo.R;
                |
                |class TestClass {
                |   public void main(String[] args) {
                |       findViewById(R.id.my_button);
                |   }
                |
                |   private void findViewById(int id) {}
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenCallIsNotFindViewById_detectsNoErrors() {
        expect(
            resourcesFile,
            """
                |package foo
                |
                |import android.view.View
                |import foo.R
                |
                |class TestClass {
                |   private lateinit var view: View
                |
                |   public fun main(args: Array<String>) {
                |       view.doNotFindViewById(R.id.my_button)
                |   }
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenCallIsFindViewById_detectsError() {
        expect(
            resourcesFile,
            """
                |package foo
                |
                |import android.view.View
                |import foo.R
                |
                |class TestClass {
                |   private lateinit var view: View
                |
                |   public fun main(args: Array<String>) {
                |       view.findViewById(R.id.my_button)
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }

    @Test
    fun inKotlinClass_whenCallIsNotAndroidViewSetOnClickListener_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |import foo.R
                |
                |class TestClass {
                |   public fun main(args: Array<String>) {
                |       findViewById(R.id.my_button)
                |   }
                |
                |   private fun findViewById(id: Int) {}
                |}
            """.inKotlin
        ) toHave NoErrors
    }
}