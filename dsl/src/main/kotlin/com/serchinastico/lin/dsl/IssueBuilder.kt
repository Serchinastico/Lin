package com.serchinastico.lin.dsl

import com.android.tools.lint.detector.api.*
import java.util.*

inline fun <reified T : Detector> createIssue(
    id: String,
    scope: EnumSet<Scope>,
    description: String,
    explanation: String,
    category: Category,
    priority: Int = 5,
    severity: Severity = Severity.ERROR
) = Issue.create(id, description, explanation, category, priority, severity, Implementation(T::class.java, scope))