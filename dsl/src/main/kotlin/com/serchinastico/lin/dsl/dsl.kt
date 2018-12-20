package com.serchinastico.lin.dsl

import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import java.util.*
import kotlin.reflect.KClass

fun detector(
    issueBuilder: IssueBuilder,
    ruleSet: RuleSet
): LinDetector = LinDetector(issueBuilder, ruleSet.rules)

fun detector(
    issueBuilder: IssueBuilder,
    block: LinRule.File.() -> LinRule<*>
): LinDetector = LinDetector(issueBuilder, listOf(LinRule.File().block()))

data class LinDetector(val issueBuilder: IssueBuilder, val roots: List<LinRule<UElement>>) {
    val applicableTypes: List<Class<out UElement>> = listOf(UFile::class.java)
}

data class RuleSet(val rules: List<LinRule<*>>) {
    companion object {
        fun anyOf(vararg rules: LinRule<*>) = RuleSet(rules.toList())
    }
}

fun issue(
    scope: EnumSet<Scope>,
    description: String,
    explanation: String,
    category: Category
): IssueBuilder = IssueBuilder(scope, description, explanation, category)

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

sealed class Quantifier {
    object All : Quantifier()
    object Any : Quantifier()
    data class Times(val times: Int) : Quantifier()
    data class AtMost(val times: Int) : Quantifier()
    data class AtLeast(val times: Int) : Quantifier()

    companion object {
        val all = All
        val any = Any
        fun times(times: Int) = Times(times)
        fun atMost(times: Int) = AtMost(times)
        fun atLeast(times: Int) = AtLeast(times)
        fun lessThan(times: Int) = AtMost(times - 1)
        fun moreThan(times: Int) = AtLeast(times + 1)
        val none = times(0)
    }
}

fun file(quantifier: Quantifier = Quantifier.Any, block: LinRule.File.() -> LinRule<UFile>) =
    LinRule.File().block().also { it.quantifier = quantifier }

sealed class LinRule<out T : UElement>(val elementType: KClass<out T>) {

    var children = mutableListOf<LinRule<*>>()
    var reportingPredicate: (UElement) -> Boolean = { true }
    var quantifier: Quantifier = Quantifier.Any

    fun suchThat(predicate: (T) -> Boolean): LinRule<T> {
        reportingPredicate = { predicate(it as T) }
        return this
    }

    fun import(
        quantifier: Quantifier = Quantifier.Any,
        block: Import.() -> LinRule<UImportStatement>
    ): LinRule<T> {
        children.add(Import().block().also { it.quantifier = quantifier })
        return this
    }

    fun declaration(
        quantifier: Quantifier = Quantifier.Any,
        block: Declaration.() -> LinRule<UDeclaration>
    ): LinRule<T> {
        children.add(Declaration().block().also { it.quantifier = quantifier })
        return this
    }

    fun type(quantifier: Quantifier = Quantifier.Any, block: LinRule.Type.() -> LinRule<UClass>): LinRule<T> {
        children.add(LinRule.Type().block().also { it.quantifier = quantifier })
        return this
    }

    fun initializer(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.Initializer.() -> LinRule<UClassInitializer>
    ): LinRule<T> {
        children.add(LinRule.Initializer().block().also { it.quantifier = quantifier })
        return this
    }

    fun method(quantifier: Quantifier = Quantifier.Any, block: LinRule.Method.() -> LinRule<UMethod>): LinRule<T> {
        children.add(LinRule.Method().block().also { it.quantifier = quantifier })
        return this
    }

    fun variable(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.Variable.() -> LinRule<UVariable>
    ): LinRule<T> {
        children.add(LinRule.Variable().block().also { it.quantifier = quantifier })
        return this
    }

    fun parameter(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.Parameter.() -> LinRule<UParameter>
    ): LinRule<T> {
        children.add(LinRule.Parameter().block().also { it.quantifier = quantifier })
        return this
    }

