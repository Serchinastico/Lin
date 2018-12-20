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
    matches(codeNodes, ruleNodes, emptyMap())

private fun matches(
    codeNodes: List<TreeNode>,
    ruleNodes: List<LinNode<UElement>>,
    quantifierCounters: Map<Quantifier, Int>
): Boolean {
    // We finished processing rules and found a match, we succeeded as long as quantifiers match their requirements
    if (ruleNodes.isEmpty()) {
        return quantifierCounters.allQualifiersMeetRequirements(ruleNodes)
    }

    // We don't have more code and there are still rules, see if there are missing matches
    if (codeNodes.isEmpty()) {
        return ruleNodes.none { it.quantifier == Quantifier.Any } &&
                quantifierCounters.allQualifiersMeetRequirements(ruleNodes)
    }

    val headCodeNode = codeNodes.first()
    val tailCodeNodes = codeNodes.drop(1)

    val applicableRuleNodes = ruleNodes.filter { it.elementType.isSuperclassOf(headCodeNode.element::class) }

    // We first check if there is any rule that is impossible to continue matching
    // e.g. All rule failing, Times rule greater than its counter
    val isPossibleToContinueMatching = applicableRuleNodes
        .any { !it.isPossibleToContinueMatching(headCodeNode.element, quantifierCounters) }

    if (isPossibleToContinueMatching) {
        return false
    }

    return applicableRuleNodes
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

private fun Counters.allQualifiersMeetRequirements(rules: List<LinNode<UElement>>): Boolean = rules.all { rule ->
    val quantifier = rule.quantifier
    when (quantifier) {
        Quantifier.All, Quantifier.Any -> true
        is Quantifier.Times -> getCount(quantifier) == quantifier.times
        is Quantifier.MoreThan -> getCount(quantifier) > quantifier.times
        is Quantifier.LessThan -> getCount(quantifier) < quantifier.times
    }
}

private fun LinNode<UElement>.isPossibleToContinueMatching(
    element: UElement,
    quantifierCounters: Map<Quantifier, Int>
): Boolean = quantifier.let {
    when (it) {
        Quantifier.All -> ruleMatchesAll(this, element)
        is Quantifier.Times -> quantifierCounters.getCount(it) < it.times
        is Quantifier.LessThan -> quantifierCounters.getCount(it) < it.times - 1
        Quantifier.Any, is Quantifier.MoreThan -> true
    }
}

private val matchesMemoizedValues = mutableMapOf<Triple<LinNode<UElement>, UElement, Counters>, Boolean>()
private fun LinNode<UElement>.matches(
    element: UElement,
    quantifierCounters: Map<Quantifier, Int>
): Boolean = matchesMemoizedValues.getOrPut(Triple(this, element, quantifierCounters)) {
    quantifier.let { quantifier ->
        return when (quantifier) {
            Quantifier.All -> ruleMatchesAll(this, element)
            Quantifier.Any -> ruleMatchesAny(this, element)
            is Quantifier.Times -> ruleMatchTimes(this, element, quantifierCounters, quantifier)
            is Quantifier.MoreThan -> ruleMatchMoreThan(this, element)
            is Quantifier.LessThan -> ruleMatchLessThan(this, element, quantifierCounters, quantifier)
        }
    }
}

private fun ruleMatchesAll(
    rule: LinNode<UElement>,
    element: UElement
): Boolean = rule.reportingPredicate(element)

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

    return quantifierCounters.getCount(quantifier) < quantifier.times
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

    return quantifierCounters.getCount(quantifier) < quantifier.times - 1
}