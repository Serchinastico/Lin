package com.serchinastico.lin.dsl

import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import java.util.*
import kotlin.reflect.KClass

data class LinRule(val issueBuilder: IssueBuilder, val root: LinNode<UElement>) {
    val applicableTypes: List<Class<out UElement>> = listOf(UFile::class.java)
}

fun rule(issueBuilder: IssueBuilder, block: LinNode.File.() -> LinNode<*>): LinRule =
    LinRule(issueBuilder, LinNode.File().block())

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

sealed class LinNode<out T : UElement>(val elementType: KClass<out T>) {

    var children = mutableListOf<LinNode<*>>()
    var reportingPredicate: (UElement) -> Boolean = { true }

    fun suchThat(predicate: (T) -> Boolean): LinNode<T> {
        reportingPredicate = { predicate(it as T) }
        return this
    }

    fun import(block: Import.() -> LinNode<UImportStatement>): LinNode<T> {
        children.add(Import().block())
        return this
    }

    fun declaration(block: Declaration.() -> LinNode<UDeclaration>): LinNode<T> {
        children.add(Declaration().block())
        return this
    }

    fun type(block: LinNode.Type.() -> LinNode<UClass>): LinNode<T> {
        children.add(LinNode.Type().block())
        return this
    }

    fun initializer(block: LinNode.Initializer.() -> LinNode<UClassInitializer>): LinNode<T> {
        children.add(LinNode.Initializer().block())
        return this
    }

    fun method(block: LinNode.Method.() -> LinNode<UMethod>): LinNode<T> {
        children.add(LinNode.Method().block())
        return this
    }

    fun variable(block: LinNode.Variable.() -> LinNode<UVariable>): LinNode<T> {
        children.add(LinNode.Variable().block())
        return this
    }

    fun parameter(block: LinNode.Parameter.() -> LinNode<UParameter>): LinNode<T> {
        children.add(LinNode.Parameter().block())
        return this
    }

    fun field(block: LinNode.Field.() -> LinNode<UField>): LinNode<T> {
        children.add(LinNode.Field().block())
        return this
    }

    fun localVariable(block: LinNode.LocalVariable.() -> LinNode<ULocalVariable>): LinNode<T> {
        children.add(LinNode.LocalVariable().block())
        return this
    }

    fun enumConstant(block: LinNode.EnumConstant.() -> LinNode<UEnumConstant>): LinNode<T> {
        children.add(LinNode.EnumConstant().block())
        return this
    }

    fun annotation(block: LinNode.Annotation.() -> LinNode<UAnnotation>): LinNode<T> {
        children.add(LinNode.Annotation().block())
        return this
    }

    fun expression(block: LinNode.Expression.() -> LinNode<UExpression>): LinNode<T> {
        children.add(LinNode.Expression().block())
        return this
    }

    fun labeledExpression(block: LinNode.LabeledExpression.() -> LinNode<ULabeledExpression>): LinNode<T> {
        children.add(LinNode.LabeledExpression().block())
        return this
    }

    fun declarationsExpression(block: LinNode.DeclarationsExpression.() -> LinNode<UDeclarationsExpression>): LinNode<T> {
        children.add(LinNode.DeclarationsExpression().block())
        return this
    }

    fun blockExpression(block: LinNode.BlockExpression.() -> LinNode<UBlockExpression>): LinNode<T> {
        children.add(LinNode.BlockExpression().block())
        return this
    }

    fun qualifiedReferenceExpression(
        block: LinNode.QualifiedReferenceExpression.() -> LinNode<UQualifiedReferenceExpression>
    ): LinNode<T> {
        children.add(LinNode.QualifiedReferenceExpression().block())
        return this
    }

    fun simpleNameReferenceExpression(
        block: LinNode.SimpleNameReferenceExpression.() -> LinNode<USimpleNameReferenceExpression>
    ): LinNode<T> {
        children.add(LinNode.SimpleNameReferenceExpression().block())
        return this
    }

    fun typeReferenceExpression(block: LinNode.TypeReferenceExpression.() -> LinNode<UTypeReferenceExpression>): LinNode<T> {
        children.add(LinNode.TypeReferenceExpression().block())
        return this
    }

    fun callExpression(block: LinNode.CallExpression.() -> LinNode<UCallExpression>): LinNode<T> {
        children.add(LinNode.CallExpression().block())
        return this
    }

    fun binaryExpression(block: LinNode.BinaryExpression.() -> LinNode<UBinaryExpression>): LinNode<T> {
        children.add(LinNode.BinaryExpression().block())
        return this
    }

    fun polyadicExpression(block: LinNode.PolyadicExpression.() -> LinNode<UPolyadicExpression>): LinNode<T> {
        children.add(LinNode.PolyadicExpression().block())
        return this
    }

    fun parenthesizedExpression(
        block: LinNode.ParenthesizedExpression.() -> LinNode<UParenthesizedExpression>
    ): LinNode<T> {
        children.add(LinNode.ParenthesizedExpression().block())
        return this
    }

    fun unaryExpression(block: LinNode.UnaryExpression.() -> LinNode<UUnaryExpression>): LinNode<T> {
        children.add(LinNode.UnaryExpression().block())
        return this
    }

