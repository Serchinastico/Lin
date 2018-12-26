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

    @Test
    fun inKotlinSealedClass_whenItHasNoFields_detectsNoErrors() {
        expect(
            """
            |package foo
            |
            |sealed class ActionStatus<out T> {
            |    class Ready<out T> : ActionStatus<T>()
            |    class OnGoing<out T> : ActionStatus<T>()
            |    class Finished<out T>(val value: T) : ActionStatus<T>()
            |}
        """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinInterface_whenItHasNoFields_detectsNoErrors() {
        expect(
            """
            |package foo

            |import okhttp3.RequestBody
            |import retrofit2.Call
            |import retrofit2.http.Body
            |import retrofit2.http.GET
            |import retrofit2.http.PUT
            |import retrofit2.http.Path
            |import retrofit2.http.Query
            |import retrofit2.http.Url
            |
            |interface SomeRetrofitApi {
            |    @GET("some/url")
            |    fun someApiCall(
            |        @Query("param1") param1: String,
            |        @Query("param2") param2: String
            |    ): Call<SomeResponse>
            |}
            |
            |typealias SomeResponse = Map<String, Any?>
        """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinDataClass_whenItHasNoFields_detectsNoErrors() {
        expect(
            """
            |package foo
            |
            |import org.joda.time.LocalDate
            |
            |typealias SomeMap = Map<LocalDate, List<Answer>>
            |
            |data class SomeDataClass(
            |    val someProperty: String,
            |    val someOtherProperty: String
            |)
            |
            |data class OtherDataClass(
            |    val someProperty: String,
            |    val someOtherProperty: String
            |)
            |
            |data class YetAnotherDataClass(
            |    val someProperty: String,
            |    private val someOtherProperty: String
            |) {
            |    val someNonConstructorProperty: String = ""
            |}
        """.inKotlin
        ) toHave NoErrors
    }
}