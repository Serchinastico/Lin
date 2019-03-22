package com.serchinastico.lin

import android.app.Activity
import android.os.Bundle
import android.view.View

class TestClass : Activity() {
    lateinit var asd: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<View>(R.id.action_bar)
    }
}