    fun binaryExpressionWithType(block: LinNode.BinaryExpressionWithType.() -> LinNode<UBinaryExpressionWithType>): LinNode<T> {
        children.add(LinNode.BinaryExpressionWithType().block())
        return this
    }

    fun prefixExpression(block: LinNode.PrefixExpression.() -> LinNode<UPrefixExpression>): LinNode<T> {
        children.add(LinNode.PrefixExpression().block())
        return this
    }

    fun postfixExpression(block: LinNode.PostfixExpression.() -> LinNode<UPostfixExpression>): LinNode<T> {
        children.add(LinNode.PostfixExpression().block())
        return this
    }

    fun expressionList(block: LinNode.ExpressionList.() -> LinNode<UExpressionList>): LinNode<T> {
        children.add(LinNode.ExpressionList().block())
        return this
    }

    fun ifExpression(block: LinNode.IfExpression.() -> LinNode<UIfExpression>): LinNode<T> {
        children.add(LinNode.IfExpression().block())
        return this
    }

    fun switchExpression(block: LinNode.SwitchExpression.() -> LinNode<USwitchExpression>): LinNode<T> {
        children.add(LinNode.SwitchExpression().block())
        return this
    }

    fun switchClauseExpression(block: LinNode.SwitchClauseExpression.() -> LinNode<USwitchClauseExpression>): LinNode<T> {
        children.add(LinNode.SwitchClauseExpression().block())
        return this
    }

    fun whileExpression(block: LinNode.WhileExpression.() -> LinNode<UWhileExpression>): LinNode<T> {
        children.add(LinNode.WhileExpression().block())
        return this
    }

    fun doWhileExpression(block: LinNode.DoWhileExpression.() -> LinNode<UDoWhileExpression>): LinNode<T> {
        children.add(LinNode.DoWhileExpression().block())
        return this
    }

    fun forExpression(block: LinNode.ForExpression.() -> LinNode<UForExpression>): LinNode<T> {
        children.add(LinNode.ForExpression().block())
        return this
    }

    fun forEachExpression(block: LinNode.ForEachExpression.() -> LinNode<UForEachExpression>): LinNode<T> {
        children.add(LinNode.ForEachExpression().block())
        return this
    }

    fun tryExpression(block: LinNode.TryExpression.() -> LinNode<UTryExpression>): LinNode<T> {
        children.add(LinNode.TryExpression().block())
        return this
    }

    fun catchClause(block: LinNode.CatchClause.() -> LinNode<UCatchClause>): LinNode<T> {
        children.add(LinNode.CatchClause().block())
        return this
    }

    fun literalExpression(block: LinNode.LiteralExpression.() -> LinNode<ULiteralExpression>): LinNode<T> {
        children.add(LinNode.LiteralExpression().block())
        return this
    }

    fun thisExpression(block: LinNode.ThisExpression.() -> LinNode<UThisExpression>): LinNode<T> {
        children.add(LinNode.ThisExpression().block())
        return this
    }

    fun superExpression(block: LinNode.SuperExpression.() -> LinNode<USuperExpression>): LinNode<T> {
        children.add(LinNode.SuperExpression().block())
        return this
    }

    fun returnExpression(block: LinNode.ReturnExpression.() -> LinNode<UReturnExpression>): LinNode<T> {
        children.add(LinNode.ReturnExpression().block())
        return this
    }

    fun breakExpression(block: LinNode.BreakExpression.() -> LinNode<UBreakExpression>): LinNode<T> {
        children.add(LinNode.BreakExpression().block())
        return this
    }

    fun continueExpression(block: LinNode.ContinueExpression.() -> LinNode<UContinueExpression>): LinNode<T> {
        children.add(LinNode.ContinueExpression().block())
        return this
    }

    fun throwExpression(block: LinNode.ThrowExpression.() -> LinNode<UThrowExpression>): LinNode<T> {
        children.add(LinNode.ThrowExpression().block())
        return this
    }

    fun arrayAccessExpression(block: LinNode.ArrayAccessExpression.() -> LinNode<UArrayAccessExpression>): LinNode<T> {
        children.add(LinNode.ArrayAccessExpression().block())
        return this
    }

    fun callableReferenceExpression(
        block: LinNode.CallableReferenceExpression.() -> LinNode<UCallableReferenceExpression>
    ): LinNode<T> {
        children.add(LinNode.CallableReferenceExpression().block())
        return this
    }

    fun classLiteralExpression(
        block: LinNode.ClassLiteralExpression.() -> LinNode<UClassLiteralExpression>
    ): LinNode<T> {
        children.add(LinNode.ClassLiteralExpression().block())
        return this
    }

    fun lambdaExpression(block: LinNode.LambdaExpression.() -> LinNode<ULambdaExpression>): LinNode<T> {
        children.add(LinNode.LambdaExpression().block())
        return this
    }

    fun objectLiteralExpression(block: LinNode.ObjectLiteralExpression.() -> LinNode<UObjectLiteralExpression>): LinNode<T> {
        children.add(LinNode.ObjectLiteralExpression().block())
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

fun file(block: LinNode.File.() -> LinNode<UFile>) = LinNode.File().block()
