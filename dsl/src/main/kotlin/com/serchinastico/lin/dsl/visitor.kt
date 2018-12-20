package com.serchinastico.lin.dsl

import org.jetbrains.uast.UElement
import org.jetbrains.uast.visitor.UastVisitor
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

data class LinVisitor(val rule: LinRule) : UastVisitor {

    private val validUElementClassNames: List<KClass<out UElement>> by lazy {
        rule.root.allUElementSuperClasses()
    }
    private var currentNode: TreeNode? = null

    val shouldReport: Boolean by lazy {
        val root = currentNode ?: throw NoVisitCalled()

        rule.root.matches(root)
    }

    override fun visitElement(node: UElement): Boolean {
        if (!validUElementClassNames.any { it.isSuperclassOf(node::class) }) {
            return false
        }

        val newChild = TreeNode(mutableListOf(), currentNode, node)
        currentNode?.children?.add(newChild)
        currentNode = newChild
        return false
    }

    override fun afterVisitElement(node: UElement) {
        if (!validUElementClassNames.any { it.isSuperclassOf(node::class) }) {
            return
        }

        currentNode = currentNode?.parent ?: currentNode
    }

    class NoVisitCalled : java.lang.RuntimeException("Always call node.accept(visitor) to initialize the visitor")
}