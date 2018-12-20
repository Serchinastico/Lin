package com.serchinastico.lin.dsl

import org.jetbrains.uast.UElement
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf


data class TreeNode(
    val children: MutableList<TreeNode>,
    var parent: TreeNode?,
    val element: UElement
) {
    override fun toString(): String {
        return "TreeNode(children=$children, element=$element)"
    }
}

fun LinNode<UElement>.matches(code: TreeNode): Boolean = matches(listOf(code), listOf(this))

fun LinNode<UElement>.allUElementSuperClasses(): List<KClass<out UElement>> {
    val superClasses = mutableSetOf<KClass<out UElement>>()
    val nodesToProcess = mutableListOf(this)

    while (nodesToProcess.isNotEmpty()) {
        val currentNode = nodesToProcess.removeAt(0)
        superClasses.add(currentNode.elementType)
        nodesToProcess.addAll(currentNode.children)
    }

    return superClasses.toList()
}

fun matches(codeNodes: List<TreeNode>, ruleNodes: List<LinNode<UElement>>): Boolean {
    // We finished processing rules and found a match, we succeeded
    if (ruleNodes.isEmpty()) {
        return true
    }

    // We don't have more code and there are still rules that are not matching, we failed
    if (codeNodes.isEmpty()) {
        return false
    }

    val headCodeNode = codeNodes.first()
    val tailCodeNodes = codeNodes.drop(1)

    fun ruleMatches(rule: LinNode<UElement>, element: UElement): Boolean = rule.reportingPredicate(element)

    return ruleNodes
        .filter { it.elementType.isSuperclassOf(headCodeNode.element::class) }
        .any { ruleNode ->
            ruleMatches(ruleNode, headCodeNode.element) &&
                    // All rule children matches with children of the node
                    matches(headCodeNode.children, ruleNode.children) &&
                    // All the other rules matches
                    matches(tailCodeNodes, ruleNodes.minus(ruleNode))
        } || matches(tailCodeNodes, ruleNodes) // OR we just ignore this line of code and continue matching rules
}