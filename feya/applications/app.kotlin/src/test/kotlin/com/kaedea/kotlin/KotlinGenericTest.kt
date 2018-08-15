package com.kaedea.kotlin

import com.kaedea.kotlin.utils.Values
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tweaking of Kotlin generic features:
 * - is-check & casting
 * - reified
 * - restrict of type parameter
 * - invariance & variance
 * - type projecting
 *
 * Runtime apis about Kotlin's generic:
 * - [kotlin.reflect.KVariance]
 * - [kotlin.reflect.KTypeParameter]
 * - [kotlin.reflect.KTypeProjection]
 *
 * @author Kaede
 * @since  2018/8/14
 */

class KtGenericTest {

    @Test
    fun nullability() {
        // type parameter is nullable by default
        val list = ArrayList<String?>()
        list.add(null)
        assertNotNull(list)
        assertEquals(1, list.size)
        assertNull(null, list.first())
        assertTrue(list.any { it == null })

        // Nonnull type parameter
        // Restrict type parameter's upperBound to Any
        class NotNullList<T : Any> : ArrayList<T>()

        val notNullList = NotNullList<String>()
        notNullList.add("text")
        // notNullList.add(null) // compile error
        assertTrue(!notNullList.any { it == null })
        notNullList.add(Values.nullString()) // runtime is ok
        assertTrue(notNullList.any { it == null })
    }

    /**
     * Raw check : is-check of type parameter
     * All raw check are meaningless because cast of ClassName<T> is regarded as ClassName<*>. Thus
     * `ClassName<*> is ClassName<String>` or `ClassName<*> is ClassName<Int>` is always true.
     *
     * Generic class check : is-check of generic class itself
     * The is-check of ClassName class works the same with normal class.
     */
    @Test
    fun isCheck() {
        // raw check
        fun checkTypeParameter(list: List<String>): Boolean {
            return list is List<String> // is-check is ok when compiler has enough type info
        }

        assertTrue(checkTypeParameter(arrayListOf("a", "b", "c")))
        // assertTrue(checkTypeParameter(arrayListOf(22, 33)))     // compile error
        assertTrue(checkTypeParameter(Values.stringListWithInt())) // runtime is ok, raw check is useless

        // check of class
        fun checkOfClass(list: List<*>): Boolean {
            // return list is ArrayList<String> // compile error
            return list is ArrayList<*>         // ok
        }

        assertTrue(checkOfClass(arrayListOf("a", "b", "c")))
        assertTrue(checkOfClass(arrayListOf(22, 33)))
        assertTrue(!checkOfClass(listOf(22, 33))) // Arrays$ArrayList is not ArrayList
    }

    /**
     * Raw cast : casting of type parameter
     * All raw cast are meaningless because cast of ClassName<T> is regarded as ClassName<*>. Every
     * raw cast should be wrapped with protecting codes.
     *
     * Generic class cast : casting of generic class itself
     * The casting of ClassName class works the same with normal class.
     */
    @Test
    fun casting() {
        // raw cast
        fun cast(list: List<*>): List<String>? {
            return list as? List<String>
        }

        assertNotNull(cast(arrayListOf("a", "b", "c")))
        assertNotNull(cast(arrayListOf(22, 33))) // raw cast (cast of type parameter) is useless

        // wrapper of raw cast
        fun safeCast(list: List<*>): List<String>? {
            if (list.all { it is String }) {
                return list as List<String>
            }
            return null
        }

        assertNotNull(safeCast(arrayListOf("a", "b", "c")))
        assertNull(safeCast(arrayListOf(22, 33))) // raw cast should be wrapped safely

        // cast of generic class
        fun castOfClass(list: List<String>): ArrayList<String>? {
            return list as? ArrayList<String>
        }

        assertNotNull(castOfClass(arrayListOf("a", "b", "c"))) // ArrayList
        assertNull(castOfClass(listOf("a", "b", "c")))         // Arrays$ArrayList, cast of generic class is ok
    }

    @Test
    fun reified() {
        fun <T> isGivenType(instance: Any): Boolean {
            // T::class      // raw literal is not allowed
            // instance is T // raw check is not allowed
            return false
        }

        assertTrue(!isGivenType<String>("text"))
        assertTrue(!isGivenType<Int>(2233))

        assertTrue(isGivenTypeReified<String>("text"))
        assertTrue(isGivenTypeReified<Int>(2233))

        assertEquals("User", reifiedInsteadOfClassLiteral<User>())
        assertEquals("Person", reifiedInsteadOfClassLiteral<Person>())
    }

    @Test
    fun invariant() {
        open class User(val name: String)
        class Guest : User("guest")

        // Array<T>
        // Array<Guest> is not subtype of Array<User>
        // Array<Any>   is not subtype of Array<User>
        fun allUsers(users: Array<User>) = users.joinToString(",", prefix = "[", postfix = "]") { it.name }

        val users: Array<User> = arrayOf(User("a"), User("b"))
        assertEquals("[a,b]", allUsers(users))

        val guests: Array<Guest> = arrayOf(Guest(), Guest())
        // assertEquals("[guest,guest]", allUsers(guests)) // compile error, type mismatch

        val castToAnys = guests as Array<Any>
        // assertEquals("[guest,guest]", allUsers(castToAnys)) // compile error, type mismatch

        val castToUsers = guests as Array<User>
        assertEquals("[guest,guest]", allUsers(castToUsers))
    }

