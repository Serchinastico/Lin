package com.serchinastico.lin.dsl

import org.jetbrains.uast.UElement
import org.jetbrains.uast.visitor.UastVisitor
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

data class LinVisitor(val detector: LinDetector) : UastVisitor {

    private val validUElementClassNames: Set<KClass<out UElement>> by lazy {
        detector.roots.flatMap { it.allUElementSuperClasses() }.toSet()
    }
    private var root: MutableList<TreeNode> = mutableListOf()
    private var currentNode: TreeNode? = null

    val shouldReport: Boolean
        get() {
            return detector.roots.matchesAny(root)
        }

    operator fun plusAssign(localVisitor: LinVisitor) {
        root.addAll(localVisitor.root)
    }

    override fun visitElement(node: UElement): Boolean {
        if (!validUElementClassNames.any { it.isSuperclassOf(node::class) }) {
            return false
        }

        val newChild = TreeNode(node, currentNode, mutableListOf())
        val newChildParent = currentNode.let { it?.children ?: root }
        newChildParent.add(newChild)
        currentNode = newChild
        return false
    }

    override fun afterVisitElement(node: UElement) {
        if (!validUElementClassNames.any { it.isSuperclassOf(node::class) }) {
            return
        }
        currentNode = currentNode?.parent
    }
}

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