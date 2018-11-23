package com.serchinastico.lin

import android.app.Activity
import android.os.Bundle
import android.view.View

class SampleActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val asd: View = findViewById<View>(R.id.content)
    }
}