    fun field(quantifier: Quantifier = Quantifier.Any, block: LinRule.Field.() -> LinRule<UField>): LinRule<T> {
        children.add(LinRule.Field().block().also { it.quantifier = quantifier })
        return this
    }

    fun localVariable(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.LocalVariable.() -> LinRule<ULocalVariable>
    ): LinRule<T> {
        children.add(LinRule.LocalVariable().block().also { it.quantifier = quantifier })
        return this
    }

    fun enumConstant(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.EnumConstant.() -> LinRule<UEnumConstant>
    ): LinRule<T> {
        children.add(LinRule.EnumConstant().block().also { it.quantifier = quantifier })
        return this
    }

    fun annotation(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.Annotation.() -> LinRule<UAnnotation>
    ): LinRule<T> {
        children.add(LinRule.Annotation().block().also { it.quantifier = quantifier })
        return this
    }

    fun expression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.Expression.() -> LinRule<UExpression>
    ): LinRule<T> {
        children.add(LinRule.Expression().block().also { it.quantifier = quantifier })
        return this
    }

    fun labeledExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.LabeledExpression.() -> LinRule<ULabeledExpression>
    ): LinRule<T> {
        children.add(LinRule.LabeledExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun declarationsExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.DeclarationsExpression.() -> LinRule<UDeclarationsExpression>
    ): LinRule<T> {
        children.add(LinRule.DeclarationsExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun blockExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.BlockExpression.() -> LinRule<UBlockExpression>
    ): LinRule<T> {
        children.add(LinRule.BlockExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun qualifiedReferenceExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.QualifiedReferenceExpression.() -> LinRule<UQualifiedReferenceExpression>
    ): LinRule<T> {
        children.add(LinRule.QualifiedReferenceExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun simpleNameReferenceExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.SimpleNameReferenceExpression.() -> LinRule<USimpleNameReferenceExpression>
    ): LinRule<T> {
        children.add(LinRule.SimpleNameReferenceExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun typeReferenceExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.TypeReferenceExpression.() -> LinRule<UTypeReferenceExpression>
    ): LinRule<T> {
        children.add(LinRule.TypeReferenceExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun callExpression(
        quantifier: Quantifier = Quantifier.Any, block: LinRule.CallExpression.() -> LinRule<UCallExpression>
    ): LinRule<T> {
        children.add(LinRule.CallExpression().block().also { it.quantifier = quantifier }.also {
            it.quantifier = quantifier
        })
        return this
    }

    fun binaryExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.BinaryExpression.() -> LinRule<UBinaryExpression>
    ): LinRule<T> {
        children.add(LinRule.BinaryExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun polyadicExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.PolyadicExpression.() -> LinRule<UPolyadicExpression>
    ): LinRule<T> {
        children.add(LinRule.PolyadicExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun parenthesizedExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.ParenthesizedExpression.() -> LinRule<UParenthesizedExpression>
    ): LinRule<T> {
        children.add(LinRule.ParenthesizedExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun unaryExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.UnaryExpression.() -> LinRule<UUnaryExpression>
    ): LinRule<T> {
        children.add(LinRule.UnaryExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun binaryExpressionWithType(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.BinaryExpressionWithType.() -> LinRule<UBinaryExpressionWithType>
    ): LinRule<T> {
        children.add(LinRule.BinaryExpressionWithType().block().also { it.quantifier = quantifier })
        return this
    }

    fun prefixExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.PrefixExpression.() -> LinRule<UPrefixExpression>
    ): LinRule<T> {
        children.add(LinRule.PrefixExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun postfixExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.PostfixExpression.() -> LinRule<UPostfixExpression>
    ): LinRule<T> {
        children.add(LinRule.PostfixExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun expressionList(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.ExpressionList.() -> LinRule<UExpressionList>
    ): LinRule<T> {
        children.add(LinRule.ExpressionList().block().also { it.quantifier = quantifier })
        return this
    }

    fun ifExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.IfExpression.() -> LinRule<UIfExpression>
    ): LinRule<T> {
        children.add(LinRule.IfExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun switchExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.SwitchExpression.() -> LinRule<USwitchExpression>
    ): LinRule<T> {
        children.add(LinRule.SwitchExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun switchClauseExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.SwitchClauseExpression.() -> LinRule<USwitchClauseExpression>
    ): LinRule<T> {
        children.add(LinRule.SwitchClauseExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun whileExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.WhileExpression.() -> LinRule<UWhileExpression>
    ): LinRule<T> {
        children.add(LinRule.WhileExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun doWhileExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.DoWhileExpression.() -> LinRule<UDoWhileExpression>
    ): LinRule<T> {
        children.add(LinRule.DoWhileExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun forExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.ForExpression.() -> LinRule<UForExpression>
    ): LinRule<T> {
        children.add(LinRule.ForExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun forEachExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.ForEachExpression.() -> LinRule<UForEachExpression>
    ): LinRule<T> {
        children.add(LinRule.ForEachExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun tryExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.TryExpression.() -> LinRule<UTryExpression>
    ): LinRule<T> {
        children.add(LinRule.TryExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun catchClause(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.CatchClause.() -> LinRule<UCatchClause>
    ): LinRule<T> {
        children.add(LinRule.CatchClause().block().also { it.quantifier = quantifier })
        return this
    }

    fun literalExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.LiteralExpression.() -> LinRule<ULiteralExpression>
    ): LinRule<T> {
        children.add(LinRule.LiteralExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun thisExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.ThisExpression.() -> LinRule<UThisExpression>
    ): LinRule<T> {
        children.add(LinRule.ThisExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun superExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.SuperExpression.() -> LinRule<USuperExpression>
    ): LinRule<T> {
        children.add(LinRule.SuperExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun returnExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.ReturnExpression.() -> LinRule<UReturnExpression>
    ): LinRule<T> {
        children.add(LinRule.ReturnExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun breakExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.BreakExpression.() -> LinRule<UBreakExpression>
    ): LinRule<T> {
        children.add(LinRule.BreakExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun continueExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.ContinueExpression.() -> LinRule<UContinueExpression>
    ): LinRule<T> {
        children.add(LinRule.ContinueExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun throwExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.ThrowExpression.() -> LinRule<UThrowExpression>
    ): LinRule<T> {
        children.add(LinRule.ThrowExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun arrayAccessExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.ArrayAccessExpression.() -> LinRule<UArrayAccessExpression>
    ): LinRule<T> {
        children.add(LinRule.ArrayAccessExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun callableReferenceExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.CallableReferenceExpression.() -> LinRule<UCallableReferenceExpression>
    ): LinRule<T> {
        children.add(LinRule.CallableReferenceExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun classLiteralExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.ClassLiteralExpression.() -> LinRule<UClassLiteralExpression>
    ): LinRule<T> {
        children.add(LinRule.ClassLiteralExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun lambdaExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.LambdaExpression.() -> LinRule<ULambdaExpression>
    ): LinRule<T> {
        children.add(LinRule.LambdaExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun objectLiteralExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinRule.ObjectLiteralExpression.() -> LinRule<UObjectLiteralExpression>
    ): LinRule<T> {
        children.add(LinRule.ObjectLiteralExpression().block().also { it.quantifier = quantifier })
        return this
    }

    class File : LinRule<UFile>(UFile::class)
    class Import : LinRule<UImportStatement>(UImportStatement::class)
    class Declaration : LinRule<UDeclaration>(UDeclaration::class)
    class Type : LinRule<UClass>(UClass::class)
    class Initializer : LinRule<UClassInitializer>(UClassInitializer::class)
    class Method : LinRule<UMethod>(UMethod::class)
    class Variable : LinRule<UVariable>(UVariable::class)
    class Parameter : LinRule<UParameter>(UParameter::class)
    class Field : LinRule<UField>(UField::class)
    class LocalVariable : LinRule<ULocalVariable>(ULocalVariable::class)
    class EnumConstant : LinRule<ULocalVariable>(ULocalVariable::class)
    class Annotation : LinRule<UAnnotation>(UAnnotation::class)
    class Expression : LinRule<UExpression>(UExpression::class)
    class LabeledExpression : LinRule<ULabeledExpression>(ULabeledExpression::class)
    class DeclarationsExpression : LinRule<UDeclarationsExpression>(UDeclarationsExpression::class)
    class BlockExpression : LinRule<UBlockExpression>(UBlockExpression::class)
    class QualifiedReferenceExpression : LinRule<UQualifiedReferenceExpression>(UQualifiedReferenceExpression::class)
    class SimpleNameReferenceExpression :
        LinRule<USimpleNameReferenceExpression>(USimpleNameReferenceExpression::class)

    class TypeReferenceExpression : LinRule<UTypeReferenceExpression>(UTypeReferenceExpression::class)
    class CallExpression : LinRule<UCallExpression>(UCallExpression::class)
    class BinaryExpression : LinRule<UBinaryExpression>(UBinaryExpression::class)
    class BinaryExpressionWithType : LinRule<UBinaryExpressionWithType>(UBinaryExpressionWithType::class)
    class PolyadicExpression : LinRule<UPolyadicExpression>(UPolyadicExpression::class)
    class ParenthesizedExpression : LinRule<UParenthesizedExpression>(UParenthesizedExpression::class)
    class UnaryExpression : LinRule<UUnaryExpression>(UUnaryExpression::class)
    class PrefixExpression : LinRule<UPrefixExpression>(UPrefixExpression::class)
    class PostfixExpression : LinRule<UPostfixExpression>(UPostfixExpression::class)
    class ExpressionList : LinRule<UExpressionList>(UExpressionList::class)
    class IfExpression : LinRule<UIfExpression>(UIfExpression::class)
    class SwitchExpression : LinRule<USwitchExpression>(USwitchExpression::class)
    class SwitchClauseExpression : LinRule<USwitchClauseExpression>(USwitchClauseExpression::class)
    class WhileExpression : LinRule<UWhileExpression>(UWhileExpression::class)
    class DoWhileExpression : LinRule<UDoWhileExpression>(UDoWhileExpression::class)
    class ForExpression : LinRule<UForExpression>(UForExpression::class)
    class ForEachExpression : LinRule<UForEachExpression>(UForEachExpression::class)
    class TryExpression : LinRule<UTryExpression>(UTryExpression::class)
    class CatchClause : LinRule<UCatchClause>(UCatchClause::class)
    class LiteralExpression : LinRule<ULiteralExpression>(ULiteralExpression::class)
    class ThisExpression : LinRule<UThisExpression>(UThisExpression::class)
    class SuperExpression : LinRule<USuperExpression>(USuperExpression::class)
    class ReturnExpression : LinRule<UReturnExpression>(UReturnExpression::class)
    class BreakExpression : LinRule<UBreakExpression>(UBreakExpression::class)
    class ContinueExpression : LinRule<UContinueExpression>(UContinueExpression::class)
    class ThrowExpression : LinRule<UThrowExpression>(UThrowExpression::class)
    class ArrayAccessExpression : LinRule<UArrayAccessExpression>(UArrayAccessExpression::class)
    class CallableReferenceExpression : LinRule<UCallableReferenceExpression>(UCallableReferenceExpression::class)
    class ClassLiteralExpression : LinRule<UClassLiteralExpression>(UClassLiteralExpression::class)
    class LambdaExpression : LinRule<ULambdaExpression>(ULambdaExpression::class)
    class ObjectLiteralExpression : LinRule<UObjectLiteralExpression>(UObjectLiteralExpression::class)
}