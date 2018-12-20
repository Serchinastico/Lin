package com.serchinastico.lin.dsl

import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import java.util.*
import kotlin.reflect.KClass

data class LinRule(val issueBuilder: IssueBuilder, val root: LinNode<UElement>) {
    val applicableTypes: List<Class<out UElement>> = listOf(UFile::class.java)
}

fun rule(
    issueBuilder: IssueBuilder,
    quantifier: Quantifier = Quantifier.Any,
    block: LinNode.File.() -> LinNode<*>
): LinRule = LinRule(issueBuilder, LinNode.File().block().also { it.quantifier = quantifier })

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
}

sealed class LinNode<out T : UElement>(val elementType: KClass<out T>) {

    var children = mutableListOf<LinNode<*>>()
    var reportingPredicate: (UElement) -> Boolean = { true }
    var quantifier: Quantifier = Quantifier.Any

    fun suchThat(predicate: (T) -> Boolean): LinNode<T> {
        reportingPredicate = { predicate(it as T) }
        return this
    }

    fun import(
        quantifier: Quantifier = Quantifier.Any,
        block: Import.() -> LinNode<UImportStatement>
    ): LinNode<T> {
        children.add(Import().block().also { it.quantifier = quantifier })
        return this
    }

    fun declaration(
        quantifier: Quantifier = Quantifier.Any,
        block: Declaration.() -> LinNode<UDeclaration>
    ): LinNode<T> {
        children.add(Declaration().block().also { it.quantifier = quantifier })
        return this
    }

    fun type(quantifier: Quantifier = Quantifier.Any, block: LinNode.Type.() -> LinNode<UClass>): LinNode<T> {
        children.add(LinNode.Type().block().also { it.quantifier = quantifier })
        return this
    }

    fun initializer(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.Initializer.() -> LinNode<UClassInitializer>
    ): LinNode<T> {
        children.add(LinNode.Initializer().block().also { it.quantifier = quantifier })
        return this
    }

    fun method(quantifier: Quantifier = Quantifier.Any, block: LinNode.Method.() -> LinNode<UMethod>): LinNode<T> {
        children.add(LinNode.Method().block().also { it.quantifier = quantifier })
        return this
    }

    fun variable(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.Variable.() -> LinNode<UVariable>
    ): LinNode<T> {
        children.add(LinNode.Variable().block().also { it.quantifier = quantifier })
        return this
    }

    fun parameter(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.Parameter.() -> LinNode<UParameter>
    ): LinNode<T> {
        children.add(LinNode.Parameter().block().also { it.quantifier = quantifier })
        return this
    }

    fun field(quantifier: Quantifier = Quantifier.Any, block: LinNode.Field.() -> LinNode<UField>): LinNode<T> {
        children.add(LinNode.Field().block().also { it.quantifier = quantifier })
        return this
    }

    fun localVariable(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.LocalVariable.() -> LinNode<ULocalVariable>
    ): LinNode<T> {
        children.add(LinNode.LocalVariable().block().also { it.quantifier = quantifier })
        return this
    }

    fun enumConstant(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.EnumConstant.() -> LinNode<UEnumConstant>
    ): LinNode<T> {
        children.add(LinNode.EnumConstant().block().also { it.quantifier = quantifier })
        return this
    }

    fun annotation(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.Annotation.() -> LinNode<UAnnotation>
    ): LinNode<T> {
        children.add(LinNode.Annotation().block().also { it.quantifier = quantifier })
        return this
    }