    /**
     * `User<T>` & `Guest<T> : User<T>`
     * Subtyping:
     * 1. Subtyping of T within User
     * 2. Subtyping of T within Guest (Subtyping of inheritant)
     * 3. Subtyping of projection (how does User effect Guest is projection)
     *
     * Usecases:
     * 1. As parameter
     * 2. As return type
     * 3. As variable
     */
    @Test
    fun covariant() {
        open class User(val name: String)
        class Guest : User("guest")

        // Out/Extend/Covariant Projection
        // List<out E>
        // List<Guest> is subtype of List<User>
        fun dumpUsers(users: List<User>) = users.joinToString(",", prefix = "[", postfix = "]") { it.name }

        // Subtyping of TYPE PARAMETER
        // 1. current type
        val users: List<User> = listOf(User("a"), User("b"))
        assertEquals("[a,b]", dumpUsers(users))

        // 2. subtype
        val guests: List<Guest> = listOf(Guest(), Guest())
        assertEquals("[guest,guest]", dumpUsers(guests))

        // 3. supertype
        val anys = users as List<Any>
        // compile error, type mismatch
        // assertEquals("[a,b]", allUsers(anys))

        // 4. star projection
        val stars = users as List<*>
        // compile error, type mismatch
        // assertEquals("[a,b]", allUsers(stars))

        // Subtyping of TYPE PARAMETER, combined with SubType of GENERIC CLASS
        // 1. current type
        val arrayListUser: ArrayList<User> = arrayListOf(User("a"), User("b"))
        assertEquals("[a,b]", dumpUsers(arrayListUser))

        // 2. subtype
        val arrayListGuest: ArrayList<Guest> = arrayListOf(Guest(), Guest())
        assertEquals("[guest,guest]", dumpUsers(arrayListGuest))

        // 3. supertype
        val arrayListAny = arrayListUser as ArrayList<Any>
        // compile error, type mismatch
        // assertEquals("[a,b]", allUsers(arrayListAny))

        // 4. star projection
        val arrayListStar = arrayListUser as ArrayList<*>
        // compile error, type mismatch
        // assertEquals("[guest,guest]", allUsers(arrayListStar))
    }

    @Test
    fun contravariant() {
        open class User(val age: Int)
        class Guest : User(-1)

        // Comparator<in T>
        // Comparator<User> is subtype of Comparator<Guest>
        val userCompa = Comparator<User> { a, b ->
            a.age - b.age
        }
        assertTrue(userCompa.compare(User(17), User(27)) < 0)

        val guestCompa = Comparator<Guest> { a, b ->
            a.age - b.age
        }
        assertTrue(guestCompa.compare(Guest(), Guest()) == 0)
        assertTrue(userCompa.compare(Guest(), Guest()) == 0)
        // assertTrue(guestCompa.compare(User(17), User(27)) < 0) // compile error, type mismatch

        val anyCompa = Comparator<Any> { a, b ->
            0
        }
        assertTrue(anyCompa.compare(Guest(), Guest()) == 0)
        assertTrue(anyCompa.compare(User(17), User(27)) == 0)
        assertTrue(anyCompa.compare(User(17), Guest()) == 0)
    }

    @Test
    fun composition() {
        open class User(val name: String)
        class Guest : User("guest")

        fun <T : User> copy(source: List<out T>, dest: ArrayList<in T>): ArrayList<in T> {
            source.forEach {
                dest.add(it)
            }
            return dest
        }

        // source is out
        copy(listOf<User>(), arrayListOf<User>())
        copy(listOf<Guest>(), arrayListOf<User>())
        // copy(listOf<Any>(), arrayListOf<User>()) // compile error

        // dest is in
        copy(listOf<User>(), arrayListOf<Any>())
        // copy(listOf<User>(), arrayListOf<Guest>()) // compile error

        // return is in project, star project, or variance with star project
        val copy1: ArrayList<in User> = copy(listOf<User>(), arrayListOf<User>())
        val copy2: ArrayList<in User> = copy(listOf<User>(), arrayListOf<Any>())

        val copy3: ArrayList<*> = copy(listOf<User>(), arrayListOf<User>())
        val copy4: ArrayList<*> = copy(listOf<User>(), arrayListOf<Any>())

        // val copy: List<in User> = copy(listOf<User>(), arrayListOf<User>()) // in project with variance is conflicting
        val copy5: List<*> = copy(listOf<User>(), arrayListOf<User>())
        val copy6: List<*> = copy(listOf<User>(), arrayListOf<Any>())
    }
}

inline fun <reified T : Any> isGivenTypeReified(instance: Any): Boolean {
    return instance is T &&                       // 'is T' is ok when reified
            instance.javaClass.kotlin == T::class // 'T::class' is ok when reified
}

inline fun <reified T> reifiedInsteadOfClassLiteral(): String? {
    println("Loading class : ${T::class.simpleName}")
    return T::class.simpleName
}