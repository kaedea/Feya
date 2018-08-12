@file:Suppress("CanBeVal", "UNUSED_EXPRESSION")

package com.kaedea.kotlin

import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

/**
 * Check [kotlin.util.Standard.kt][TODO]
 *
 * @author Kaede
 * @since  2018/8/9
 */

/*
Kotlin standard util apis contain:
1. Scoping functions: also, apply, let, run, with, (takeIf, takeUnless)
2. Utility functions: repeat, TODO
3. Utility class: NotImplementedError

_Scoping function_ works like:
```
var result = [caller.]xxx[(receiver)] { [it -> ]
    this.foo
    [return any]
}
```

| Function   | Function type | Parameter type |    Usage    | Access Self |
|------------|---------------|----------------|-------------|-------------|
|   T.also   |   Extension   |    Function    | self.also{} |      it     |
|   T.apply  |   Extension   | Extension Func | self.apply{}|     this    |
|   T.let    |   Extension   |    Function    | self.let{}  |      it     |
|   T.run    |   Extension   | Extension Func | self.run{}  |     this    |
|------------|---------------|----------------|-------------|-------------|
|    run     |   Top Level   |    Function    |    run{}    |      -      |
| with(self) |   Top Level   | Extension Func | with(self){}|     this    |

|  function  | "this" |  "it"  |  return      | signature                                     |
|------------|--------|--------|--------------|-----------------------------------------------|
|   T.also   |    -   |  self  |     self     | <T>    T.also(block: (T) -> Unit): T          |
|   T.apply  |  self  |    -   |     self     | <T>    T.apply(block: T.() -> Unit): T        |
|   T.let    |    -   |  self  | block return | <T, R> T.let(block: (T) -> R): R              |
|   T.run    |  self  |    -   | block return | <T, R> T.run(block: T.() -> R): R             |
|------------|--------|--------|--------------|-----------------------------------------------|
|    run     |    -   |    -   | block return | <R> run(block: () -> R): R                    |
| with(self) |  self  |    -   | block return | <T, R> with(receiver: T, block: T.() -> R): R |

1. run() has both of top-level & extension function: run(), T.run()
2. with() is a top-level function (static)
3. with() is the same as T.run() but not as an extension function, `x.run() {}`is the same as `with(x) {}`
4. T.xxx extension function can take context as implicit receiver, which make the codes work like top-level function

Which scoping function to choose?
1. What do we want to return?
   Do we need return the caller itself for chaining coding

2. Do we need a ext function?
   What codes do we want to coding: xxx(), caller.xxx(), or xxx(receiver)
   Ext function does avoid null check in closure

3. How to access caller?
   when `this` can be omitted, `this` is better
   `this` maybe references wrong context, `it` is clearer
   `it` can be renamed in need

                               +---------------+
                               | return self ? |
                               +---------------+
                                       |
                            +----- N --+-- Y --------------+
                            |                              |
                    +---------------+              +---------------+
                    |   ext fun ?   |              |  it or this?  |
                    +---------------+              +---------------+
                            |                              |
             +----- N ------+-- Y --+              + this -+-- it -+
             |                      |              |               |
     +---------------+      +---------------+  "T.apply()"     "T.also()"
     |  it or this?  |      |  it or this?  |
     +---------------+      +---------------+
             |                          |
     + this -+- n/a -+          + this -+-- it -+
     |               |          |               |
 "with(self)"     "run()"   "T.run()"       "T.let()"
 */

@RunWith(JUnit4::class)
class KtUtilExtFuncTest {

    @Test
    fun also() {
        // Access
        also {
            assertTrue(this.javaClass === KtUtilExtFuncTest::class.java)
            assertTrue(it.javaClass === KtUtilExtFuncTest::class.java)
        }

        var text = "hey"
        text.also {
            assertTrue(this.javaClass === KtUtilExtFuncTest::class.java)
            assertEquals("hey", it)
        }
        var result = text.also {
            "dude"
        }
        assertEquals("hey", result)

        // Example
        // Use context as implicit receiver
        // 1. also() { this: context, it: context -> context }
        also {
            println("type: ${it.javaClass.name}")
        }.also {
            println("super: ${it.javaClass.superclass.name}")
        }

        // Use explicit receiver
        // 2. self.also() { this: context, it: self -> self }
        text.also {
            println("content: $it")
        }.also {
            println("length: ${it.length}")
        }.also {
            println("type: ${it.javaClass.name}")
        }

        // Signature
        fun foo(block: (Any) -> Unit) = text.also(block)
        foo {
            println("content: $it")
        }
    }

