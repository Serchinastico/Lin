package com.serchinastico.lin.detectors

import com.serchinastico.lin.detectors.test.LintTest
import com.serchinastico.lin.detectors.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.detectors.test.LintTest.Expectation.SomeError
import org.junit.Test

class NoElseInSwitchWithEnumOrSealedDetectorTest : LintTest {

    override val issue = NoElseInSwitchWithEnumOrSealedDetector.issue

    @Test
    fun inJavaSwitchStatement_whenAllCasesAreCovered_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |class TestClass {
                |   public static void main(String[] args) {
                |       NumberUpToFive number = NumberUpToFive.Four;
                |       switch (number) {
                |       case One: System.out.println("One"); break;
                |       case Two: System.out.println("Two"); break;
                |       case Three: System.out.println("Three"); break;
                |       case Four: System.out.println("Four"); break;
                |       case Five: System.out.println("Five"); break;
                |       }
                |   }
                |
                |   private enum NumberUpToFive {
                |     One, Two, Three, Four, Five
                |   }
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inJavaSwitchStatement_whenDefaultCaseIsUsed_detectsErrors() {
        expect(
            """
                |package foo;
                |
                |class TestClass {
                |   public static void main(String[] args) {
                |       NumberUpToFive number = NumberUpToFive.Four;
                |       switch (number) {
                |       case One: System.out.println("One"); break;
                |       case Two: System.out.println("Two"); break;
                |       case Three: System.out.println("Three"); break;
                |       case Four: System.out.println("Four"); break;
                |       default: System.out.println("Five"); break;
                |       }
                |   }
                |
                |   private enum NumberUpToFive {
                |     One, Two, Three, Four, Five
                |   }
                |}
            """.inJava
        ) toHave SomeError("src/foo/TestClass.java")
    }

    @Test
    fun inJavaSwitchStatement_whenSwitchArgumentIsFunctionParameter_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |class TestClass {
                |   public static void main(NumberUpToFive number) {
                |       switch (number) {
                |       case One: System.out.println("One"); break;
                |       case Two: System.out.println("Two"); break;
                |       case Three: System.out.println("Three"); break;
                |       case Four: System.out.println("Four"); break;
                |       case Five: System.out.println("Five"); break;
                |       }
                |   }
                |
                |   private enum NumberUpToFive {
                |     One, Two, Three, Four, Five
                |   }
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inKotlinWhenStatement_whenAllCasesAreCovered_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |   fun main(args: Array<String>) {
                |       val number = NumberUpToFive.Four
                |       when (number) {
                |           One -> System.out.println("One")
                |           Two -> System.out.println("Two")
                |           Three -> System.out.println("Three")
                |           Four -> System.out.println("Four")
                |           Five -> System.out.println("Five")
                |       }
                |   }
                |
                |   enum class NumberUpToFive {
                |     One, Two, Three, Four, Five
                |   }
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinWhenStatement_whenElseIsUsed_detectsErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |   fun main(args: Array<String>) {
                |       val number = NumberUpToFive.Four
                |       when (number) {
                |           One -> System.out.println("One")
                |           Two -> System.out.println("Two")
                |           Three -> System.out.println("Three")
                |           Four -> System.out.println("Four")
                |           else -> System.out.println("Five")
                |       }
                |   }
                |
                |   enum class NumberUpToFive {
                |     One, Two, Three, Four, Five
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }


    @Test
    fun inKotlinWhenExpression_whenWhenArgumentIsFunctionParameter_detectsErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |   fun main(number: NumberUpToFive) = when (number) {
                |       One -> System.out.println("One")
                |       Two -> System.out.println("Two")
                |       Three -> System.out.println("Three")
                |       Four -> System.out.println("Four")
                |       else -> System.out.println("Five")
                |   }
                |
                |   enum class NumberUpToFive {
                |     One, Two, Three, Four, Five
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }

    @Test
    fun inKotlinWhenExpression_whenArgumentTypeIsSealedClass_detectsErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |   fun main(number: NumberUpToFive) = when (number) {
                |       One -> System.out.println("One")
                |       Two -> System.out.println("Two")
                |       Three -> System.out.println("Three")
                |       Four -> System.out.println("Four")
                |       else -> System.out.println("Five")
                |   }
                |
                |   sealed class NumberUpToFive {
                |     object One: NumberUpToFive()
                |     object Two: NumberUpToFive()
                |     object Three: NumberUpToFive()
                |     object Four: NumberUpToFive()
                |     object Five: NumberUpToFive()
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }
}