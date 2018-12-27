package com.serchinastico.lin.dsl

import com.serchinastico.lin.dsl.MatchResult.Companion.NoMatch
import com.serchinastico.lin.dsl.MatchResult.Companion.maybe
import org.jetbrains.uast.UElement
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

private data class MatchLinContext(
    override val storage: Storage = mutableMapOf(),
    val counterOfQuantifiers: Counters = mapOf()
) : LinContext {
    fun plus(quantifier: Quantifier): MatchLinContext =
        copy(counterOfQuantifiers = counterOfQuantifiers.plusOne(quantifier))
}

private data class MatchResult(
    val didFoundMatch: Boolean,
    val elementsMatching: List<UElement>
) {
    companion object {
        val NoMatch: MatchResult = MatchResult(false, emptyList())
        fun maybe(didMatch: Boolean) = MatchResult(didMatch, emptyList())
    }

    fun and(block: (MatchResult) -> MatchResult): MatchResult {
        return if (didFoundMatch) {
            plus(block(this))
        } else {
            this
        }
    }

    fun plus(result: MatchResult): MatchResult =
        copy(
            didFoundMatch = didFoundMatch && result.didFoundMatch,
            elementsMatching = elementsMatching.plus(result.elementsMatching)
        )

    fun plus(element: UElement): MatchResult =
        copy(elementsMatching = elementsMatching.plus(element))
}

private typealias Counters = Map<Quantifier, Int>

private fun Counters.getCount(quantifier: Quantifier): Int = this.getOrDefault(quantifier, 0)
private fun Counters.plusOne(quantifier: Quantifier): Counters = plus(quantifier to getCount(quantifier) + 1)

fun List<LinRule<UElement>>.matchesAny(code: List<TreeNode>): List<UElement> =
    stream()
        .map { matchesAll(code, listOf(it)) }
        .filter { it.isNotEmpty() }
        .findFirst()
        .orElse(emptyList())

private fun matchesAll(codeNodes: List<TreeNode>, rules: List<LinRule<UElement>>): List<UElement> =
    matchesAll(codeNodes, rules, MatchLinContext()).let { if (it.didFoundMatch) it.elementsMatching else emptyList() }

private fun matchesAll(
    codeNodes: List<TreeNode>,
    rules: List<LinRule<UElement>>,
    context: MatchLinContext
): MatchResult {
    // We finished processing rules and found a match, we succeeded as long as quantifiers match their requirements
    if (rules.isEmpty()) {
        return maybe(context.counterOfQuantifiers.allQualifiersMeetRequirements(rules))
    }

    // We don't have more code and there are still rules, see if there are missing matches
    if (codeNodes.isEmpty()) {
        return maybe(rules.none { it.quantifier == Quantifier.Any } &&
                context.counterOfQuantifiers.allQualifiersMeetRequirements(rules))
    }

    val headCodeNode = codeNodes.first()
    val tailCodeNodes = codeNodes.drop(1)

    val applicableRules = rules.filter { it.elementType.isSuperclassOf(headCodeNode.element::class) }

    // We first check if there is any rule that is impossible to continue matching
    // e.g. All rule failing, Times rule greater than its counter
    val isPossibleToContinueMatching = applicableRules
        .all { it.isPossibleToContinueMatching(headCodeNode.element, context) }

    if (!isPossibleToContinueMatching) {
        return NoMatch
    }

    val result = applicableRules
        .map { it to context.copy() }
        .filter { it.first.matches(headCodeNode.element, it.second) }
        .stream()
        .map { it.first.matchesChildren(headCodeNode, tailCodeNodes, rules, it.second) }
        .findFirst()
        .orElse(NoMatch)

    return when {
        result.didFoundMatch -> result.plus(headCodeNode.element)
        rules.none { it.quantifier == Quantifier.All } -> matchesAll(tailCodeNodes, rules, context.copy())
        else -> NoMatch
    }
}

private fun LinRule<UElement>.matchesChildren(
    head: TreeNode,
    tail: List<TreeNode>,
    rules: List<LinRule<UElement>>,
    context: MatchLinContext
): MatchResult {
    return when (quantifier) {
        Quantifier.All ->
            matchesAll(head.children, children, context)
                .and { matchesAll(tail, rules, context) }
        Quantifier.Any ->
            matchesAll(head.children, children, context)
                .and { matchesAll(tail, rules.minus(this@matchesChildren), context) }
        is Quantifier.Times, is Quantifier.AtLeast, is Quantifier.AtMost ->
            matchesAll(head.children, children, context)
                .and {
                    matchesAll(
                        tail,
                        rules,
                        context.copy(counterOfQuantifiers = context.counterOfQuantifiers.plusOne(quantifier))
                    )
                }
    }
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
    context: MatchLinContext
): Boolean = quantifier.let {
    when (it) {
        Quantifier.All -> ruleMatchesAll(this, element, context)
        is Quantifier.Times -> context.counterOfQuantifiers.getCount(it) < it.times
        is Quantifier.AtMost -> context.counterOfQuantifiers.getCount(it) < it.times
        Quantifier.Any, is Quantifier.AtLeast -> true
    }
}

private fun LinRule<UElement>.matches(
    element: UElement,
    context: MatchLinContext
): Boolean = quantifier.let { quantifier ->
    return when (quantifier) {
        Quantifier.All -> ruleMatchesAll(this, element, context)
        Quantifier.Any -> ruleMatchesAny(this, element, context)
        is Quantifier.Times -> ruleMatchTimes(this, element, quantifier, context)
        is Quantifier.AtLeast -> ruleMatchAtLeast(this, element, context)
        is Quantifier.AtMost -> ruleMatchAtMost(this, element, quantifier, context)
    }
}

private fun ruleMatchesAll(
    rule: LinRule<UElement>,
    element: UElement,
    context: MatchLinContext
): Boolean = rule.reportingPredicate(context, element)

private fun ruleMatchesAny(
    rule: LinRule<UElement>,
    element: UElement,
    context: MatchLinContext
): Boolean = rule.reportingPredicate(context, element)

private fun ruleMatchTimes(
    rule: LinRule<UElement>,
    element: UElement,
    quantifier: Quantifier.Times,
    context: MatchLinContext
): Boolean {
    if (!rule.reportingPredicate(context, element)) {
        return false
    }

    return context.counterOfQuantifiers.getCount(quantifier) < quantifier.times
}

private fun ruleMatchAtLeast(
    rule: LinRule<UElement>,
    element: UElement,
    context: MatchLinContext
): Boolean = rule.reportingPredicate(context, element)

private fun ruleMatchAtMost(
    rule: LinRule<UElement>,
    element: UElement,
    quantifier: Quantifier.AtMost,
    context: MatchLinContext
): Boolean {
    if (!rule.reportingPredicate(context, element)) {
        return false
    }

    return context.counterOfQuantifiers.getCount(quantifier) < quantifier.times - 1
}