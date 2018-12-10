package com.serchinastico.lin

import android.view.View

class TestClass {
    public val view: View = View(null)

    fun main() {
        val asd: Asd = Asd.valueOf("Asd")
        when (asd) {
            Asd.Asd -> print("asd")
            Asd.Bsd -> print("bsd")
            else -> print("csd")
        }
    }

    enum class Asd {
        Asd, Bsd, Csd
    }
}
