package com.serchinastico.lin.dsl

import org.jetbrains.uast.*

sealed class LinNode {
    object File : LinNode()
    object Import : LinNode()
    object Type : LinNode()
    object Field : LinNode()
    object Method : LinNode()
    object SwitchExpression : LinNode()
    object CallExpression : LinNode()
}

typealias LinNodePair = Pair<LinNode, LinNode>

val elementsByLinNode = mapOf(
    LinNode.File to UFile::class,
    LinNode.Import to UImportStatement::class,
    LinNode.Type to UClass::class,
    LinNode.Field to UField::class,
    LinNode.Method to UMethod::class,
    LinNode.SwitchExpression to USwitchExpression::class,
    LinNode.CallExpression to UCallExpression::class
)

typealias Mapping = (UElement) -> List<UElement>
typealias AllMappings = Map<LinNodePair, Mapping>

inline fun <reified T : UElement> mapping(crossinline map: (T) -> List<UElement>) =
    { node: UElement -> map(node as T) }

val allMappings: AllMappings = mutableMapOf(
    (LinNode.File to LinNode.Import) to mapping<UFile> { it.imports },
    (LinNode.File to LinNode.Type) to mapping<UFile> { it.classes },
    (LinNode.Type to LinNode.Type) to mapping<UClass> { it.innerClasses.toList() },
    (LinNode.Type to LinNode.Field) to mapping<UClass> { it.fields.toList() },
    (LinNode.Type to LinNode.Method) to mapping<UClass> { it.methods.toList() }
)

fun Map<LinNodePair, *>.filterNodePairs(
    predicate: LinNodePair.() -> Boolean
): List<LinNodePair> = keys.filter { it.predicate() }

fun expand(): AllMappings {
    val processedNodePairs = allMappings.toMutableMap()
    val nodesAboutToProcess = elementsByLinNode.keys.toMutableList()

    while (nodesAboutToProcess.isNotEmpty()) {
        val nextNode = nodesAboutToProcess.removeAt(0)

        // We get pairs of pairs: ((P1, P2), (P2, P3))
        val nextNodeWithAllGrandchildren = processedNodePairs
            // Node pairs where (P1, P2) == (nextNode, P2)
            .filterNodePairs { first == nextNode }
            // Node pairs where (nextNode, P2) == (P2, P3)
            .flatMap { pair -> processedNodePairs.filterNodePairs { first == pair.second }.map { pair to it } }

        nextNodeWithAllGrandchildren.forEach { pair ->
            // The transitive node of ((P1, P2), (P2, P3)) is (P1, P3)
            val transitiveNodePair = pair.first.first to pair.second.second
            // The transitive node has already been processed so we skip it
            if (processedNodePairs.contains(transitiveNodePair)) {
                return@forEach
            }

            processedNodePairs[transitiveNodePair] = { node ->
                val firstMapping = processedNodePairs[pair.first] ?: { emptyList() }
                val secondMapping = processedNodePairs[pair.second] ?: { emptyList() }

                firstMapping.invoke(node).flatMap { secondMapping.invoke(it) }
            }
        }
    }

    return processedNodePairs
}

fun main(args: Array<String>) {
    // 1. Create a map of all possible relationships by expanding each one and seeing if there are new ones.
    // 2. Create types with all their DSL functions
    // 3. [IGNORE] Inside type A looking for type B (all possible ones) the method has to traverse all possible edges from A to B
    //    [IGNORE] and stop once there are no changes and every single edge has been tested.
    //    [IGNORE]    a. First select all potential edges that will generate ending class (all nodes pointing to B, directly and indirectly).
    //    [IGNORE]     b. Flat-map them until after traversing all selected edges there are no changes in the final list of elements.
    // 4. NO NEED because with step 1 we already have them all, be careful with A -> A BTW
    println("STARTING: $allMappings")
    println("EXPANDED: ${expand()}")
}