    @Test
    fun apply() {
        // Access
        apply {
            assertTrue(this.javaClass === KtUtilExtFuncTest::class.java)
        }

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
        // Use context as implicit receiver
        // 1. apply() { this: context -> context }
        apply {
            println("type: ${javaClass.name}")
        }.apply {
            println("super: ${javaClass.superclass.name}")
        }

        // Use explicit receiver
        // 2. self.apply() { this: self -> self }
        text.apply {
            println("content: ${toString()}")
        }.apply {
            println("length: $length")
        }.apply {
            println("type: ${javaClass.name}")
        }

        // Signature
        fun foo(block: Any.() -> Unit) = text.apply(block)
        foo {
            println("content: ${toString()}")
        }
    }

    @Test
    fun let() {
        // Access
        let {
            assertTrue(this.javaClass === KtUtilExtFuncTest::class.java)
            assertTrue(it.javaClass === KtUtilExtFuncTest::class.java)
        }

        var text = "hey"
        text.let {
            assertTrue(this.javaClass === KtUtilExtFuncTest::class.java)
            assertEquals("hey", it)
        }
        var result = text.let {
            "dude"
        }
        assertEquals("dude", result)

        // Example
        // Use context as implicit receiver
        // 1. let() { this: context, it: context -> any }
        let {
            "hey"
        }.let {
            it.length
        }.let {
            it.javaClass
        }.let {
            println("type: ${it.name}")
        }

        // Use explicit receiver
        // 2. self.let() { this: context, it: self -> any }
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
        run {
            assertTrue(this.javaClass === KtUtilExtFuncTest::class.java)
        }

        var text = "hey"
        text.run {
            assertEquals("hey", this)
            // assertTrue(it == null), "it" is not support
        }
        var result = text.run {
            "dude"
        }
        assertEquals("dude", result)

        // Example
        // Use context as implicit receiver
        // 1. run() { this: context -> any}
        run {
            "hey"
        }.run {
            length
        }.run {
            javaClass
        }.run {
            println("type: $name")
        }

        // Use explicit receiver
        // 2. self.run() { this: self -> any }
        text.run {
            length
        }.run {
            javaClass
        }.run {
            println("type: $name")
        }
    }
}

@RunWith(JUnit4::class)
class KtUtilTopLevelFuncTest {

    @Test
    fun run() {
        // Access
        kotlin.run {
            assertTrue(this.javaClass === KtUtilTopLevelFuncTest::class.java)
            // assertTrue(it == null), "it" is not support
        }

        var result = kotlin.run {
            "dude"
        }
        assertEquals("dude", result)

        // Example
        // 1. run() { this: context -> any }
        kotlin.run {
            "hey"
        }.run {
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
            // assertTrue(it == null), "it" is not support
        }
        var result = with(text) {
            "dude"
        }
        assertEquals("dude", result)

        // Example
        // 1. with(self) { this: self -> any }
        with(text) {
            println("content: ${toString()}")
            println("length: $length")
            println("type: ${javaClass.name}")
        }
    }

    @Test
    fun customWith() {
        // Access
        var text = "hey"
        customWith(text) {
            assertTrue(this.javaClass === KtUtilTopLevelFuncTest::class.java)
            assertEquals("hey", it)
        }
        var result = customWith(text) {
            "dude"
        }
        assertEquals("dude", result)

        // Example
        // 1. customWith(self) { this: context, it: self  -> any }
        customWith(text) {
            println("content: $it")
            println("length: ${it.length}")
            println("type: ${it.javaClass.name}")
        }
    }
}

