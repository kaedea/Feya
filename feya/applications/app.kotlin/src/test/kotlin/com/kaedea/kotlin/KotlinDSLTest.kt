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
    fun dslWithInvoke() {
        class DependencyHandler {
            infix fun compile(dep: String) = this
            operator fun invoke(body: DependencyHandler.() -> Unit) = body()
        }

        val dependencies = DependencyHandler()

        dependencies.compile("com.kaedea.kotlin:dsl:0.1.0")
        dependencies compile "com.kaedea.kotlin:dsl:0.1.0"

        dependencies {
            compile("com.kaedea.kotlin:dsl:0.1.0")
        }

        assertEquals("com.kaedea.kotlin:dsl:0.1.0", dependencies {
            compile("com.kaedea.kotlin:dsl:0.1.0")
        })
    }

    @Test
    fun dslWithInvoke2() {
        dependencies.compile("com.kaedea.kotlin:dsl:0.1.0")
        dependencies compile "com.kaedea.kotlin:dsl:0.1.0"

        dependencies {
            compile("com.kaedea.kotlin:dsl:0.1.0")
        }

        assertEquals("com.kaedea.kotlin:dsl:0.1.0", dependencies {
            compile("com.kaedea.kotlin:dsl:0.1.0")
        })
    }

    object dependencies {
        infix fun compile(dep: String) = this
        operator fun invoke(body: dependencies.() -> Unit) = this.body()
    }

    @Test
    fun dslWithInvoke3() {
        class DependencyHandler {
            infix fun compile(dep: String) = this
        }

        fun dependencies(body: DependencyHandler.() -> Unit) {
            val dependencies = DependencyHandler()
            dependencies.body()
        }

        dependencies {
            compile("com.kaedea.kotlin:dsl:0.1.0")
        }
        dependencies {
            this compile "com.kaedea.kotlin:dsl:0.1.0" // Damn, 'this' can not be omitted in infix function
        }

        assertEquals("com.kaedea.kotlin:dsl:0.1.0", KtDslFuncTest.dependencies {
            this compile "com.kaedea.kotlin:dsl:0.1.0"
        })
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

        1 days agos
        val date = 1 days agos

        assertEquals(yesterday, date)
    }

    fun Int.days() = Period.ofDays(this)
    val Int.days
        get() = Period.ofDays(this)
    val Period.ago
        get() = LocalDate.now() - this

    object agos

    private inline infix fun Int.days(x: agos) = LocalDate.now() - Period.ofDays(this)

    @Test
    fun dslWithMemberExt() {
        val user = User("kidhaibara@gmail.com")
        // user.computeName()   // can not access member ext out of class
        // "text".computeName() // can not access member ext in ext target
        assertEquals("kidhaibara", user.name)

        class Guest : User(email = "empty") {
            // access member ext fun within inheritor class
            override val name = "guest@xxx.com".computeName()
        }
        assertEquals("guest", Guest().name)
    }

    open class User(val email: String) {
        open val name = email.computeName() // access member ext fun within class
        public fun String.computeName() = this.substringBefore("@")
    }

    @Test
    fun dslWithHtml() {
        table {
            tr {
                td {

                }
            }
        }

    }

    open class Tag(val name: String) {
        private val items = arrayListOf<Tag>()
        protected fun <T : Tag> addItem(item: T, init: T.() -> Unit) {
            item.init()
            items.add(item)
        }

        override fun toString() = "<$name>${items.joinToString("")}</$name>"
    }

    class TD : Tag("td")

    class TR : Tag("tr") {
        fun td(init: TD.() -> Unit) = addItem(TD(), init)
    }

    class TABLE : Tag("table") {
        fun tr(init: TR.() -> Unit)  = addItem(TR(), init)
    }

    private inline fun table(init: TABLE.() -> Unit) = TABLE().apply(init)
}
