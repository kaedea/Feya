package com.kaedea.kotlin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Check [TODO] (location "kotlin.util.Standard.kt")
 *
 * @author Kaede
 * @since  2018/8/9
 */

@RunWith(JUnit4::class)
class KotlinUtilTest {
    /*
    ```
    | Function   | Function type | Target passed as | Returns            |
    |------------|---------------|------------------|--------------------|
    |   `also`   | Extension     | `it`             | Target             |
    |   `apply`  | Extension     | `this`           | Target             |
    |   `let`    | Extension     | `it`             | Block return value |
    |   `run`    | Extension     | `this`           | Block return value |
    |------------|---------------|------------------|--------------------|
    |   `with`   | Top Level     | `this`           | Block return value |

    | function | "this" |  "it"  |  return      | signature                                     |
    |----------|--------|--------|--------------|-----------------------------------------------|
    |   also   |    -   |  self  |     self     | <T>    T.also(block: (T) -> Unit): T          |
    |   apply  |  self  |    -   |     self     | <T>    T.apply(block: T.() -> Unit): T        |
    |   let    |    -   |  self  | block return | <T, R> T.let(block: (T) -> R): R              |
    |   run    |  self  |    -   | block return | <T, R> T.run(block: T.() -> R): R             |
    |----------|--------|--------|--------------|-----------------------------------------------|
    |   with   |        |        | block return | <T, R> with(receiver: T, block: T.() -> R): R |

    with() is a top-level function (static)
    with() is the same as T.run() but not as an extension function
    "x.run() {}" is the same as "with(x) {}"
    ```
     */
    @Test
    fun also() {
        // Access
        var text = "hey"
        text.also {
            assertTrue(this.javaClass === KotlinUtilTest::class.java)
            assertEquals("hey", it)
        }
        var result = text.also {
            "dude"
        }
        assertEquals("hey", result)

        // Example
        text.also {
            println("content: $it")
        }.also {
            println("length: ${it.length}")
        }.also {
            println("type: ${it.javaClass.name}")
        }
    }

    @Test
    fun apply() {
        // Access
        var text = "hey"
        text.apply {
            assertEquals("hey", this)
            // assertTrue(it == null) // No it argument
        }
        var result = text.apply {
            "dude"
        }
        assertEquals("hey", result)

        // Example
        text.apply {
            println("content: ${toString()}")
        }.apply {
            println("length: $length")
        }.apply {
            println("type: $javaClass.name")
        }
    }

    @Test
    fun let() {
        // Access
        var text = "hey"
        text.let {
            assertTrue(this.javaClass === KotlinUtilTest::class.java)
            assertEquals("hey", it)
        }
        var result = text.let {
            "dude"
        }
        assertEquals("dude", result)

        // Example
        text.let {
            it.length
        }.let {
            it.javaClass
        }.let {
            println("type: ${it.name}")
        }
    }

    @Test
    fun run() {
        // Access
        var text = "hey"
        text.run {
            assertEquals("hey", this)
            // assertTrue(it == null) // No it argument
        }
        var result = text.run {
            "dude"
        }
        assertEquals("dude", result)

        // Example
        text.run {
            length
        }.run {
            javaClass
        }.run {
            println("type: $name")
        }
    }

    @Test
    fun with() {
        // Access
        var text = "hey"
        with(text) {
            assertEquals("hey", this)
            // assertTrue(it == null) // No it argument
        }
        var result = with(text) {
            "dude"
        }
        assertEquals("dude", result)

        // Example
        with(text) {
            println("content: ${toString()}")
            println("length: $length")
            println("type: $javaClass.name")
        }
    }

    @Test
    @Ignore
    fun composition() {

    }
}