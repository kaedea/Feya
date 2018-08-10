@file:Suppress("CanBeVal", "UNUSED_EXPRESSION")

package com.kaedea.kotlin

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.fail

@RunWith(JUnit4::class)
class KtExtFuncTest {

    @Test
    fun ifTrueThen() {
        // if true then ..
        fun Boolean.then(block: () -> Unit) {
            println("receiver: $this")
            if (this) block()
        }

        var text = "#aec2cc"
        text.startsWith("#").then {
            text = text.replace("#", "kaede & ")
        }
        assertEquals("kaede & aec2cc", text)

        text.startsWith("#").then {
            fail()
        }

        fun ifText(text: String) = text
        ifText("#aec2cc").startsWith("#").then { }
    }

    @Test
    fun ifNotNullAndTrueThen() {
        // if true or Notnull
        fun <T> T.then(block: () -> Unit) {
            println("receiver: $this")
            if (this == null) return // null
            if (this is Boolean && !this) return // false
            if (this is String && this.equals("false", true)) return // string "false"
            block()
        }

        var text = "#aec2cc"
        text.startsWith("#").then {
            text = text.replace("#", "kaede & ")
        }
        assertEquals("kaede & aec2cc", text)

        text.startsWith("#").then {
            fail()
        }

        "false".then { fail() }
        "FALSE".then { fail() }
        null.then { fail() }
    }
}

