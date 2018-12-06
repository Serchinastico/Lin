package com.serchinastico.lin.dsl

import com.autodsl.annotation.AutoDsl

@AutoDsl("file")
data class LinFile(
    val imports: List<LinImport>,
    val types: List<LinType>
) : SuchThat

@AutoDsl("type")
data class LinType(
    override val suchThat: () -> Boolean
) : SuchThat

@AutoDsl("import")
data class LinImport(
    override val suchThat: () -> Boolean
) : SuchThat

@AutoDsl
data class Something(val id: String)

interface SuchThat {
    val suchThat: (() -> Boolean)?
        get() = null
}

fun main(args: Array<String>) {
    file {
        import { suchThat = { true } }
        type { suchThat = { true } }
    }
}