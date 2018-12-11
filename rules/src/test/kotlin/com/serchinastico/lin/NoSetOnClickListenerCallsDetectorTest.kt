package com.serchinastico.lin

import com.serchinastico.lin.rules.NoSetOnClickListenerCallsDetector
import com.serchinastico.lin.test.LintTest
import com.serchinastico.lin.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.test.LintTest.Expectation.SomeError
import org.junit.Test

class NoSetOnClickListenerCallsDetectorTest : LintTest {

    override val issue = NoSetOnClickListenerCallsDetector.issue

    @Test
    fun inJavaClass_whenCallIsNotSetOnClickListener_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |import android.view.View;
                |
                |class TestClass implements View.OnClickListener {
                |   private View view;
                |
                |   public void main(String[] args) {
                |       view.doNotSetOnClickListener(this);
                |   }
                |}
            """.trimMargin()
        ).inJava toHave NoErrors
    }

    @Test
    fun inJavaClass_whenCallIsSetOnClickListener_detectsError() {
        expect(
            """
                |package foo;
                |
                |import android.view.View;
                |
                |class TestClass implements View.OnClickListener {
                |   private View view;
                |
                |   public void main(String[] args) {
                |       view.setOnClickListener(this);
                |   }
                |}
            """.trimMargin()
        ).inJava toHave SomeError
    }

    @Test
    fun inJavaClass_whenCallIsNotAndroidViewSetOnClickListener_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |class TestClass implements View.OnClickListener {
                |   public void main(String[] args) {
                |       setOnClickListener(this);
                |   }
                |
                |   private void setOnClickListener(View.OnClickListener listener) {}
                |}
            """.trimMargin()
        ).inJava toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenCallIsNotSetOnClickListener_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |import android.view.View
                |
                |class TestClass: View.OnClickListener {
                |   private lateinit var view: View
                |
                |   public fun main(args: Array<String>) {
                |       view.doNotSetOnClickListener(this)
                |   }
                |}
            """.trimMargin()
        ).inKotlin toHave NoErrors
    }


    @Test
    fun inKotlinClass_whenCallIsSetOnClickListener_detectsError() {
        expect(
            """
                |package foo
                |
                |import android.view.View
                |
                |class TestClass: View.OnClickListener {
                |   private lateinit var view: View
                |
                |   public fun main(args: Array<String>) {
                |       view.setOnClickListener(this)
                |   }
                |}
            """.trimMargin()
        ).inKotlin toHave SomeError
    }

    @Test
    fun inKotlinClass_whenCallIsNotAndroidViewSetOnClickListener_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass: View.OnClickListener {
                |   public fun main(args: Array<String>) {
                |       setOnClickListener(this)
                |   }
                |
                |   private fun setOnClickListener(listener: View.OnClickListener) {}
                |}
            """.trimMargin()
        ).inKotlin toHave NoErrors
    }
}