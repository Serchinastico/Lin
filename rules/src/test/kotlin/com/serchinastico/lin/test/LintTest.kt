package com.serchinastico.lin.test

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.TextFormat
import com.serchinastico.lin.dsl.exhaustive

interface LintTest {

    val issue: Issue

    data class Snippet(val code: String, val language: Language)

    val String.inJava: Snippet get() = Snippet(this.trimMargin(), Language.Java)
    val String.inKotlin: Snippet get() = Snippet(this.trimMargin(), Language.Kotlin)

    data class ExpectBuilder(val issue: Issue, val snippets: List<Snippet>) {
        infix fun toHave(expectation: Expectation) {
            val result = lint()
                .files(
                    *snippets.map {
                        when (it.language) {
                            Language.Java -> java(it.code)
                            Language.Kotlin -> kotlin(it.code)
                        }
                    }.toTypedArray()
                )
                .issues(issue)
                .run()

            when (expectation) {
                Expectation.NoErrors -> result.expectClean()
                is Expectation.SomeWarning -> result.expect(
                    """
                        |${expectation.fileName}: Warning: ${issue.getBriefDescription(TextFormat.TEXT)} [${issue.id}]
                        |0 errors, 1 warnings
                    """.trimMargin()
                )
                is Expectation.SomeError -> result.expect(
                    """
                        |${expectation.fileName}: Error: ${issue.getBriefDescription(TextFormat.TEXT)} [${issue.id}]
                        |1 errors, 0 warnings
                    """.trimMargin()
                )
            }.exhaustive
        }

    }

    fun expect(snippet: Snippet): ExpectBuilder = ExpectBuilder(issue, listOf(snippet))
    fun expect(vararg snippets: Snippet): ExpectBuilder = ExpectBuilder(issue, snippets.toList())

    enum class Language {
        Java, Kotlin
    }

    sealed class Expectation {
        object NoErrors : Expectation()
        data class SomeWarning(val fileName: String) : Expectation()
        data class SomeError(val fileName: String) : Expectation()
    }
}