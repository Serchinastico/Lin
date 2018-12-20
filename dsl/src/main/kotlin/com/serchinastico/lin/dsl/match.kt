package com.serchinastico.lin.dsl

import org.jetbrains.uast.UElement
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf


data class TreeNode(
    val element: UElement,
    var parent: TreeNode?,
    val children: MutableList<TreeNode>
) {
    override fun toString(): String {
        return "TreeNode(children=$children, element=$element)"
    }
}

typealias Counters = Map<Quantifier, Int>

fun Counters.getCount(quantifier: Quantifier): Int = this.getOrDefault(quantifier, 0)
fun Counters.plusOne(quantifier: Quantifier): Counters = plus(quantifier to getCount(quantifier) + 1)

fun List<LinRule<UElement>>.matchesAny(code: List<TreeNode>): Boolean = this.any { matchesAll(code, listOf(it)) }

fun LinRule<UElement>.allUElementSuperClasses(): List<KClass<out UElement>> {
    val superClasses = mutableSetOf<KClass<out UElement>>()
    val nodesToProcess = mutableListOf(this)

    while (nodesToProcess.isNotEmpty()) {
        val currentNode = nodesToProcess.removeAt(0)
        superClasses.add(currentNode.elementType)
        nodesToProcess.addAll(currentNode.children)
    }

    return superClasses.toList()
}

private fun matchesAll(codeNodes: List<TreeNode>, rules: List<LinRule<UElement>>): Boolean =
    matchesAll(codeNodes, rules, emptyMap())

private fun matchesAll(
    codeNodes: List<TreeNode>,
    rules: List<LinRule<UElement>>,
    quantifierCounters: Map<Quantifier, Int>
): Boolean {
    // We finished processing rules and found a match, we succeeded as long as quantifiers match their requirements
    if (rules.isEmpty()) {
        return quantifierCounters.allQualifiersMeetRequirements(rules)
    }

    // We don't have more code and there are still rules, see if there are missing matches
    if (codeNodes.isEmpty()) {
        return rules.none { it.quantifier == Quantifier.Any } &&
                quantifierCounters.allQualifiersMeetRequirements(rules)
    }

    val headCodeNode = codeNodes.first()
    val tailCodeNodes = codeNodes.drop(1)

    val applicableRules = rules.filter { it.elementType.isSuperclassOf(headCodeNode.element::class) }

    // We first check if there is any rule that is impossible to continue matching
    // e.g. All rule failing, Times rule greater than its counter
    val isPossibleToContinueMatching = applicableRules
        .all { it.isPossibleToContinueMatching(headCodeNode.element, quantifierCounters) }

    if (!isPossibleToContinueMatching) {
        return false
    }

    return applicableRules
        .filter { it.matches(headCodeNode.element, quantifierCounters) }
        .any { ruleNode ->
            when (ruleNode.quantifier) {
                Quantifier.All -> matchesAll(headCodeNode.children, ruleNode.children, quantifierCounters) &&
                        matchesAll(tailCodeNodes, rules, quantifierCounters)
                Quantifier.Any -> matchesAll(headCodeNode.children, ruleNode.children, quantifierCounters) &&
                        matchesAll(tailCodeNodes, rules.minus(ruleNode), quantifierCounters)
                is Quantifier.Times, is Quantifier.AtLeast, is Quantifier.AtMost ->
                    matchesAll(headCodeNode.children, ruleNode.children, quantifierCounters) &&
                            matchesAll(tailCodeNodes, rules, quantifierCounters.plusOne(ruleNode.quantifier))
            }
        } || ((rules.none { it.quantifier == Quantifier.All } &&
            matchesAll(tailCodeNodes, rules, quantifierCounters)))
}

private fun Counters.allQualifiersMeetRequirements(rules: List<LinRule<UElement>>): Boolean = rules.all { rule ->
    val quantifier = rule.quantifier
    when (quantifier) {
        Quantifier.All, Quantifier.Any -> true
        is Quantifier.Times -> getCount(quantifier) == quantifier.times
        is Quantifier.AtLeast -> getCount(quantifier) >= quantifier.times
        is Quantifier.AtMost -> getCount(quantifier) <= quantifier.times
    }
}

private fun LinRule<UElement>.isPossibleToContinueMatching(
    element: UElement,
    quantifierCounters: Map<Quantifier, Int>
): Boolean = quantifier.let {
    when (it) {
        Quantifier.All -> ruleMatchesAll(this, element)
        is Quantifier.Times -> quantifierCounters.getCount(it) < it.times
        is Quantifier.AtMost -> quantifierCounters.getCount(it) < it.times
        Quantifier.Any, is Quantifier.AtLeast -> true
    }
}

private val matchesMemoizedValues = mutableMapOf<Triple<LinRule<UElement>, UElement, Counters>, Boolean>()
private fun LinRule<UElement>.matches(
    element: UElement,
    quantifierCounters: Map<Quantifier, Int>
): Boolean = matchesMemoizedValues.getOrPut(Triple(this, element, quantifierCounters)) {
    quantifier.let { quantifier ->
        return when (quantifier) {
            Quantifier.All -> ruleMatchesAll(this, element)
            Quantifier.Any -> ruleMatchesAny(this, element)
            is Quantifier.Times -> ruleMatchTimes(this, element, quantifierCounters, quantifier)
            is Quantifier.AtLeast -> ruleMatchAtLeast(this, element)
            is Quantifier.AtMost -> ruleMatchAtMost(this, element, quantifierCounters, quantifier)
        }
    }
}

private fun ruleMatchesAll(
    rule: LinRule<UElement>,
    element: UElement
): Boolean = rule.reportingPredicate(element)

private fun ruleMatchesAny(
    rule: LinRule<UElement>,
    element: UElement
): Boolean = rule.reportingPredicate(element)

private fun ruleMatchTimes(
    rule: LinRule<UElement>,
    element: UElement,
    quantifierCounters: Map<Quantifier, Int>,
    quantifier: Quantifier.Times
): Boolean {
    if (!rule.reportingPredicate(element)) {
        return false
    }

    return quantifierCounters.getCount(quantifier) < quantifier.times
}

private fun ruleMatchAtLeast(
    rule: LinRule<UElement>,
    element: UElement
): Boolean = rule.reportingPredicate(element)

private fun ruleMatchAtMost(
    rule: LinRule<UElement>,
    element: UElement,
    quantifierCounters: Map<Quantifier, Int>,
    quantifier: Quantifier.AtMost
): Boolean {
    if (!rule.reportingPredicate(element)) {
        return false
    }

    return quantifierCounters.getCount(quantifier) < quantifier.times - 1
}