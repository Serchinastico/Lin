package com.serchinastico.lin.dsl

import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import java.util.*
import kotlin.reflect.KClass

data class LinRule(val issueBuilder: IssueBuilder) {

    private var lNode: LNode<*>? = null

    val applicableTypes: List<Class<out UElement>>
        get() = lNode?.applicableType?.let { listOf(it) } ?: emptyList()

    fun shouldReport(node: UElement): Boolean = lNode?.match(node) ?: false

    fun file(block: LNode.LFile.() -> LNode.LFile): LinRule {
        lNode = LNode.LFile().block()
        return this
    }

    fun import(block: LNode.LImport.() -> LNode.LImport): LinRule {
        lNode = LNode.LImport().block()
        return this
    }

    fun type(block: LNode.LType.() -> LNode.LType): LinRule {
        lNode = LNode.LType().block()
        return this
    }

    fun switch(block: LNode.LSwitchExpression.() -> LNode.LSwitchExpression): LinRule {
        lNode = LNode.LSwitchExpression().block()
        return this
    }

    fun call(block: LNode.LCallExpression.() -> LNode.LCallExpression): LinRule {
        lNode = LNode.LCallExpression().block()
        return this
    }

    fun field(block: LNode.LField.() -> LNode.LField): LinRule {
        lNode = LNode.LField().block()
        return this
    }
}

sealed class LNode<T : UElement> {

    abstract val applicableType: Class<out T>
    internal val children: MutableList<LNode<*>> = mutableListOf()
    private var suchThatPredicate: ((T) -> Boolean)? = null

    fun match(element: UElement): Boolean {
        val predicate = suchThatPredicate ?: { true }
        return predicate.invoke(applicableType.cast(element)) && children.all { child ->
            child.lookForChildren(element).any { child.match(it) }
        }
    }

    fun <S : LNode<*>> suchThat(predicate: (T) -> Boolean): S {
        suchThatPredicate = predicate
        return this as S
    }

    open fun lookForChildren(element: UElement): List<T> = emptyList()

    class LFile : LNode<UFile>() {

        fun import(block: LImport.() -> LImport): LFile {
            children.add(LImport().block())
            return this
        }

        fun type(block: LType.() -> LType): LFile {
            children.add(LType().block())
            return this
        }

        override val applicableType: Class<out UFile> = UFile::class.java
    }

    class LImport : LNode<UImportStatement>() {
        override val applicableType: Class<out UImportStatement> = UImportStatement::class.java

        override fun lookForChildren(element: UElement): List<UImportStatement> = when (element) {
            is UFile -> element.imports
            else -> super.lookForChildren(element)
        }
    }

    class LType : LNode<UClass>() {
        override val applicableType: Class<out UClass> = UClass::class.java

        override fun lookForChildren(element: UElement): List<UClass> = when (element) {
            is UFile -> element.classes
            else -> super.lookForChildren(element)
        }

        fun switch(block: LSwitchExpression.() -> LSwitchExpression): LType {
            children.add(LSwitchExpression().block())
            return this
        }

        fun calls(block: LCallExpression.() -> LCallExpression): LType {
            children.add(LCallExpression().block())
            return this
        }

        fun field(block: LField.() -> LField): LType {
            children.add(LField().block())
            return this
        }
    }

    class LSwitchExpression : LNode<USwitchExpression>() {
        override val applicableType: Class<out USwitchExpression> = USwitchExpression::class.java
    }

    class LCallExpression : LNode<UCallExpression>() {
        override val applicableType: Class<out UCallExpression> = UCallExpression::class.java
    }

    class LField : LNode<UField>() {
        override val applicableType: Class<out UField> = UField::class.java
    }
}

fun rule(issueBuilder: IssueBuilder, block: LinRule.() -> LinRule): LinRule = LinRule(issueBuilder).block()

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