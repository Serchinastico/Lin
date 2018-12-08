package com.serchinastico.lin.dsl

import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import java.util.*
import kotlin.reflect.KClass

data class LinRule(val issueBuilder: IssueBuilder) {

    private var file: LinFile? = null
    private var switchExpression: LinSwitchExpression? = null
    private var callExpression: LinCallExpression? = null
    private var field: LinField? = null

    val applicableTypes: List<Class<out UElement>>
        get() = when {
            file != null -> listOf(UFile::class.java)
            switchExpression != null -> listOf(USwitchExpression::class.java)
            callExpression != null -> listOf(UCallExpression::class.java)
            this.field != null -> listOf(UField::class.java)
            else -> emptyList()
        }

    fun file(block: LinFile.() -> LinFile): LinRule {
        file = LinFile().block()
        return this
    }

    fun switch(block: LinSwitchExpression.() -> LinSwitchExpression): LinRule {
        switchExpression = LinSwitchExpression().block()
        return this
    }

    fun callExpression(block: LinCallExpression.() -> LinCallExpression): LinRule {
        callExpression = LinCallExpression().block()
        return this
    }

    fun field(block: LinField.() -> LinField): LinRule {
        field = LinField().block()
        return this
    }

    fun matches(node: UElement): Boolean = when (node) {
        is UFile -> matches(node)
        is USwitchExpression -> matches(node)
        is UCallExpression -> matches(node)
        is UField -> matches(node)
        else -> false
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

    private fun matches(node: USwitchExpression): Boolean {
        val switchExpression = switchExpression ?: return false
        return switchExpression.suchThatPredicate?.invoke(node) ?: false
    }

    private fun matches(node: UCallExpression): Boolean {
        val callExpression = callExpression ?: return false
        return callExpression.suchThatPredicate?.invoke(node) ?: false
    }

    private fun matches(node: UField): Boolean {
        val field = field ?: return false
        return field.suchThatPredicate?.invoke(node) ?: false
    }
}

class LinFile {

    private val importRules: MutableList<LinImport> = mutableListOf()
    private val typeRules: MutableList<LinType> = mutableListOf()
    private var suchThatPredicate: ((UFile) -> Boolean)? = null

    fun suchThat(predicate: (UFile) -> Boolean): LinFile {
        suchThatPredicate = predicate
        return this
    }

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

class LinImport {
    var suchThatPredicate: ((UImportStatement) -> Boolean)? = null

    fun suchThat(predicate: (UImportStatement) -> Boolean): LinImport {
        suchThatPredicate = predicate
        return this
    }
}

class LinType {
    var suchThatPredicate: ((UClass) -> Boolean)? = null

    fun suchThat(predicate: (UClass) -> Boolean): LinType {
        suchThatPredicate = predicate
        return this
    }
}

class LinSwitchExpression {
    var suchThatPredicate: ((USwitchExpression) -> Boolean)? = null

    fun suchThat(predicate: (USwitchExpression) -> Boolean): LinSwitchExpression {
        suchThatPredicate = predicate
        return this
    }
}

class LinCallExpression {
    var suchThatPredicate: ((UCallExpression) -> Boolean)? = null

    fun suchThat(predicate: (UCallExpression) -> Boolean): LinCallExpression {
        suchThatPredicate = predicate
        return this
    }
}

class LinField {
    var suchThatPredicate: ((UField) -> Boolean)? = null

    fun suchThat(predicate: (UField) -> Boolean): LinField {
        suchThatPredicate = predicate
        return this
    }
}

fun main(args: Array<String>) {
    val detectorScope = Scope.JAVA_FILE_SCOPE

    rule(
        issue(
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
    scope: EnumSet<Scope>,
    description: String,
    explanation: String,
    category: Category
): IssueBuilder {
    return IssueBuilder(scope, description, explanation, category)
}

data class IssueBuilder(
    val scope: EnumSet<Scope>,
    val description: String,
    val explanation: String,
    val category: Category,
    var priority: Int = 5,
    var severity: Severity = Severity.ERROR
) {
    fun <T : Detector> build(detectorClass: KClass<T>): Issue =
        Issue.create(
            detectorClass.simpleName ?: "RuleWithNoId",
            description,
            explanation,
            category,
            priority,
            severity,
            Implementation(detectorClass.java, scope)
        )
}