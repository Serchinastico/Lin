package com.serchinastico.lin.detectors

import com.serchinastico.lin.detectors.test.LintTest
import com.serchinastico.lin.detectors.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.detectors.test.LintTest.Expectation.SomeError
import org.junit.Test

class WrongSyntheticViewReferenceDetectorTest : LintTest {

    private val resourcesFile = """
            |package foo;
            |
            |public final class R {
            |   public static final class layout {
            |       public static final int activity_test_1=0x7f010000;
            |       public static final int activity_test_2=0x7f020000;
            |   }
            |}
        """.inJava

    override val issue = WrongSyntheticViewReferenceDetector.issue

    @Test
    fun inKotlinActivity_whenImportReferencesFromDifferentLayout_detectsError() {
        expect(
            resourcesFile,
            """
                |package foo
                |
                |import foo.R
                |import kotlinx.android.synthetic.main.activity_test_2.*
                |
                |class TestClass: Activity() {
                |   override val layoutId = R.layout.activity_test_1
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }

    @Test
    fun inKotlinActivity_whenImportReferencesFromBothSameAndDifferentLayout_detectsError() {
        expect(
            resourcesFile,
            """
                |package foo
                |
                |import foo.R
                |import kotlinx.android.synthetic.main.activity_test_1.*
                |import kotlinx.android.synthetic.main.activity_test_2.*
                |
                |class TestClass: Activity() {
                |   override val layoutId = R.layout.activity_test_1
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }

    @Test
    fun inKotlinActivity_whenImportReferencesFromSameLayout_detectsNoErrors() {
        expect(
            resourcesFile,
            """
                |package foo
                |
                |import foo.R
                |import kotlinx.android.synthetic.main.activity_test_1.*
                |
                |class TestClass: Activity() {
                |   override val layoutId = R.layout.activity_test_1
                |}
            """.inKotlin
        ) toHave NoErrors
    }
}