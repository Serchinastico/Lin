package com.serchinastico.rules

import com.intellij.psi.PsiType
import org.jetbrains.uast.*

val PsiType.allIsATypes: List<PsiType>
    get() = superTypes.asList() + this

fun PsiType.isAnyOf(vararg fullyQualifiedNames: String): Boolean =
    allIsATypes.any { type -> fullyQualifiedNames.asList().any { type.canonicalText == it } }

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