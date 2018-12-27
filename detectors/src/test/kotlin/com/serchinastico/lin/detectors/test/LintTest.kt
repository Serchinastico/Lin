package com.serchinastico.lin.detectors.test

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.detector.api.Issue
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
                is Expectation.SomeWarning -> result.expectWarningCount(1)
                is Expectation.SomeError -> result.expectErrorCount(1)
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