    fun expression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.Expression.() -> LinNode<UExpression>
    ): LinNode<T> {
        children.add(LinNode.Expression().block().also { it.quantifier = quantifier })
        return this
    }

    fun labeledExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.LabeledExpression.() -> LinNode<ULabeledExpression>
    ): LinNode<T> {
        children.add(LinNode.LabeledExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun declarationsExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.DeclarationsExpression.() -> LinNode<UDeclarationsExpression>
    ): LinNode<T> {
        children.add(LinNode.DeclarationsExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun blockExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.BlockExpression.() -> LinNode<UBlockExpression>
    ): LinNode<T> {
        children.add(LinNode.BlockExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun qualifiedReferenceExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.QualifiedReferenceExpression.() -> LinNode<UQualifiedReferenceExpression>
    ): LinNode<T> {
        children.add(LinNode.QualifiedReferenceExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun simpleNameReferenceExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.SimpleNameReferenceExpression.() -> LinNode<USimpleNameReferenceExpression>
    ): LinNode<T> {
        children.add(LinNode.SimpleNameReferenceExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun typeReferenceExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.TypeReferenceExpression.() -> LinNode<UTypeReferenceExpression>
    ): LinNode<T> {
        children.add(LinNode.TypeReferenceExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun callExpression(
        quantifier: Quantifier = Quantifier.Any, block: LinNode.CallExpression.() -> LinNode<UCallExpression>
    ): LinNode<T> {
        children.add(LinNode.CallExpression().block().also { it.quantifier = quantifier }.also {
            it.quantifier = quantifier
        })
        return this
    }

    fun binaryExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.BinaryExpression.() -> LinNode<UBinaryExpression>
    ): LinNode<T> {
        children.add(LinNode.BinaryExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun polyadicExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.PolyadicExpression.() -> LinNode<UPolyadicExpression>
    ): LinNode<T> {
        children.add(LinNode.PolyadicExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun parenthesizedExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.ParenthesizedExpression.() -> LinNode<UParenthesizedExpression>
    ): LinNode<T> {
        children.add(LinNode.ParenthesizedExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun unaryExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.UnaryExpression.() -> LinNode<UUnaryExpression>
    ): LinNode<T> {
        children.add(LinNode.UnaryExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun binaryExpressionWithType(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.BinaryExpressionWithType.() -> LinNode<UBinaryExpressionWithType>
    ): LinNode<T> {
        children.add(LinNode.BinaryExpressionWithType().block().also { it.quantifier = quantifier })
        return this
    }

    fun prefixExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.PrefixExpression.() -> LinNode<UPrefixExpression>
    ): LinNode<T> {
        children.add(LinNode.PrefixExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun postfixExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.PostfixExpression.() -> LinNode<UPostfixExpression>
    ): LinNode<T> {
        children.add(LinNode.PostfixExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun expressionList(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.ExpressionList.() -> LinNode<UExpressionList>
    ): LinNode<T> {
        children.add(LinNode.ExpressionList().block().also { it.quantifier = quantifier })
        return this
    }

    fun ifExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.IfExpression.() -> LinNode<UIfExpression>
    ): LinNode<T> {
        children.add(LinNode.IfExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun switchExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.SwitchExpression.() -> LinNode<USwitchExpression>
    ): LinNode<T> {
        children.add(LinNode.SwitchExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun switchClauseExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.SwitchClauseExpression.() -> LinNode<USwitchClauseExpression>
    ): LinNode<T> {
        children.add(LinNode.SwitchClauseExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun whileExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.WhileExpression.() -> LinNode<UWhileExpression>
    ): LinNode<T> {
        children.add(LinNode.WhileExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun doWhileExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.DoWhileExpression.() -> LinNode<UDoWhileExpression>
    ): LinNode<T> {
        children.add(LinNode.DoWhileExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun forExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.ForExpression.() -> LinNode<UForExpression>
    ): LinNode<T> {
        children.add(LinNode.ForExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun forEachExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.ForEachExpression.() -> LinNode<UForEachExpression>
    ): LinNode<T> {
        children.add(LinNode.ForEachExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun tryExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.TryExpression.() -> LinNode<UTryExpression>
    ): LinNode<T> {
        children.add(LinNode.TryExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun catchClause(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.CatchClause.() -> LinNode<UCatchClause>
    ): LinNode<T> {
        children.add(LinNode.CatchClause().block().also { it.quantifier = quantifier })
        return this
    }

    fun literalExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.LiteralExpression.() -> LinNode<ULiteralExpression>
    ): LinNode<T> {
        children.add(LinNode.LiteralExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun thisExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.ThisExpression.() -> LinNode<UThisExpression>
    ): LinNode<T> {
        children.add(LinNode.ThisExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun superExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.SuperExpression.() -> LinNode<USuperExpression>
    ): LinNode<T> {
        children.add(LinNode.SuperExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun returnExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.ReturnExpression.() -> LinNode<UReturnExpression>
    ): LinNode<T> {
        children.add(LinNode.ReturnExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun breakExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.BreakExpression.() -> LinNode<UBreakExpression>
    ): LinNode<T> {
        children.add(LinNode.BreakExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun continueExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.ContinueExpression.() -> LinNode<UContinueExpression>
    ): LinNode<T> {
        children.add(LinNode.ContinueExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun throwExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.ThrowExpression.() -> LinNode<UThrowExpression>
    ): LinNode<T> {
        children.add(LinNode.ThrowExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun arrayAccessExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.ArrayAccessExpression.() -> LinNode<UArrayAccessExpression>
    ): LinNode<T> {
        children.add(LinNode.ArrayAccessExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun callableReferenceExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.CallableReferenceExpression.() -> LinNode<UCallableReferenceExpression>
    ): LinNode<T> {
        children.add(LinNode.CallableReferenceExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun classLiteralExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.ClassLiteralExpression.() -> LinNode<UClassLiteralExpression>
    ): LinNode<T> {
        children.add(LinNode.ClassLiteralExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun lambdaExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.LambdaExpression.() -> LinNode<ULambdaExpression>
    ): LinNode<T> {
        children.add(LinNode.LambdaExpression().block().also { it.quantifier = quantifier })
        return this
    }

    fun objectLiteralExpression(
        quantifier: Quantifier = Quantifier.Any,
        block: LinNode.ObjectLiteralExpression.() -> LinNode<UObjectLiteralExpression>
    ): LinNode<T> {
        children.add(LinNode.ObjectLiteralExpression().block().also { it.quantifier = quantifier })
        return this
    }

    class File : LinNode<UFile>(UFile::class)
    class Import : LinNode<UImportStatement>(UImportStatement::class)
    class Declaration : LinNode<UDeclaration>(UDeclaration::class)
    class Type : LinNode<UClass>(UClass::class)
    class Initializer : LinNode<UClassInitializer>(UClassInitializer::class)
    class Method : LinNode<UMethod>(UMethod::class)
    class Variable : LinNode<UVariable>(UVariable::class)
    class Parameter : LinNode<UParameter>(UParameter::class)
    class Field : LinNode<UField>(UField::class)
    class LocalVariable : LinNode<ULocalVariable>(ULocalVariable::class)
    class EnumConstant : LinNode<ULocalVariable>(ULocalVariable::class)
    class Annotation : LinNode<UAnnotation>(UAnnotation::class)
    class Expression : LinNode<UExpression>(UExpression::class)
    class LabeledExpression : LinNode<ULabeledExpression>(ULabeledExpression::class)
    class DeclarationsExpression : LinNode<UDeclarationsExpression>(UDeclarationsExpression::class)
    class BlockExpression : LinNode<UBlockExpression>(UBlockExpression::class)
    class QualifiedReferenceExpression : LinNode<UQualifiedReferenceExpression>(UQualifiedReferenceExpression::class)
    class SimpleNameReferenceExpression :
        LinNode<USimpleNameReferenceExpression>(USimpleNameReferenceExpression::class)

    class TypeReferenceExpression : LinNode<UTypeReferenceExpression>(UTypeReferenceExpression::class)
    class CallExpression : LinNode<UCallExpression>(UCallExpression::class)
    class BinaryExpression : LinNode<UBinaryExpression>(UBinaryExpression::class)
    class BinaryExpressionWithType : LinNode<UBinaryExpressionWithType>(UBinaryExpressionWithType::class)
    class PolyadicExpression : LinNode<UPolyadicExpression>(UPolyadicExpression::class)
    class ParenthesizedExpression : LinNode<UParenthesizedExpression>(UParenthesizedExpression::class)
    class UnaryExpression : LinNode<UUnaryExpression>(UUnaryExpression::class)
    class PrefixExpression : LinNode<UPrefixExpression>(UPrefixExpression::class)
    class PostfixExpression : LinNode<UPostfixExpression>(UPostfixExpression::class)
    class ExpressionList : LinNode<UExpressionList>(UExpressionList::class)
    class IfExpression : LinNode<UIfExpression>(UIfExpression::class)
    class SwitchExpression : LinNode<USwitchExpression>(USwitchExpression::class)
    class SwitchClauseExpression : LinNode<USwitchClauseExpression>(USwitchClauseExpression::class)
    class WhileExpression : LinNode<UWhileExpression>(UWhileExpression::class)
    class DoWhileExpression : LinNode<UDoWhileExpression>(UDoWhileExpression::class)
    class ForExpression : LinNode<UForExpression>(UForExpression::class)
    class ForEachExpression : LinNode<UForEachExpression>(UForEachExpression::class)
    class TryExpression : LinNode<UTryExpression>(UTryExpression::class)
    class CatchClause : LinNode<UCatchClause>(UCatchClause::class)
    class LiteralExpression : LinNode<ULiteralExpression>(ULiteralExpression::class)
    class ThisExpression : LinNode<UThisExpression>(UThisExpression::class)
    class SuperExpression : LinNode<USuperExpression>(USuperExpression::class)
    class ReturnExpression : LinNode<UReturnExpression>(UReturnExpression::class)
    class BreakExpression : LinNode<UBreakExpression>(UBreakExpression::class)
    class ContinueExpression : LinNode<UContinueExpression>(UContinueExpression::class)
    class ThrowExpression : LinNode<UThrowExpression>(UThrowExpression::class)
    class ArrayAccessExpression : LinNode<UArrayAccessExpression>(UArrayAccessExpression::class)
    class CallableReferenceExpression : LinNode<UCallableReferenceExpression>(UCallableReferenceExpression::class)
    class ClassLiteralExpression : LinNode<UClassLiteralExpression>(UClassLiteralExpression::class)
    class LambdaExpression : LinNode<ULambdaExpression>(ULambdaExpression::class)
    class ObjectLiteralExpression : LinNode<UObjectLiteralExpression>(UObjectLiteralExpression::class)
}

data class Rule(val issueBuilder: IssueBuilder, val node: LinNode<*>)

fun rule(issueBuilder: IssueBuilder, node: LinNode<*>) = Rule(issueBuilder, node)

fun file(quantifier: Quantifier = Quantifier.Any, block: LinNode.File.() -> LinNode<UFile>) =
    LinNode.File().block().also { it.quantifier = quantifier }
