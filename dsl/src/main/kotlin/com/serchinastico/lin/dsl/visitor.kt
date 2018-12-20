package com.serchinastico.lin.dsl

import org.jetbrains.uast.UElement
import org.jetbrains.uast.visitor.UastVisitor
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

data class LinVisitor(val rule: LinRule) : UastVisitor {

    private val validUElementClassNames: List<KClass<out UElement>> by lazy {
        rule.root.allUElementSuperClasses()
    }
    private var root: MutableList<TreeNode> = mutableListOf()
    private var currentNode: TreeNode? = null

    val shouldReport: Boolean
        get() {
            return rule.root.matches(root)
        }

    operator fun plusAssign(localVisitor: LinVisitor) {
        root.addAll(localVisitor.root)
    }

    override fun visitElement(node: UElement): Boolean {
        if (!validUElementClassNames.any { it.isSuperclassOf(node::class) }) {
            return false
        }
        println("VISIT ${this.hashCode()} - $node")

        val newChild = TreeNode(node, currentNode, mutableListOf())
        val newChildParent = currentNode.let { it?.children ?: root }
        println("ADDING TO: $newChildParent")
        newChildParent.add(newChild)
        currentNode = newChild
        return false
    }

    override fun afterVisitElement(node: UElement) {
        if (!validUElementClassNames.any { it.isSuperclassOf(node::class) }) {
            return
        }
        println("AFTER VISIT ${this.hashCode()} - $node")
        println("ROOT: $root")
        currentNode = currentNode?.parent
    }
}