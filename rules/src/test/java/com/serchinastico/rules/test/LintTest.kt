package com.serchinastico.rules.test

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.TextFormat
import com.serchinastico.rules.exhaustive

interface LintTest {

    val issue: Issue

    data class SnippetBuilder(val issue: Issue, val snippet: String) {

        val inJava: ExpectBuilder
            get() = ExpectBuilder(issue, Language.Java, snippet)
        val inKotlin: ExpectBuilder
            get() = ExpectBuilder(issue, Language.Kotlin, snippet)
    }

    data class ExpectBuilder(val issue: Issue, val language: Language, val snippet: String) {
        infix fun toHave(expectation: Expectation) {
            val result = lint()
                .files(
                    when (language) {
                        Language.Java -> java(snippet)
                        Language.Kotlin -> kotlin(snippet)
                    }
                )
                .issues(issue)
                .run()

            when (expectation) {
                Expectation.NoErrors -> result.expectClean()
                Expectation.SomeWarning -> result.expect(
                    """
                        |src/foo/TestClass.${language.extension}: Warning: ${issue.getBriefDescription(TextFormat.TEXT)} [${issue.id}]
                        |0 errors, 1 warnings
                    """.trimMargin()
                )
                Expectation.SomeError -> result.expect(
                    """
                        |src/foo/TestClass.${language.extension}: Error: ${issue.getBriefDescription(TextFormat.TEXT)} [${issue.id}]
                        |1 errors, 0 warnings
                    """.trimMargin()
                )
            }.exhaustive
        }

    }

    fun expect(snippet: String): SnippetBuilder = SnippetBuilder(issue, snippet)

    enum class Language {
        Java, Kotlin;

        val extension: String
            get() = when (this) {
                Java -> "java"
                Kotlin -> "kt"
            }
    }

    sealed class Expectation {
        object NoErrors : Expectation()
        object SomeWarning : Expectation()
        object SomeError : Expectation()
    }
}