package com.serchinastico.lin.dsl

import com.android.ide.common.blame.SourcePosition
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.lang.Language
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.impl.source.PsiImmediateClassType
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.uast.*
import org.jetbrains.uast.java.JavaUDefaultCaseExpression
import org.jetbrains.uast.kotlin.KotlinUClass

val Any?.exhaustive get() = Unit

fun Context.report(issue: Issue, position: Location, source: UElement) {
    report(
        issue,
        Location.create(file, position.start!!, position.end).withSource(source),
        issue.getBriefDescription(TextFormat.TEXT)
    )
}

val USwitchExpression.clauses: List<USwitchClauseExpression>
    get() = body.expressions.mapNotNull { it as? USwitchClauseExpression }

val USwitchClauseExpression.isElseBranch: Boolean
    get() = caseValues.isEmpty() || caseValues.any { it is JavaUDefaultCaseExpression }

val PsiType.isSealed: Boolean
    get() = when (this) {
        is PsiClassReferenceType -> {
            val resolvedType = resolve()
            when (resolvedType) {
                is KtLightClass -> (resolvedType.kotlinOrigin as? KtClass)?.isSealed() ?: false
                else -> false
            }
        }
        else -> false
    }

val PsiType.isEnum: Boolean
    get() = when (this) {
        is PsiImmediateClassType -> resolve()?.isEnum ?: false
        is PsiClassReferenceType -> resolve()?.isEnum ?: false
        else -> false
    }

val UField.isPrivate: Boolean
    get() {
        when {
            language.isJava -> return visibility == UastVisibility.PRIVATE
            language.isKotlin -> {
                val parent = (uastParent ?: return false) as? KotlinUClass ?: return false

                val propertyAccessorName = "get${name.capitalize()}"
                val propertyAccessor = parent.methods
                    .filter { it.name == propertyAccessorName }
                    .filter { it.parameters.isEmpty() }
                    .firstOrNull { it.returnType == type }

                val accessor: UDeclaration = propertyAccessor ?: this
                return accessor.visibility == UastVisibility.PRIVATE
            }
            else -> return false
        }
    }

val UField.isOverride: Boolean
    get() = when {
        language.isJava -> annotations.any { it.qualifiedName == Override::class.qualifiedName }
        // The only way to see if a property is overriding in kotlin is to see if it has the same
        // signature than any ancestor class property. This is a quick correct-in-most-cases fix
        language.isKotlin -> text.contains("override ")
        else -> false
    }

val Language.isKotlin: Boolean
    get() = this == Language.findLanguageByID("kotlin")
val Language.isJava: Boolean
    get() = this == Language.findLanguageByID("JAVA")

fun UVariable.isClassOrSubclassOf(vararg fullyQualifiedNames: String): Boolean =
    allIsATypes.any { type -> fullyQualifiedNames.asList().any { type.canonicalText == it } }

fun PsiType.isClassOrSubclassOf(vararg fullyQualifiedNames: String): Boolean =
    allIsATypes.any { type -> fullyQualifiedNames.asList().any { type.canonicalText == it } }

val UVariable.allIsATypes: List<PsiType>
    get() = typeReference?.type?.allIsATypes ?: type.allIsATypes

val PsiType.allIsATypes: List<PsiType>
    get() = superTypes.asList() + this

val UTypeReferenceExpression.isAndroidFrameworkType: Boolean
    get() = getQualifiedName()?.let { name ->
        listOf(
            "android.app.",
            "android.support.v4.app"
        ).any { name.startsWith(it) }
    } ?: false

val UImportStatement.isFrameworkLibraryImport: Boolean
    get() {
        val importedPackageName = importReference?.asRenderString() ?: return false

        return listOf(
            "com.squareup.retrofit",
            "com.squareup.retrofit2",
            "com.squareup.okhttp",
            "com.android.volley",
            "com.mcxiaoke.volley",
            "androidx.room",
            "android.arch.persistence.room",
            "android.content.SharedPreferences",
            "android.database",
            "java.net"
        ).any { importedPackageName.startsWith(it) }
    }

val UElement.classesInSameFile: List<UClass>
    get() = getContainingUFile()?.classes ?: emptyList()