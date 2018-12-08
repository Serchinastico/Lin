package com.serchinastico.lin.dsl

import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UFile
import org.jetbrains.uast.UImportStatement
import java.util.*
import kotlin.reflect.KClass

data class LinRule(val issueBuilder: IssueBuilder) {

    private var file: LinFile? = null

    fun file(block: LinFile.() -> LinFile): LinRule {
        file = LinFile().block()
        return this
    }

    fun matches(node: UFile): Boolean {
        val file = file ?: return false

        if (!file.anyImport(node.imports)) {
            return false
        }

        if (!file.anyType(node.classes)) {
            return false
        }

        return true
    }
}

class LinFile : SuchThat<UFile> by SuchThatStored() {

    private val importRules: MutableList<LinImport> = mutableListOf()
    private val typeRules: MutableList<LinType> = mutableListOf()

    fun import(block: LinImport.() -> LinImport): LinFile {
        importRules.add(LinImport().block())
        return this
    }

    fun type(block: LinType.() -> LinType): LinFile {
        typeRules.add(LinType().block())
        return this
    }

    fun anyImport(imports: List<UImportStatement>): Boolean = imports.any { import ->
        importRules.any { rule ->
            rule.suchThatPredicate?.invoke(import) ?: false
        }
    }

    fun anyType(types: List<UClass>): Boolean = types.any { type ->
        typeRules.any { rule ->
            rule.suchThatPredicate?.invoke(type) ?: false
        }
    }
}

fun rule(issueBuilder: IssueBuilder, block: LinRule.() -> LinRule): LinRule = LinRule(issueBuilder).block()


class LinImport : SuchThat<UImportStatement> by SuchThatStored()

class LinType : SuchThat<UClass> by SuchThatStored()

class SuchThatStored<T> : SuchThat<T> {
    override var suchThatPredicate: ((T) -> Boolean)? = null

    override fun <S : SuchThat<T>> suchThat(predicate: (T) -> Boolean): S {
        suchThatPredicate = predicate
        return this as S
    }
}

interface SuchThat<T> {
    val suchThatPredicate: ((T) -> Boolean)?
    fun <S : SuchThat<T>> suchThat(predicate: (T) -> Boolean): S
}

fun main(args: Array<String>) {
    val detectorScope = Scope.JAVA_FILE_SCOPE

    rule(
        issue(
            "NoDataFrameworksFromAndroidClass",
            detectorScope,
            "Framework classes to get or store data should never be called from Activities, Fragments or any other" +
                    " Android related view.",
            "Your Android classes should not be responsible for retrieving or storing information, that should be " +
                    "responsibility of another classes.",
            Category.INTEROPERABILITY
        )
    ) {
        file {
            import { suchThat { it.isFrameworkLibraryImport } }
            type { suchThat { node -> node.uastSuperTypes.any { it.isAndroidFrameworkType } } }
        }
    }
}

fun issue(
    id: String,
    scope: EnumSet<Scope>,
    description: String,
    explanation: String,
    category: Category
): IssueBuilder {
    return IssueBuilder(id, scope, description, explanation, category)
}

data class IssueBuilder(
    val id: String,
    val scope: EnumSet<Scope>,
    val description: String,
    val explanation: String,
    val category: Category,
    var priority: Int = 5,
    var severity: Severity = Severity.ERROR
) {
    fun <T : Detector> build(detectorClass: KClass<T>): Issue =
        Issue.create(
            id,
            description,
            explanation,
            category,
            priority,
            severity,
            Implementation(detectorClass.java, scope)
        )
}