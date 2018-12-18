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

typealias Counters = Map<Quantifier, Int>

fun Counters.getCount(quantifier: Quantifier): Int = this.getOrDefault(quantifier, 0)
fun Counters.plusOne(quantifier: Quantifier): Counters = plus(quantifier to getCount(quantifier) + 1)

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

fun matches(codeNodes: List<TreeNode>, ruleNodes: List<LinNode<UElement>>): Boolean =
    try {
        matches(codeNodes, ruleNodes, emptyMap())
    } catch (e: MatchError) {
        false
    }

/* The function throws an error if we decided the rules won't ever apply to the code.
 * We could do it with a different type for it and dealing with it every time we'd find it but I'm finding it easier
 * to read if we consider that a different thing.
 * I'm doing it because it's a private function so that we don't ever throw that exception out of this code and because
 * thanks to breaking the whole call stack we don't have to deal with it every single time we find it, just on the
 * caller's side
 */
private fun matches(
    codeNodes: List<TreeNode>,
    ruleNodes: List<LinNode<UElement>>,
    quantifierCounters: Map<Quantifier, Int>
): Boolean {
    // We finished processing rules and found a match, we succeeded as long as quantifiers match their requirements
    if (ruleNodes.isEmpty()) {
        return quantifierCounters.allQualifiersMeetRequirements
    }

    // We don't have more code and there are still rules that are not matching, we failed
    if (codeNodes.isEmpty()) {
        return false
    }

    val headCodeNode = codeNodes.first()
    val tailCodeNodes = codeNodes.drop(1)

    return ruleNodes
        .filter { it.elementType.isSuperclassOf(headCodeNode.element::class) }
        .filter { it.matches(headCodeNode.element, quantifierCounters) }
        .any { ruleNode ->
            when (ruleNode.quantifier) {
                Quantifier.All -> matches(headCodeNode.children, ruleNode.children, quantifierCounters) &&
                        matches(tailCodeNodes, ruleNodes, quantifierCounters)
                Quantifier.Any -> matches(headCodeNode.children, ruleNode.children, quantifierCounters) &&
                        matches(tailCodeNodes, ruleNodes.minus(ruleNode), quantifierCounters)
                is Quantifier.Times, is Quantifier.MoreThan, is Quantifier.LessThan ->
                    matches(headCodeNode.children, ruleNode.children, quantifierCounters) &&
                            matches(tailCodeNodes, ruleNodes, quantifierCounters.plusOne(ruleNode.quantifier))
            }
        } || ((ruleNodes.none { it.quantifier == Quantifier.All } &&
            matches(tailCodeNodes, ruleNodes, quantifierCounters)))
}

private val Counters.allQualifiersMeetRequirements: Boolean
    get() = all { entry ->
        val quantifier = entry.key
        when (quantifier) {
            Quantifier.All, Quantifier.Any -> true
            is Quantifier.Times -> quantifier.times == entry.value
            is Quantifier.MoreThan -> quantifier.times > entry.value
            is Quantifier.LessThan -> quantifier.times < entry.value
        }
    }

private fun LinNode<UElement>.matches(
    element: UElement,
    quantifierCounters: Map<Quantifier, Int>
): Boolean = quantifier.let { quantifier ->
    when (quantifier) {
        Quantifier.All -> ruleMatchesAll(this, element)
        Quantifier.Any -> ruleMatchesAny(this, element)
        is Quantifier.Times -> ruleMatchTimes(this, element, quantifierCounters, quantifier)
        is Quantifier.MoreThan -> ruleMatchMoreThan(this, element)
        is Quantifier.LessThan -> ruleMatchLessThan(this, element, quantifierCounters, quantifier)
    }
}

private fun ruleMatchesAll(
    rule: LinNode<UElement>,
    element: UElement
): Boolean = if (rule.reportingPredicate(element)) {
    true
} else {
    throw MatchError()
}

private fun ruleMatchesAny(
    rule: LinNode<UElement>,
    element: UElement
): Boolean = rule.reportingPredicate(element)

private fun ruleMatchTimes(
    rule: LinNode<UElement>,
    element: UElement,
    quantifierCounters: Map<Quantifier, Int>,
    quantifier: Quantifier.Times
): Boolean {
    if (!rule.reportingPredicate(element)) {
        return false
    }

    return if (quantifierCounters.getCount(quantifier) >= quantifier.times) {
        throw MatchError()
    } else {
        true
    }
}

private fun ruleMatchMoreThan(
    rule: LinNode<UElement>,
    element: UElement
): Boolean = rule.reportingPredicate(element)

private fun ruleMatchLessThan(
    rule: LinNode<UElement>,
    element: UElement,
    quantifierCounters: Map<Quantifier, Int>,
    quantifier: Quantifier.LessThan
): Boolean {
    if (!rule.reportingPredicate(element)) {
        return false
    }

    return if (quantifierCounters.getCount(quantifier) >= quantifier.times - 1) {
        throw MatchError()
    } else {
        true
    }
}

private class MatchError : Error("Impossible to find a match")