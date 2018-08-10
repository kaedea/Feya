@file:Suppress("CanBeVal", "UNUSED_EXPRESSION")

package com.kaedea.kotlin

import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.todo

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

Scoping function works like:
```
var zzz = [instance.]xxx[(receiver)] { [it -> ]
    this.foo
    [return yyy]
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

                               +---------------+
                               | return self ? |
                               +---------------+
                                       |
                            +----- N --+-- Y --------------+
                            |                              |
                    +---------------+              +---------------+
                    |   ext fun ?   |              | access self ? |
                    +---------------+              +---------------+
                            |                              |
             +----- N ------+-- Y --+              + this -+-- it -+
             |                      |              |               |
     +---------------+      +---------------+  "T.apply()"     "T.also()"
     | access self ? |      | access self ? |
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

    @Test
    @Ignore
    fun composition() {
        var musume = "22"

        run {
            if (musume.endsWith("2")) Droid.Musume22()
            else Droid.Musume33()
        }.greet()

        kotlin.run {
            if (musume.endsWith("2")) Droid.Musume22()
            else Droid.Musume33()
        }.greet()

        musume.run {
            if (endsWith("2")) Droid.Musume22()
            else Droid.Musume33()
        }

        TODO("Practice")
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
        todo {
            // impl
        }
    }

    @Test
    @Ignore
    fun takeUnless() {
        throw NotImplementedError()
    }
}

/**
 * Custom with function that take receiver as parameter, so that you can access receiver via `it`
 * in the closure block.
 */
inline fun <T, R> customWith(receiver: T, block: (T) -> R): R {
    return block(receiver)
}

sealed class Droid {
    abstract fun greet()
    class Musume22 : Droid() {
        override fun greet() {
            println("Ciao")
        }
    }

    class Musume33 : Droid() {
        override fun greet() {
            println("Hello")
        }
    }
}