@RunWith(JUnit4::class)
class KtUtilMaybeUnpopularFuncTest {

    @Test
    fun repeat() {
        var sum = 0
        repeat(6) { index ->
            sum += index * index
        }
        assertEquals(0 + 1 + 4 + 9 + 16 + 25, sum)

        val arrays = Array<String?>(5) { null }
        arrays.forEach {
            assertNull(it)
        }
        repeat(5) {
            arrays[it] = it.toString()
        }
        for ((i, v) in arrays.withIndex()) {
            assertNotNull(v)
            assertEquals(i.toString(), v)
        }
    }

    @Test
    fun takeIf() {
        var droid: Musume?
        droid = Musume.Musume22().takeIf {
            when {
                it.id != 33 -> false
                it.javaClass != Musume.Musume33::class.java -> false
                else -> true
            }
        }
        assertNull(droid)

        droid = Musume.Musume33().takeIf {
            when {
                it.id != 33 -> false
                it.javaClass != Musume.Musume33::class.java -> false
                else -> true
            }
        }
        assertNotNull(droid)
    }

    @Test
    fun takeUnless() {
        var human: Musume?
        human = Musume.Musume22().takeUnless {
            when {
                it.id != 33 -> false
                it.javaClass != Musume.Musume33::class.java -> false
                else -> true
            }
        }
        assertNotNull(human)

        human = Musume.Musume33().takeUnless {
            when {
                it.id != 33 -> false
                it.javaClass != Musume.Musume33::class.java -> false
                else -> true
            }
        }
        assertNull(human)
    }

    @Test
    fun todo() {
        var error: Throwable?
        try {
            TODO("Tomorrow is always well!")
            fail()
        } catch (e: Error) {
            error = e
        }

        assertNotNull(error)
        assertTrue(error is NotImplementedError)
    }
}

@RunWith(JUnit4::class)
class KtUtilCompositionTest {

    @Test
    fun composition() {
        // 1. Closure scoping variable
        var musume = "22"
        kotlin.run {
            var musume = "33"
            assertEquals("33", musume)
        }
        assertEquals("22", musume)

        // 2. Condition return
        // With traditional imperative codes, with need to call #greet for both 22 & 33
        // Now with scoping function, we just call it once in closure return
        kotlin.run {
            if (musume.endsWith("2")) Musume.Musume22()
            else Musume.Musume33()
        }.greet()

        musume.run {
            if (endsWith("2")) Musume.Musume22()
            else Musume.Musume33()
        }.greet()

        // 3. Avoid null check
        // This is the benefit of extension function
        var droid: Musume? = null
        droid?.run {
            println(id)
            greet()
        }
        // Non-ext function does not work
        kotlin.run {
            println(droid?.id)
            droid?.greet()
        }
        with(droid) {
            println(this?.id)
            this?.greet()
        }

        // 4. Chain coding
        // This is the benefit of self-return
        fun makeDir(path: String) = path.let { File(it) }.also { it.mkdirs() }.takeIf { it.exists() }

        // Normal approach
        fun makeDirNormal(path: String): File? {
            val result = File(path)
            result.mkdirs()
            if (result.exists()) return result
            return null
        }

        // More chaining
        fun getUri(scheme: String?, host: String?, path: String?) = {
            Uri().apply {
                if (scheme!! in arrayOf("http", "https")) throw IllegalArgumentException()
                sch = scheme
            }.apply {
                hst = host ?: "unknown"
            }.apply {
                pat = path
            }
        }
    }
}

/**
 * Custom with function that take receiver as parameter, so that you can access receiver via `it`
 * in the closure block.
 */
inline fun <T, R> customWith(receiver: T, block: (T) -> R): R {
    return block(receiver)
}

sealed class Musume {
    abstract val id: Int
    abstract fun greet()
    class Musume22(override val id: Int = 22) : Musume() {
        override fun greet() {
            println("Ciao")
        }
    }

    class Musume33(override val id: Int = 33) : Musume() {
        override fun greet() {
            println("Hello")
        }
    }
}

internal class Uri {
    var sch: String? = null
    var hst: String? = null
    var pat: String? = null
}
