package com.kaedea.kotlin

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.LocalDate
import java.time.Period

/**
 * @author Kaede
 * @since  2018/8/10
 */

@RunWith(JUnit4::class)
class KtDslFuncTest {

    @Test
    fun createWithDsl() {
        fun uri(block: Uri.() -> Unit): Uri {
            val instance = Uri()
            instance.block()
            return instance
        }

        val uri = uri {
            sch = "scheme"
            hst = "host"
            pat = "path"
        }

        assertNotNull(uri)
        assertEquals("scheme", uri.sch)
        assertEquals("host", uri.hst)
        assertEquals("path", uri.pat)
    }

    @Test
    fun dslWithInfix() {
        "kotlin".should().with("kot")
        "kotlin".should(start).with("kot")
        "kotlin" should (start) with ("kot")
        "kotlin" should start with ("kot")
        "kotlin" should start with "kot"

        "kotlin".should() endsWith "in"
        try {
            "kotlin".should() endsWith "kot"
            fail()
        } catch (e: Error) {
        }
    }

    class StartWrapper(private val value: String) {
        infix fun with(prefix: String) {
            if (!value.startsWith(prefix))
                throw AssertionError("$value does not start with $prefix !")
        }

        infix fun endsWith(suffix: String) {
            if (!value.endsWith(suffix))
                throw AssertionError("$value does not ends with $suffix !")
        }
    }

    fun String.should() = StartWrapper(this)

    object start

    infix fun String.should(x: start) = StartWrapper(this)

    @Test
    fun dslWithExtProperty() {
        1.days()
        1.days
        1.days.ago

        assertEquals(1.days(), 1.days)

        val now = LocalDate.now()
        val yesterday = 1.days.ago

        assertEquals(yesterday, now - 1.days)
        assertEquals(yesterday, 1.days.ago)

        1 days ago
        val date = 1 days ago

        assertEquals(yesterday, date)
    }

    fun Int.days() = Period.ofDays(this)
    val Int.days
        get() = Period.ofDays(this)
    val Period.ago
        get() = LocalDate.now() - this
}

object ago

internal inline infix fun Int.days(x: ago) = LocalDate.now() - Period.ofDays(this)
