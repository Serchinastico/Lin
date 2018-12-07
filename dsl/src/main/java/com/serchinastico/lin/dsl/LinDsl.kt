package com.serchinastico.lin.dsl

import org.jetbrains.uast.UClass
import org.jetbrains.uast.UFile
import org.jetbrains.uast.UImportStatement

class LinFile : SuchThat<UFile> by SuchThatStored() {

    private val imports: MutableList<LinImport> = mutableListOf()
    private val types: MutableList<LinType> = mutableListOf()

    fun import(block: LinImport.() -> LinImport): LinFile {
        imports.add(LinImport().block())
        return this
    }

    fun type(block: LinType.() -> LinType): LinFile {
        types.add(LinType().block())
        return this
    }
}

fun file(block: LinFile.() -> LinFile): LinFile {
    return LinFile().block()
}

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
    file {
        import { suchThat { it.isFrameworkLibraryImport } }
        type { suchThat { node -> node.uastSuperTypes.any { it.isAndroidFrameworkType } } }
    }
}