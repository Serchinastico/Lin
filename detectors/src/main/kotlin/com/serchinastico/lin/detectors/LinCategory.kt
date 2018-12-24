package com.serchinastico.lin.detectors

import com.android.tools.lint.detector.api.Category

val Category.Companion.Lin: Category
    get() = Category.getCategory("Lin") ?: Category.create("Lin", 100)