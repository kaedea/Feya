@file:Suppress("unused", "UNUSED_VARIABLE", "UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")

package com.kaedea.kotlin

import com.kaedea.kotlin.utils.Values
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.*

/**
 * # The Kotlin's Generic
 *
 * ## Keywords
 *
 * - type parameter
 * - type argument
 * - subtyping complex
 * - variance annotations
 * - projections
 *
 * ## Tweaking of Kotlin generic features:
 *
 * - is-check & casting
 * - reified
 * - restrict of type parameter
 * - invariance & variance
 * - type projecting
 * - overrides of generic
 *
 * ## Tests Structure
 *
 * - [Basic Usage][KtGenericBasicTest]
 * - [Generic Class Tweak][KtGenericClassTest]
 * - [Generic Function Tweak][KtGenericFuncTest]
 * - [Subtyping Tweak][KtGenericSubtypingTest]
 * - [Principles of Generic Tweak][KtGenericPrincipleTest]
 * - [Overrides of Generic Tweak][KtGenericOverrideTest]
 *
 * ## Runtime apis about Kotlin's generic:
 *
 * - [kotlin.reflect.KVariance]
 * - [kotlin.reflect.KTypeParameter]
 * - [kotlin.reflect.KTypeProjection]
 *
 * @author Kaede
 * @since  2018/8/14
 */


@RunWith(JUnit4::class)
class KtGenericBasicTest {

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
     * # 'is-check' of Generic
     *
     * ## Raw check : is-check of type parameter
     * All raw check are meaningless because cast of ClassName<T> is regarded as ClassName<*>. Thus
     * `ClassName<*> is ClassName<String>` or `ClassName<*> is ClassName<Int>` is always true.
     *
     * ## Class check : is-check of generic class itself
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
     * # 'casting' of Generic
     *
     * ## Raw cast : casting of type parameter
     * All raw cast are meaningless because cast of ClassName<T> is regarded as ClassName<*>. Every
     * raw cast should be wrapped with protecting codes.
     *
     * ## Class cast : casting of generic class itself
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
    fun typeParameterLiteral() {
        // What do I mean by writing 'type parameter literal' here?
        todo {}
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
        fun fetchUsers(users: Array<User>) = users.joinToString(",", prefix = "[", postfix = "]") { it.name }

        val users: Array<User> = arrayOf(User("a"), User("b"))
        assertEquals("[a,b]", fetchUsers(users))

        val guests: Array<Guest> = arrayOf(Guest(), Guest())
        // compile error, type mismatch
        // assertEquals("[guest,guest]", fetchUsers(guests))

        val castToAnys = guests as Array<Any>
        // compile error, type mismatch
        // assertEquals("[guest,guest]", fetchUsers(castToAnys))

        val castToUsers = guests as Array<User>
        assertEquals("[guest,guest]", fetchUsers(castToUsers))
    }

    /**
     * `User<T>` & `Guest<T> : User<T>`
     * Subtyping:
     * 1. Subtyping of T within User
     * 2. Subtyping of T within Guest (Subtyping of inheritances)
     * 3. Subtyping of projection (how does User effect Guest is projection)
     *
     * Use Cases:
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
        fun fetchUsers(users: List<User>) = users.joinToString(",", prefix = "[", postfix = "]") { it.name }

        /** Subtyping of TYPE PARAMETER **/
        // 1. current type
        val users: List<User> = listOf(User("a"), User("b"))
        assertEquals("[a,b]", fetchUsers(users))

        // 2. subtype
        val guests: List<Guest> = listOf(Guest(), Guest())
        assertEquals("[guest,guest]", fetchUsers(guests))

        // 3. supertype
        val anys = users as List<Any>
        // compile error, type mismatch
        // assertEquals("[a,b]", fetchUsers(anys))

        // 4. star projection
        val stars = users as List<*>
        // compile error, type mismatch
        // assertEquals("[a,b]", fetchUsers(stars))

        /** Subtyping of TYPE PARAMETER, combined with SubType of GENERIC CLASS **/
        // 1. current type
        val arrayListUser: ArrayList<User> = arrayListOf(User("a"), User("b"))
        assertEquals("[a,b]", fetchUsers(arrayListUser))

        // 2. subtype
        val arrayListGuest: ArrayList<Guest> = arrayListOf(Guest(), Guest())
        assertEquals("[guest,guest]", fetchUsers(arrayListGuest))

        // 3. supertype
        val arrayListAny = arrayListUser as ArrayList<Any>
        // compile error, type mismatch
        // assertEquals("[a,b]", fetchUsers(arrayListAny))

        // 4. star projection
        val arrayListStar = arrayListUser as ArrayList<*>
        // compile error, type mismatch
        // assertEquals("[guest,guest]", fetchUsers(arrayListStar))
    }

    @Test
    fun contravariant() {
        open class User(val age: Int)
        class Guest : User(-1)

        // Comparator<in T>
        // Comparator<User> is subtype of Comparator<Guest>
        val userCompa = Comparator<User> { a, b -> a.age - b.age }
        val guestCompa = Comparator<Guest> { a, b -> a.age - b.age }
        val anyCompa = Comparator<Any> { a, b -> 0 }

        assertTrue(userCompa.compare(User(17), User(27)) < 0)
        assertTrue(guestCompa.compare(Guest(), Guest()) == 0)
        assertTrue(userCompa.compare(Guest(), Guest()) == 0)
        // compile error, type mismatch
        // assertTrue(guestCompa.compare(User(17), User(27)) < 0)

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
        // compile error
        // copy(listOf<Any>(), arrayListOf<User>())

        // dest is in
        copy(listOf<User>(), arrayListOf<Any>())
        // compile error
        // copy(listOf<User>(), arrayListOf<Guest>())

        // return with in/out/star projection
        // compile error, ArrayList<in User> to ArrayList<User>
        // val copy1: ArrayList<User> = copy(listOf<User>(), arrayListOf<Any>())
        // compile error,  ArrayList<in User> to ArrayList<out User>
        // val copy2: ArrayList<out User> = copy(listOf<User>(), arrayListOf<User>())
        val copy3: ArrayList<in User> = copy(listOf<User>(), arrayListOf<User>())
        val copy4: ArrayList<*> = copy(listOf<User>(), arrayListOf<Any>())

        // in Kotlin, List is out while ArrayList is invariant
        // out/in projection with List<T> is conflicting
        // val copy5: List<out User> = copy(listOf<User>(), arrayListOf<User>())
        // val copy6: List<in User> = copy(listOf<User>(), arrayListOf<User>())
        val copy7: List<*> = copy(listOf<User>(), arrayListOf<User>())
    }

    inline fun <reified T : Any> isGivenTypeReified(instance: Any): Boolean {
        return instance is T &&                       // 'is T' is ok when reified
                instance.javaClass.kotlin == T::class // 'T::class' is ok when reified
    }

    inline fun <reified T> reifiedInsteadOfClassLiteral(): String? {
        println("Loading class : ${T::class.simpleName}")
        return T::class.simpleName
    }
}

/**
 * # Class With Type Parameter
 *
 * Variance is subtyping complex of class with type parameter (dual typing),
 * containing:
 *
 * - invariant     : Array<Child> & Array<Father> is not subtype of each other
 * - covariant     : List<Child> is subtype of List<Father>
 * - contravariant : Comparator<Father> is subtype Comparator<Child>
 * - non-variant   : this<Father> & this<Child>
 *
 * Note that subtyping complex only exists with type parameter and the subtyping of generic class itself is
 * just the same as normal class. ArrayList<*> is subtype of List, while ArrayList<Father> is nothing
 * with List<Child> (subtype conflicting ?).
 *
 * ## Variance Annotations
 * Declaring generic class as invariant/variant, also called "Declaration-site Variance".
 * @see [invariant], [covariant], [contravariant]
 *
 * ## Type Projections
 * Make an invariant type argument be variant is use, also called "Use-site Variance".
 * @see [projections], [starProjection]
 *
 */
@RunWith(JUnit4::class)
class KtGenericClassTest {

    @Test
    fun invariant() {
        open class User(val name: String)
        class Guest : User("guest")

        open class Container<T>(var value: Any? = null) {
            fun get(): T = value as T
            fun set(t: T) {
                value = t
            }
        }

        /** 1. Constructor : invariant **/
        val userContainer1: Container<User> = Container<User>(User("Kaede"))
        val guestContainer1: Container<Guest> = Container<Guest>(Guest())
        val intContainer: Container<Int> = Container<Int>(Integer.valueOf(2233))
        // error
        // val userContainer2: Container<User> = Container<Guest>(User("Kaede"))
        // val guestContainer2: Container<Guest> = Container<User>(User("Kaede"))
        // val anyContainer: Container<Any?> = Container<Int>(Integer.valueOf(2233))


        /** 2.1 Member param : non **/
        Container<User>().set(User("Kaedea"))
        Container<User>().set(Guest())
        Container<Guest>().set(Guest())
        Container<Any?>().set(Integer.valueOf(2233))
        // error
        // Container<Guest>().set(User("Kaede"))

        /** 2.2 Member return : non  **/
        val user1: User = Container<User>(User("Kaede")).get()
        val user2: User = Container<Guest>(Guest()).get()
        val guest1: Guest = Container<Guest>(Guest()).get()
        val any: Any? = Container<Int>(Integer.valueOf(2233)).get()
        // error
        // val guest2: Guest = Container<User>(User("Kaede")).get()


        fun <T> call(container: Container<T>) {
            println("I'm ${container.get()} from ${container}")
        }

        /** 3.1 Func param : invariant **/
        call<User>(Container<User>(User("Kaede")))
        call<Guest>(Container<Guest>(Guest()))
        // error
        // call<User>(Container<Guest>(Guest()))
        // call<Guest>(Container<User>(User("Kaede")))
        // call<Any>(Container<User>(User("Kaede")))


        fun <T> fetch(any: Any): Container<T> = with(any) {
            println("Create container for ${any.javaClass.simpleName}")
            return Container<T>(any)
        }

        /** 3.2 Func return : invariant **/
        val userContainerFetch1: Container<User> = fetch<User>(User("Kaede"))
        val guestContainerFetch1: Container<Guest> = fetch<Guest>(Guest())
        val anyFetch: Any = fetch<User>(User("Kaede"))
        // error
        // val userContainerFetch2: Container<User> = fetch<Guest>(Guest())
        // val guestContainerFetch2: Container<Guest> = fetch<User>(User("Kaede"))
        // val anyContainerFetch: Container<Any> = fetch<User>(User("Kaede"))


        class ExContainer<T>(value: Any?) : Container<T>(value)

        /** 4.1 Ext constructor : invariant **/
        val userExContainer1: ExContainer<User> = ExContainer<User>(User("Kaede"))
        val userContainerCast1: Container<User> = ExContainer<User>(User("Kaede"))
        val guestExContainer1: ExContainer<Guest> = ExContainer<Guest>(Guest())
        val guestContainerCast2: Container<Guest> = ExContainer<Guest>(Guest())
        // error
        // val userExContainer2: ExContainer<User> = ExContainer<Guest>(Guest())
        // val userContainerCast2: Container<User> = ExContainer<Guest>(Guest())

        /** 4.2 Ext func param : invariant **/
        call<User>(ExContainer<User>(User("Kaede")))
        call<Guest>(ExContainer<Guest>(Guest()))
        // error
        // call<Guest>(ExContainer<User>(User("Kaede")))
        // call<User>(ExContainer<Guest>(Guest()))

        /** 4.2 Ext func param : type mismatch **/
        // val userExContainerFetch1: ExContainer<User> = fetch<User>(User("Kaede"))
        // val guestExContainerFetch1: ExContainer<Guest> = fetch<Guest>(Guest())
    }

    @Test
    fun covariant() {
        open class User(val name: String)
        class Guest : User("guest")

        open class Container<out T>(var value: Any? = null) {
            fun get(): T = value as T
            // error, out projection can not occur in 'in' position
            // fun set(t: T) { value = t }
        }

        /** 1. Constructor : covariant **/
        val userContainer1: Container<User> = Container<User>(User("Kaede"))
        val guestContainer1: Container<Guest> = Container<Guest>(Guest())
        val intContainer: Container<Int> = Container<Int>(Integer.valueOf(2233))
        val userContainer2: Container<User> = Container<Guest>(User("Kaede"))
        val anyContainer: Container<Any?> = Container<Int>(Integer.valueOf(2233))
        // error
        // val guestContainer2: Container<Guest> = Container<User>(User("Kaede"))

        /** 2.1 Member param : not support **/
        /** 2.2 Member return : non  **/
        val user1: User = Container<User>(User("Kaede")).get()
        val user2: User = Container<Guest>(Guest()).get()
        val guest1: Guest = Container<Guest>(Guest()).get()
        val any: Any? = Container<Int>(Integer.valueOf(2233)).get()
        // error
        // val guest2: Guest = Container<User>(User("Kaede")).get()


        fun <T> call(container: Container<out T>) {
            println("I'm ${container.get()} from ${container}")
        }

        /** 3.1 Func param : covariant **/
        call<User>(Container<User>(User("Kaede")))
        call<Guest>(Container<Guest>(Guest()))
        call<User>(Container<Guest>(Guest()))
        call<Any>(Container<User>(User("Kaede")))
        // error
        // call<Guest>(Container<User>(User("Kaede")))


        fun <T> fetch(any: Any): Container<T> = with(any) {
            println("Create container for ${any.javaClass.simpleName}")
            return Container<T>(any)
        }

        /** 3.2 Func return : covariant **/
        val userContainerFetch1: Container<User> = fetch<User>(User("Kaede"))
        val guestContainerFetch1: Container<Guest> = fetch<Guest>(Guest())
        val userContainerFetch2: Container<User> = fetch<Guest>(Guest())
        val anyContainerFetch: Container<Any> = fetch<User>(User("Kaede"))
        // error
        // val guestContainerFetch2: Container<Guest> = fetch<User>(User("Kaede"))
        val anyFetch: Any = fetch<User>(User("Kaede"))


        class ExContainer<out T>(value: Any?) : Container<T>(value)

        /** 4.1 Ext constructor : covariant **/
        val userExContainer1: ExContainer<User> = ExContainer<User>(User("Kaede"))
        val userContainerCast1: Container<User> = ExContainer<User>(User("Kaede"))
        val guestExContainer1: ExContainer<Guest> = ExContainer<Guest>(Guest())
        val guestContainerCast1: Container<Guest> = ExContainer<Guest>(Guest())
        val userExContainer2: ExContainer<User> = ExContainer<Guest>(Guest())
        val userContainerCast2: Container<User> = ExContainer<Guest>(Guest())
        val anyExContainer: ExContainer<Any?> = ExContainer<User>(User("Kaede"))
        val anyExContainerCast: Container<Any?> = ExContainer<User>(User("Kaede"))
        // error
        // val guestContainerCast2: Container<Guest> = ExContainer<User>(User("Kaede"))
        // casting error
        // val userExContainerCast1: ExContainer<User> = Container<User>(User("Kaede"))
        // val userExContainerCast2: ExContainer<User> = Container<Guest>(Guest)
        // val guestExContainerCast3: ExContainer<Guest> = Container<User>(User("Kaede"))
        // val guestExContainerCast4: ExContainer<Guest> = Container<Guest>(Guest)

        /** 4.2 Ext func param : covariant **/
        call<User>(ExContainer<User>(User("Kaede")))
        call<Guest>(ExContainer<Guest>(Guest()))
        call<User>(ExContainer<Guest>(Guest()))
        // error
        // call<Guest>(ExContainer<User>(User("Kaede")))

        /** 4.2 Ext func return : type mismatch **/
        // val userExContainerFetch1: ExContainer<User> = fetch<User>(User("Kaede"))
        // val userExContainerFetch2: ExContainer<User> = fetch<Guest>(Guest())
        // val guestExContainerFetch1: ExContainer<Guest> = fetch<Guest>(Guest())
        // val guestExContainerFetch2: ExContainer<Guest> = fetch<User>(User("Kaede"))
    }

    @Test
    fun contravariant() {
        open class User(val name: String)
        class Guest : User("guest")

        open class Container<in T>(var value: Any? = null) {
            // error, in projection can not occur in 'out' position
            // fun get(): T = value as T
            fun set(t: T) {
                value = t
            }
        }

        /** 1. Constructor : contravariant **/
        val userContainer1: Container<User> = Container<User>(User("Kaede"))
        val guestContainer1: Container<Guest> = Container<Guest>(Guest())
        val intContainer: Container<Int> = Container<Int>(Integer.valueOf(2233))
        val guestContainer2: Container<Guest> = Container<User>(User("Kaede"))
        val guestContainer3: Container<Guest> = Container<Any?>()
        // error
        // val userContainer2: Container<User> = Container<Guest>(User("Kaede"))
        // val anyContainer: Container<Any?> = Container<Int>(Integer.valueOf(2233))

        /** 2.1 Member param : non **/
        /** 2.2 Member return : not support **/
        Container<User>().set(User("Kaedea"))
        Container<User>().set(Guest())
        Container<Guest>().set(Guest())
        Container<Any?>().set(Integer.valueOf(2233))
        // error
        // Container<Guest>().set(User("Kaede"))


        fun <T> call(container: Container<in T>) {
            println("I'm ${container}")
        }

        /** 3.1 Func param : contravariant **/
        call<User>(Container<User>(User("Kaede")))
        call<Guest>(Container<Guest>(Guest()))
        call<Guest>(Container<User>(User("Kaede")))
        call<Guest>(Container<Any>())
        // error
        // call<User>(Container<Guest>(Guest()))
        // call<Any>(Container<User>(User("Kaede")))


        fun <T> fetch(any: Any): Container<T> = with(any) {
            println("Create container for ${any.javaClass.simpleName}")
            return Container<T>(any)
        }

        /** 3.2 Func return : contravariant **/
        val userContainerFetch1: Container<User> = fetch<User>(User("Kaede"))
        val guestContainerFetch1: Container<Guest> = fetch<Guest>(Guest())
        val guestContainerFetch2: Container<Guest> = fetch<User>(User("Kaede"))
        // error
        // val userContainerFetch2: Container<User> = fetch<Guest>(Guest())
        // val anyContainerFetch: Container<Any> = fetch<User>(User("Kaede"))
        val anyFetch: Any = fetch<User>(User("Kaede"))


        class ExContainer<in T>(value: Any? = null) : Container<T>(value)

        /** 4.1 Ext constructor : contravariant **/
        val userExContainer1: ExContainer<User> = ExContainer<User>(User("Kaede"))
        val userContainerCast1: Container<User> = ExContainer<User>(User("Kaede"))
        val guestExContainer1: ExContainer<Guest> = ExContainer<Guest>(Guest())
        val guestContainerCast1: Container<Guest> = ExContainer<Guest>(Guest())
        val guestContainerCast2: Container<Guest> = ExContainer<User>(User("Kaede"))
        // error
        // val userExContainer2: ExContainer<User> = ExContainer<Guest>(Guest())
        // val anyExContainer: ExContainer<Any?> = ExContainer<User>(User("Kaede"))
        // val userContainerCast2: Container<User> = ExContainer<Guest>(Guest())
        // val anyExContainerCast: Container<Any?> = ExContainer<User>(User("Kaede"))
        // casting error
        // val userExContainerCast1: ExContainer<User> = Container<User>(User("Kaede"))
        // val userExContainerCast2: ExContainer<User> = Container<Guest>(Guest)
        // val guestExContainerCast3: ExContainer<Guest> = Container<User>(User("Kaede"))
        // val guestExContainerCast4: ExContainer<Guest> = Container<Guest>(Guest)

        /** 4.2 Ext func param : contravariant **/
        call<User>(ExContainer<User>(User("Kaede")))
        call<Guest>(ExContainer<Guest>(Guest()))
        call<Guest>(ExContainer<User>(User("Kaede")))
        call<Guest>(ExContainer<Any?>())
        // error
        // call<User>(ExContainer<Guest>(Guest()))

        /** 4.2 Ext func return : type mismatch **/
        // casting error
        // val userExContainerFetch1: ExContainer<User> = fetch<User>(User("Kaede"))
        // val userExContainerFetch2: ExContainer<User> = fetch<Guest>(Guest())
        // val guestExContainerFetch1: ExContainer<Guest> = fetch<Guest>(Guest())
        // val guestExContainerFetch2: ExContainer<Guest> = fetch<User>(User("Kaede"))
    }

    @Test
    fun projections() {
        open class User(val name: String)
        class Guest : User("guest")

        open class Container<T>(var value: Any? = null) {
            open fun get(): T = value as T
            open fun set(t: T) {
                value = t
            }
        }

        /**
         * Container is invariant by default
         * @see [invariant]
         */
        fun <T> call(container: Container<T>) {
            println("I'm ${container.get()} from ${container}")
        }

        call<User>(Container<User>(User("Kaede")))
        call<Guest>(Container<Guest>(Guest()))
        // error
        // call<User>(Container<Guest>(Guest()))
        // call<Guest>(Container<User>(User("Kaede")))
        // call<Any>(Container<User>(User("Kaede")))

        /**
         * The following function use __type projection__ to make it become covariant
         * @see [covariant]
         */
        fun <T> callOutProject(container: Container<out T>) {
            println("I'm ${container.get()} from ${container}")
        }

        callOutProject<User>(Container<User>(User("Kaede")))
        callOutProject<Guest>(Container<Guest>(Guest()))
        callOutProject<User>(Container<Guest>(Guest()))
        callOutProject<Any>(Container<User>(User("Kaede")))
        callOutProject<Any?>(Container<Any?>())
        // error
        // callOutProject<Guest>(Container<User>(User("Kaede")))

        // TODO
        // out projection with upper bound of Container<T>
        fun callOutProjectBound(container: Container<out User>) {
            println("I'm ${container.get()} from ${container}")
        }

        callOutProjectBound(Container<User>())
        callOutProjectBound(Container<Guest>())
        // error
        // callOutProjectBound(Container<Any>())


        /**
         * The following function use __type projection__ to make it become contravariant
         * @see [contravariant]
         */
        fun <T> callInProject(container: Container<in T>) {
            println("I'm ${container}")
        }

        callInProject<User>(Container<User>(User("Kaede")))
        callInProject<Guest>(Container<Guest>(Guest()))
        callInProject<Guest>(Container<User>(User("Kaede")))
        callInProject<Guest>(Container<Any>())
        // error
        // callInProject<User>(Container<Guest>(Guest()))
        // callInProject<Any>(Container<User>(User("Kaede")))

        // TODO
        // in projection with upper bound of Container<T>
        fun callInProjectBound(container: Container<in User>) {
            println("I'm ${container}")
        }

        callInProjectBound(Container<User>(User("Kaede")))
        callInProjectBound(Container<Any>())
        // error
        // callInProjectBound(Container<Guest>(Guest()))

        // Can not use type projections in inheritances
        // Error
        // class OutContainer<out T>(value: Any?) : Container<T>(value)
        // class InContainer<in T>(value: Any?) : Container<T>(value)
    }

    /**
     * 1. Star projection is super type of each generic type.
     * 2. Star projection is a special out projection (most upper of out projection).
     * 3. Star projection always work with reading, while writing with star projection is nonsense.
     * 4. Star projection combined with upper bounding can have more exact type referring.
     * 5. Star projection is very much like Java's raw type, but safe.
     */
    @Test
    fun starProjection() {
        open class User(val name: String)
        class Guest : User("guest")

        /**
         * 1. Subtyping
         * Star projection is super type of each concrete instantiation of generic type. It offers a safe
         * way to reference the generic type when you know nothing about the type argument.
         */
        open class Container<T>(var value: Any? = null) {
            open fun get(): T = value as T
            open fun set(t: T) {
                value = t
            }
        }

        open class ContainerOut<out T>(var value: Any? = null) {
            fun get(): T = value as T
        }

        open class ContainerIn<in T>(var value: Any? = null) {
            fun set(t: T) {
                value = t
            }
        }
        // Container<*> is now super type of Container<T>
        val userContainer1: Container<*> = Container<User>(User("Kaede"))
        val guestContainer1: Container<*> = Container<Guest>(Guest())
        val intContainer: Container<*> = Container<Int>(Integer.valueOf(2233))
        val userContainer2: Container<*> = Container<Guest>(User("Kaede"))
        val guestContainer2: Container<*> = Container<User>(User("Kaede"))
        val anyContainer: Container<*> = Container<Int>(Integer.valueOf(2233))
        // Container<*> is also super type of ContainerOut<T>
        val userContainerOut1: ContainerOut<*> = ContainerOut<User>(User("Kaede"))
        val guestContainerOut1: ContainerOut<*> = ContainerOut<Guest>(Guest())
        val intContainerOut: ContainerOut<*> = ContainerOut<Int>(Integer.valueOf(2233))
        val userContainerOut2: ContainerOut<*> = ContainerOut<Guest>(User("Kaede"))
        val anyContainerOut: ContainerOut<*> = ContainerOut<Int>(Integer.valueOf(2233))
        val guestContainerOut2: ContainerOut<*> = ContainerOut<User>(User("Kaede"))
        // Container<*> is also super type of ContainerIn<T>
        val userContainerIn1: ContainerIn<*> = ContainerIn<User>(User("Kaede"))
        val guestContainerIn1: ContainerIn<*> = ContainerIn<Guest>(Guest())
        val intContainerIn: ContainerIn<*> = ContainerIn<Int>(Integer.valueOf(2233))
        val guestContainerIn2: ContainerIn<*> = ContainerIn<User>(User("Kaede"))
        val guestContainerIn3: ContainerIn<*> = ContainerIn<Any?>()
        val userContainerIn2: ContainerIn<*> = ContainerIn<Guest>(User("Kaede"))
        val anyContainerIn: ContainerIn<*> = ContainerIn<Int>(Integer.valueOf(2233))

        /**
         * 2. Star of invariant
         * Foo<*> is equivalent to Foo<out TUpper> for reading values and to Foo<in Nothing> for writing values.
         */
        val starOfInvariant: Container<*> = Container<User>(User("Kaede"))
        val any1: Any? = starOfInvariant.get()

        open class Foo<T : User>(var value: Any? = null) {
            open fun get(): T = value as T
            open fun set(t: T) {
                value = t
            }
        }

        val starOfInvariantBound: Foo<*> = Foo<User>(User("Kaede"))
        val get1: User = starOfInvariantBound.get()
        // error
        // Out-projected type 'Foo<*>' prohibits the use of 'fun set(t: T): Unit'
        // starOfInvariantBound.set(User("Akatsuki"))

        /**
         * 3. Star of covariant
         * Foo<*> is equivalent to Foo<out Any?>
         * Foo<*> is equivalent to Foo<out TUpper>. It means that when the T is unknown you can safely read values of TUpper from Foo<*>.
         */
        val starOfCovariant: ContainerOut<*> = ContainerOut<User>(User("Kaede"))
        val any2: Any? = starOfCovariant.get()

        open class FooOut<out T : User>(var value: Any? = null) {
            fun get(): T = value as T
        }

        val starOfCovariantBound: FooOut<*> = FooOut<User>(User("Kaede"))
        val get2: User = starOfCovariantBound.get()

        /**
         * 4. Star of contravariant
         * Foo<*> is equivalent to Foo<in Nothing>. It means there is nothing you can write to Foo<*> in a safe way when T is unknown.
         */
        open class FooIn<in T : User>(var value: Any? = null) {
            fun set(t: T) {
                value = t
            }
        }

        val starOfContravariantBound: FooIn<*> = FooIn<User>(User("Kaede"))
        // error
        // Out-projected type 'FooIn<*>' prohibits the use of 'fun set(t: T): Unit'
        // starOfContravariantBound.set(User("Akatsuki"))
    }
}

/**
 * Function With Type Parameters
 * Function has no inheritance(?), thus function combined with type parameter has no subtyping complex.
 *
 * - Function with type parameter as parameters
 * - Function with type parameter as return
 * - Function with type parameter, combined with generic class
 * - Function without type parameter, combined with generic class
 */
@RunWith(JUnit4::class)
class KtGenericFuncTest {

    open class User(val name: String)
    class Guest : User("guest")

    @Test
    fun funcParam() {
        fun <T> call(t: T) {
            println("I'm ${t.toString()}")
        }

        call<User>(User("Kaede"))
        call<User>(Guest())
        call<Guest>(Guest())
        call<Int>(Integer.valueOf(2233))
        call<Any?>(null)
        // error
        // call<Guest>(User("Kaede"))
        // call<Int>(null)

        fun <T : User> callWithUpperBound(t: T) {
            println("I'm ${t.toString()}, upper bound = User")
        }

        callWithUpperBound<User>(User("Kaede"))
        callWithUpperBound<Guest>(Guest())
        callWithUpperBound<User>(Guest())
        // error
        // callWithUpperBound<Int>(Integer.valueOf(2233))
    }

    @Test
    fun funcReturn() {
        fun <T> fetch(any: Any): T = with(any) {
            println("Cast from ${any.javaClass.simpleName}")
            return any as T
        }

        val user1: User = fetch<User>(User("Kaede"))
        val user2: User = fetch<Guest>(Guest())
        val guest1: Guest = fetch<Guest>(Guest())
        val any: Any? = fetch<Int>(Integer.valueOf(2233))
        // error
        // val guest2: Guest = get<User>(Guest())
        // val int: Int = get<Any?>(Integer.valueOf(2233))


        fun <T : User> getWithUpperBound(any: Any): T = any as T

        val userBound1: User = getWithUpperBound<User>(User("Kaede"))
        val userBound2: User = getWithUpperBound<Guest>(Guest())
        val guestBound1: Guest = getWithUpperBound<Guest>(Guest())
        // error
        // val intBound: Int = getWithUpperBound<Int>(Integer.valueOf(2233))
    }

    /**
     * For function without type parameter, check the following.
     *
     * @see [KtGenericBasicTest.invariant]
     * @see [KtGenericBasicTest.covariant]
     */
    @Test
    fun funcWithGenericClass() {
        fun <T : User> fetchUsers(users: Array<T>) = users.joinToString(",", prefix = "[", postfix = "]") { it.name }

        val users: Array<User> = arrayOf(User("a"), User("b"))
        assertEquals("[a,b]", fetchUsers<User>(users))

        val guests: Array<Guest> = arrayOf(Guest(), Guest())
        assertEquals("[guest,guest]", fetchUsers<Guest>(guests)) // ok
        // assertEquals("[guest,guest]", fetchUsers<User>(guests))        // compile error, type mismatch
    }
}

@RunWith(JUnit4::class)
class KtGenericSubtypingTest {

    open class User()
    open class Guest : User()

    open class Container<T>(var value: Any? = null) {
        open fun get(): T = value as T
        open fun set(t: T) {
            value = t
        }
    }

    /**
     * For Invariant<T>, Invariant<Child> is not subtype of Invariant<Father>, vice versa.
     * For Invariant<T : Bound>, it is the same.
     * When Type Parameter is not reified, generic classes with different type parameters are not
     * subtype of each other.
     */
    @Test
    fun upperBound() {
        /** assign **/
        val fatherContainer1: Container<User> = Container<User>(User())
        // error
        // val fatherContainer2: Container<User> = Container<Guest>(Guest())
        // val childContainer1: Container<Guest> = Container<User>(User())

        /** return type **/
        open abstract class GenericClass<T : User> {
            abstract fun foo(): Container<T>
        }

        // father's return type is Container<T : User>
        // child's  return type is Container<T : User>
        abstract class ExGenericClass1<T : User> : GenericClass<T>() {
            abstract override fun foo(): Container<T>
        }

        // father's return type is Container<T : Guest>
        // child's  return type is Container<T : Guest>
        abstract class ExGenericClass2<T : Guest> : GenericClass<T>() {
            abstract override fun foo(): Container<T>
        }

        // father's return type is Container<User>
        // child's  return type is Container<User>
        abstract class ExGenericClass3 : GenericClass<User>() {
            abstract override fun foo(): Container<User>
        }

        // father's return type is Container<Guest>
        // child's  return type is Container<Guest>
        abstract class ExGenericClass4 : GenericClass<Guest>() {
            abstract override fun foo(): Container<Guest>
        }

        // father's return type is Container<User>
        // child's  return type is Container<User>
        abstract class ExGenericClass5 : ExGenericClass3() {
            abstract override fun foo(): Container<User>
        }

        // father's return type is Container<Guest>
        // child's  return type is Container<Guest>
        abstract class ExGenericClass6 : ExGenericClass4() {
            abstract override fun foo(): Container<Guest>
        }
        // error
        //     Container<Guest> is not subtype of Container<T : User>
        //     father's return type is Container<T : User>
        //     child's  return type is Container<Guest>
        // abstract class ExGenericClass7<T : User>: GenericClass<T>() {
        //     abstract override fun foo() : Container<Guest>
        // }
        //     Container<Guest> is not subtype of Container<User>
        //     father's return type is Container<User>
        //     child's  return type is Container<Guest>
        // abstract class ExGenericClass8: GenericClass<User>() {
        //     abstract override fun foo(): Container<Guest>
        // }
        //     Container<Guest> is not subtype of Container<User>
        //     father's return type is Container<User>
        //     child's  return type is Container<Guest>
        // abstract class ExGenericClass9: ExGenericClass3() {
        //     abstract override fun foo(): Container<Guest>
        // }
        //     Container<User> is not subtype of Container<Guest>
        //     father's return type is Container<Guest>
        //     child's  return type is Container<User>
        // abstract class ExGenericClass10: ExGenericClass4() {
        //     abstract override fun foo(): Container<User>
        // }

        // error
        // Container<T2 : User> is not subtype of Container<T1 : User>, when Type Parameter is not reified.
        // abstract class ExGenericClass11<T1 : User, T2 : User>: GenericClass<T1>() {
        //     abstract override fun foo() : Container<T2>
        // }
    }

    /**
     * For Generic<out T>, B --> A
     * (B --> A : B is subtype of A)
     *
     * - Generic<B> --> Generic<A>
     * - Generic<T> --> Generic<out T>
     * - Generic<B> --> Generic<out B>
     * - Generic<B> --> Generic<out B> --> Generic<out A>
     * - Generic<B> --> Generic<A>     --> Generic<out A>
     * - Generic<A> !-> Generic<out B>
     * - Generic<out B> !-> Generic<A>, Invariant
     */
    @Test
    fun outProjection() {
        open abstract class GenericClass<T> {
            abstract fun foo(): Container<out T>
        }

        // Container<T> --> Container<out T>
        abstract class ExGenericClass1<T> : GenericClass<T>() {
            abstract override fun foo(): Container<T>
        }

        // Container<out T> --> Container<out T>
        abstract class ExGenericClass2<T> : GenericClass<T>() {
            abstract override fun foo(): Container<out T>
        }

        // Container<out User> --> Container<out User>
        abstract class ExGenericClass3 : GenericClass<User>() {
            abstract override fun foo(): Container<out User>
        }

        // Container<out Guest> --> Container<out User>
        abstract class ExGenericClass4 : GenericClass<User>() {
            abstract override fun foo(): Container<out Guest>
        }

        // Container<out Guest> --> Container<out User>
        abstract class ExGenericClass5 : ExGenericClass3() {
            abstract override fun foo(): Container<out Guest>
        }

        // Container<User> --> Container<out User>
        abstract class ExGenericClass6 : ExGenericClass3() {
            abstract override fun foo(): Container<User>
        }
        // error
        // Container<User> !-> Container<out Guest>
        // abstract class ExGenericClass7 : ExGenericClass4() {
        //     abstract override fun foo() : Container<User>
        // }
        // Container<out Guest> !-> Container<user>, invariant
        // abstract class ExGenericClass8 : ExGenericClass6() {
        //     abstract override fun foo() : Container<out Guest>
        // }


        fun foo(): Array<Guest> {
            return arrayOf<Guest>()
        }
        // Array<Guest> --> Array<out Guest> --> Array<out User>
        val get1: Array<out Guest> = foo()
        val get2: Array<out User> = get1
    }

    /**
     * For Generic<in T>, B --> A
     * (B --> A : B is subtype of A)
     *
     * - Generic<A> --> Generic<B>
     * - Generic<T> --> Generic<in T>
     * - Generic<A> --> Generic<in A>
     * - Generic<A> --> Generic<in A> --> Generic<in B>
     * - Generic<A> --> Generic<B>    --> Generic<in B>
     * - Generic<B> !-> Generic<in A>
     */
    @Test
    fun inProjection() {
        open abstract class GenericClass<T> {
            abstract fun foo(): Container<in T>
        }

        // Container<T> --> Container<in T>
        abstract class ExGenericClass1<T> : GenericClass<T>() {
            abstract override fun foo(): Container<T>
        }

        // Container<in T> --> Container<in T>
        abstract class ExGenericClass2<T> : GenericClass<T>() {
            abstract override fun foo(): Container<in T>
        }

        // Container<in Guest> --> Container<in Guest>
        abstract class ExGenericClass3 : GenericClass<Guest>() {
            abstract override fun foo(): Container<in Guest>
        }

        // Container<in User> --> Container<in Guest>
        abstract class ExGenericClass4 : GenericClass<Guest>() {
            abstract override fun foo(): Container<in User>
        }

        // Container<User> --> Container<in Guest>
        abstract class ExGenericClass5 : ExGenericClass3() {
            abstract override fun foo(): Container<User>
        }

        // Container<Guest> --> Container<in Guest>
        abstract class ExGenericClass6 : ExGenericClass3() {
            abstract override fun foo(): Container<Guest>
        }
        // error
        // Container<Guest> !-> Container<in User>
        // abstract class ExGenericClass7 : ExGenericClass4() {
        //     abstract override fun foo() : Container<Guest>
        // }

        fun foo(): Array<User> {
            return arrayOf<User>()
        }
        // Array<User> --> Array<in User> --> Array<in Guest>
        val get1: Array<in User> = foo()
        val get2: Array<in Guest> = get1
    }

    /**
     * As for subtyping of function type:
     *
     * - Function's parameter respect method's parameter (can not be overridden)
     * - Function's return type respect method's return type, check [upperBound], [outProjection], [inProjection]
     */
    @Test
    fun functionType() {
        open abstract class GenericClass<T> {
            abstract fun foo(): (Container<T>) -> Container<T>
        }

        // father's return type is (Container<T>) -> Container<T>
        // child's  return type is (Container<T>) -> Container<T>
        abstract class ExGenericClass1<T : User> : GenericClass<T>() {
            abstract override fun foo(): (Container<T>) -> Container<T>
        }

        // (Container<User>) -> Container<User>
        // Container<User>) -> Container<User>
        abstract class ExGenericClass2 : GenericClass<User>() {
            abstract override fun foo(): (Container<User>) -> Container<User>
        }
        // error
        // (Container<User>) -> Container<User>
        // (Container<Guest>) -> Container<User>
        // abstract class ExGenericClass3: GenericClass<User>() {
        //     abstract override fun foo() : (Container<Guest>) -> Container<User>
        // }
        // (Container<Guest>) -> Container<Guest>
        // (Container<User>) -> Container<User>
        // abstract class ExGenericClass4: GenericClass<Guest>() {
        //     abstract override fun foo() : (Container<User>) -> Container<Guest>
        // }
        // (Container<User>) -> Container<User>
        // (Container<User>) -> Container<Guest>
        // abstract class ExGenericClass5: GenericClass<User>() {
        //     abstract override fun foo() : (Container<User>) -> Container<Guest>
        // }
        // (Container<Guest>) -> Container<Guest>
        // (Container<Guest>) -> Container<User>
        // abstract class ExGenericClass6: GenericClass<Guest>() {
        //     abstract override fun foo() : (Container<Guest>) -> Container<User>
        // }


        open abstract class GenericClassOut<T> {
            abstract fun foo(): (Container<out T>) -> Container<out T>
        }

        // (Container<out T>) -> Container<out T>
        // (Container<out T>) -> Container<T>
        abstract class ExGenericClassOut1<T : User> : GenericClassOut<T>() {
            abstract override fun foo(): (Container<out T>) -> Container<T>
        }

        // (Container<out T>) -> Container<out T>
        // (Container<out T>) -> Container<out T>
        abstract class ExGenericClassOut2<T : User> : GenericClassOut<T>() {
            abstract override fun foo(): (Container<out T>) -> Container<out T>
        }

        // (Container<out User>) -> Container<out User>
        // (Container<out User>) -> Container<User>
        abstract class ExGenericClassOut3 : GenericClassOut<User>() {
            abstract override fun foo(): (Container<out User>) -> Container<User>
        }

        // (Container<out User>) -> Container<out User>
        // (Container<out User>) -> Container<out Guest>
        abstract class ExGenericClassOut4 : GenericClassOut<User>() {
            abstract override fun foo(): (Container<out User>) -> Container<out Guest>
        }

        // (Container<out User>) -> Container<out User>
        // (Container<out User>) -> Container<Guest>
        abstract class ExGenericClassOut5 : GenericClassOut<User>() {
            abstract override fun foo(): (Container<out User>) -> Container<Guest>
        }


        open abstract class GenericClassIn<T> {
            abstract fun foo(): (Container<in T>) -> Container<in T>
        }

        // (Container<in T>) -> Container<in T>
        // (Container<in T>) -> Container<T>
        abstract class ExGenericClassIn1<T : User> : GenericClassIn<T>() {
            abstract override fun foo(): (Container<in T>) -> Container<T>
        }

        // (Container<in T>) -> Container<in T>
        // (Container<in T>) -> Container<in T>
        abstract class ExGenericClassIn2<T : User> : GenericClassIn<T>() {
            abstract override fun foo(): (Container<in T>) -> Container<in T>
        }

        // (Container<in User>) -> Container<in User>
        // (Container<in User>) -> Container<User>
        abstract class ExGenericClassIn3 : GenericClassIn<User>() {
            abstract override fun foo(): (Container<in User>) -> Container<User>
        }

        // (Container<in Guest>) -> Container<in Guest>
        // (Container<in Guest>) -> Container<in User>
        abstract class ExGenericClassIn4 : GenericClassIn<Guest>() {
            abstract override fun foo(): (Container<in Guest>) -> Container<in User>
        }

        // (Container<in Guest>) -> Container<in Guest>
        // (Container<in Guest>) -> Container<User>
        abstract class ExGenericClassIn5 : GenericClassIn<Guest>() {
            abstract override fun foo(): (Container<in Guest>) -> Container<User>
        }
    }
}

/**
 * # Principles & Compactibility of Generic Class/Function
 *
 * ## Principles of Use Cases
 *
 * ### Keywords
 *
 * Declaration-site:
 * 1. TPC  : type parameter declaration of class/interface
 * 2. TPF  : type parameter declaration of function
 *
 * Use-site:
 * 1. TAS  : type argument single, type argument as variable/parameter/return
 * 2. TAC  : type argument of class
 * 3. TAF  : type argument of function/property
 * 4. TAI  : type argument immediate
 *
 * ### Use Cases of Type Parameter/Argument
 *
 * ```
 * // For class
 *
 *     class ClassName<TPC>
 *     class ExClassName<TPC> : ClassName<TAI>
 *
 *
 * // For function
 *
 *     // Declaration
 *     fun <TPF> functionName(TAS): TAS
 *     fun <TPF> functionName(ClassName<TAC>): TAS
 *     fun <TPF> functionName(ClassName<TAC>): ClassName<TAC>
 *     fun <TPF> functionName(TAS): ClassName<TAC>
 *
 *     // Usage
 *     // functionName<TAF>()
 *
 *
 * // For reference
 *
 *     TAS = ...
 *     ClassName<TAC> = ...
 * ```
 *
 * ### Principles Table
 *
 * > | Use Cases | out/in         | <*>            | <T : Bound>    |
 *   |-----------|----------------|----------------|----------------|
 *   |    TPC    | √ (Variance)   | -              | √ (UpperBound) |
 *   |    TPF    | × (Variance)   | -              | √ (UpperBound) |
 *   |-----------|----------------|----------------|----------------|
 *   |    TAS    | × (Projection) | × (Projection) | -              |
 *   |    TAC    | √ (Projection) | √ (Projection) | -              |
 *   |    TAF    | × (Projection) | × (Projection) | -              |
 *   |    TAI    | × (Projection) | × (Projection) | -              |
 *
 * Note: You should first distinguish between Variance & Projection.
 *
 * ## Compactibility of 'out/in' Positions
 *
 * ### keywords
 *
 * 1. invariant : invariant position, only invariant is allowed
 * 2. out       : out position, covariant & invariant are allowed
 * 2. in        : in position, contravariant & invariant are allowed
 *
 * ### 'out/in' Positions of Type Argument
 *
 * ```
 *     // Function
 *     fun functionName('in'): 'out'
 *
 *     // Class
 *     InvariantClass<'invariant'>
 *     CovariantClass<'out'>
 *     ContravariantClass<'in'>
 *
 *     // Class members
 *     ClassName<T...> {
 *         memberProperty: 'invariant'                              reverse?
 *         memberProperty: InvariantClass<'invariant'>                  |
 *         memberProperty: CovariantClass<'invariant'> // <-------------|
 *         memberProperty: ContravariantClass<'invariant'> // <---------|
 *                                                                      |
 *         fun functionName('in'): 'out'                                |
 *         fun functionName(InvariantClass<'invariant'>): [As Class]    |
 *         fun functionName(CovariantClass<'in'>): [As Class] // <------|
 *         fun functionName(ContravariantClass<'out'>): [As Class] // <-|
 *     }
 * ```
 *
 * ### 'out/in' Compactibility Table
 *
 * > | Position  | invariant | covariant | contra | out      | in       | <*> |
 *   |-----------|-----------|-----------|--------|----------|----------|-----|
 *   |'invariant'| √         | ×         | ×      | √ (Proj) | √ (Proj) | √   |
 *   |   'out'   | √         | √         | ×      | √ (Proj) | × (Proj) | √   |
 *   |   'in'    | √         | ×         | √      | × (Proj) | √ (Proj) | √   |
 *
 * 1. invariant: allow type argument invariant,     projections in/out/star
 * 2. out      : allow type argument out/invariant, projections out/star
 * 3. in       : allow type argument in/invariant,  projections in/star
 * 4. projections are not allowed for any postion of type argument single
 */
@RunWith(JUnit4::class)
class KtGenericPrincipleTest {
    open class User(val name: String)
    class Guest : User("guest")

    open class Container<T>(var value: Any? = null) {
        open fun get(): T = value as T
        open fun set(t: T) {
            value = t
        }
    }
    open class OutContainer<out T>(var value: Any? = null)
    open class InContainer<in T>(var value: Any? = null)

    @Test
    @Ignore
    fun genericFunc() {
        /**
         * 1. Function with type parameter
         */
        fun <T> funcTp(t: T): T = t
        // fun <out T> funcOutTp(t: T): T = t      // TPF: Variance annotations are only allowed for type parameters of classes and interfaces
        // fun <in T> funcInTp(t: T): T = t        // TPF: Variance annotations are only allowed for type parameters of classes and interfaces
        // fun <*> funcTpStarProj(t: Any): Any = t // TPF: Projections are not allowed for type parameters
        fun <T : User> funcTpBound(t: T): T = t

        /**
         * 2. Function with type argument (single)
         */
        // fun <T> funcTpWithOutProj(t: out T): T = t        // TAS: Syntax error
        // fun <T> funcTpWithReturnOutProj(t: T): out T = t  // TAS: Syntax error
        // fun <T> funcTpWithInProj(t: in T): T = t          // TAS: Syntax error
        // fun <T> funcTpWithReturnInProj(t: T): in T = t    // TAS: Syntax error
        // fun <T> funcTpWithBound(t: T : User): T = t       // TAS: Syntax error
        // fun <T> funcTpWithReturnBound(t: T): T : User = t // TAS: Syntax error

        fun <T> typeArgSingleWithFuncTp(t: T) {
            val refTp: T = funcTp<T>(t)
            // val refOutTp: out T = funcTp<T>(t)      // TAS: Syntax error
            // val refInTp: in T = funcTp<T>(t)        // TAS: Syntax error
            // val refTpBound: T : User = funcTp<T>(t) // TAS: Syntax error
        }

        /**
         * 3. Function with type argument of function
         */
        fun <T> typeArgFuncWithFuncTp(t: T) {
            funcTp<T>(t)
            // funcTp<out T>(t)    // TAF:  Projections are not allowed on type arguments of functions and properties
            // funcTp<*>(t)        // TAF:  Projections are not allowed on type arguments of functions and properties
            // funcTp<T : User>(t) // TAF: Syntax error

            funcTp<User>(Guest())
            funcTp<User>(Guest())
            funcTp<Guest>(Guest())
        }

        /**
         * 4. Function with generic class
         */
        fun funcWithInvariantClass1(container: Container<User>): Container<User> = container
        fun funcWithInvariantClass2(container: Container<User>): Container<out User> = container
        fun funcWithInvariantClass3(container: Container<in User>): Container<in User> = container
        fun funcWithInvariantClass4(container: Container<User>): Container<*> = container
        fun funcWithInvariantClass5(container: Container<out User>): Container<*> = container
        fun funcWithInvariantClass6(container: Container<*>): Container<*> = container

        fun <T> funcTpWithInvariantClass1(container: Container<T>): Container<T> = container
        fun <T> funcTpWithInvariantClass2(container: Container<T>): Container<out T> = container
        fun <T> funcTpWithInvariantClass3(container: Container<in T>): Container<in T> = container
        fun <T> funcTpWithInvariantClass4(container: Container<T>): Container<*> = container
        fun <T> funcTpWithInvariantClass5(container: Container<out T>): Container<*> = container
        fun <T> funcTpWithInvariantClass6(container: Container<*>): Container<*> = container
        // fun <T> funcTpWithInvariantClass7(container: Container<T : User>): Container<*> = container          // TAC: Syntax error
        // fun <T> funcTpWithInvariantClass8(container: Container<T>): Container<T : User> = container          // TAC: Syntax error

        fun funcWithCovariantClass1(container: OutContainer<User>): OutContainer<User> = container
        fun funcWithCovariantClass2(container: OutContainer<User>): OutContainer<out User> = container
        // fun funcWithCovariantClass3(container: OutContainer<in User>): OutContainer<in User> = container     // TAC: Projection conflicting
        fun funcWithCovariantClass4(container: OutContainer<User>): OutContainer<*> = container
        fun funcWithCovariantClass5(container: OutContainer<*>): OutContainer<*> = container

        fun <T> funcTpWithCovariantClass1(container: OutContainer<T>): OutContainer<T> = container
        fun <T> funcTpWithCovariantClass2(container: OutContainer<T>): OutContainer<out T> = container
        fun <T> funcTpWithCovariantClass3(container: OutContainer<out T>): OutContainer<*> = container

        fun funcWithContravariantClass1(container: InContainer<User>): InContainer<User> = container
        fun funcWithContravariantClass2(container: InContainer<User>): InContainer<in User> = container
        // fun funcWithContravariantClass3(container: InContainer<out User>): InContainer<out User> = container // TAC: Projection conflicting

        fun <T> funcTpWithContravariantClass1(container: InContainer<T>): InContainer<T> = container
        fun <T> funcTpWithContravariantClass2(container: InContainer<T>): InContainer<in T> = container
        fun <T> funcTpWithContravariantClass3(container: InContainer<*>): InContainer<*> = container
    }

    @Test
    fun genericClass() {
        /**
         * 1. Generic class with type parameter declaration
         */
        class GenericClassDeclarationTp<T>
        class GenericClassDeclarationTpOutProj<out T>    // 'out/in' are variance annotations here, not projections
        class GenericClassDeclarationTpInProj<in T>      // 'out/in' are variance annotations here, not projections
        // class GenericClassDeclarationTpStarProj<*>    // TPC: Projections are not allowed for type parameters
        class GenericClassDeclarationTpBound<out T : User>
        class GenericClassDeclarationTpOutProjBound<out T : User>
        class GenericClassDeclarationTpInProjBound<in T : User>
        class GenericClassDeclarationTpBound1<out User>  // User is TP here, not class
        class GenericClassDeclarationTpBound2<in User>   // User is TP here, not class


        /**
         * 2.1 Generic class(invariant) inheritance
         */
        open class GenericClassTp<T>

        class ExGenericClassTp : GenericClassTp<User>()
        // class ExGenericClassTpStarProj : GenericClassTp<*>()                 // TAI: Projections are not allowed for immediate arguments of a supertype
        // class ExGenericClassTpBound : GenericClassTp<T : User>()             // TAI: Syntax error

        class ExGenericClassTpWithTp<R> : GenericClassTp<R>()
        // class ExGenericClassTpOutProjWithTp<R> : GenericClassTp<out R>()     // TAI: Projections are not allowed for immediate arguments of a supertype
        // class ExGenericClassTpInProjWithTp<R> : GenericClassTp<in R>()       // TAI: Projections are not allowed for immediate arguments of a supertype
        // class ExGenericClassTpStarProjWithTp<R> : GenericClassTp<*>()        // TAI: Projections are not allowed for immediate arguments of a supertype
        // class ExGenericClassTpOfBoundWithTp<R> : GenericClassTp<R : User>()  // TAI: Syntax error

        // class ExGenericClassTpWithOutTp<out R> : GenericClassTp<R>()         // TAI: Type parameter R is declared as 'out' but occurs in 'invariant' position
        // class ExGenericClassTpWithInTp<in R> : GenericClassTp<R>()           // TAI: Type parameter R is declared as 'in' but occurs in 'invariant' position
        // class ExGenericClassTpWithTpStarProj<*> : GenericClassTp<*>()        // TPC, TAI: Projections are not allowed for type parameters, or immediate arguments of a supertype
        class ExGenericClassTpWithTpBound<R : User> : GenericClassTp<R>()

        class ExGenericClassTpReif : GenericClassTp<User>()
        class ExGenericClassTpReifWithTp<R> : GenericClassTp<User>()
        class ExGenericClassTpReifWithOutTp<out R> : GenericClassTp<User>()
        class ExGenericClassTpReifWithInTp<in R> : GenericClassTp<User>()
        // class ExGenericClassTpReifWithTpStarProj<*> : GenericClassTp<User>() // TPC: Projections are not allowed for type parameters
        class ExGenericClassTpReifWithTpBound<R : User> : GenericClassTp<User>()
        class ExGenericClassTpReifWithOutTpBound<out R : User> : GenericClassTp<User>()


        /**
         * 2.2 Generic class(variant) inheritance
         */
        open class GenericClassOutTp<out T>
        class ExGenericClassOutTpWithTp<R> : GenericClassOutTp<R>() // TODO
        class ExGenericClassOutTpWithOutTp<out R> : GenericClassOutTp<R>()
        class ExGenericClassOutTpWithOutTpBound<out R : User> : GenericClassOutTp<R>()
        class ExGenericClassOutTpReif: GenericClassOutTp<User>()

        open class GenericClassInTp<in T>
        class ExGenericClassInTpWithTp<R> : GenericClassInTp<R>()   // TODO
        class ExGenericClassInTpWithInTp<in R> : GenericClassInTp<R>()
        class ExGenericClassInTpWithInTpBound<in R : User> : GenericClassInTp<R>()
        class ExGenericClassInTpReif : GenericClassInTp<User>()

        open class GenericClassTpBound<T : User>
        // class ExGenericClassTpBoundWithTp<R> : GenericClassTpBound<R>()               // Type argument is not within its bounds.
        class ExGenericClassTpBoundWithTpBound<R: User> : GenericClassTpBound<R>()
        class ExGenericClassTpBoundWithTpLowerBound<R: Guest> : GenericClassTpBound<R>() // 'Guest' is a final type, and thus a value of the type parameter is predetermined
        class ExGenericClassTpBoundReifWithTp<R> : GenericClassTpBound<User>()
        class ExGenericClassTpBoundReifWithOutTp<out R> : GenericClassTpBound<User>()
        class ExGenericClassTpBoundReifWithTpBound<R: User> : GenericClassTpBound<User>()
        class ExGenericClassTpBoundReifWithInTpBound<in R: User> : GenericClassTpBound<User>()
        class ExGenericClassTpBoundReifWithTpHigherBound<R: Any> : GenericClassTpBound<Guest>()

        open class GenericClassOutTpBound<out T : User>
        class ExGenericClassOutTpBoundWithTpBound<R : User> : GenericClassOutTpBound<R>()
        class ExGenericClassOutTpBoundWithTpLowerBound<R : Guest> : GenericClassOutTpBound<R>()
        class ExGenericClassOutTpBoundWithOutTp<out R : User> : GenericClassOutTpBound<R>()
        class ExGenericClassOutTpBoundReif : GenericClassOutTpBound<User>()

        open class GenericClassInTpBound<in T : User>
        class ExGenericClassInTpBoundWithTpBound<R : User> : GenericClassInTpBound<R>()
        class ExGenericClassInTpBoundWithTpLowerBound<R : Guest> : GenericClassInTpBound<R>()
        class ExGenericClassInTpBoundWithInTp<in R : User> : GenericClassInTpBound<R>()
        class ExGenericClassInTpBoundReif : GenericClassInTpBound<User>()


        /**
         * 3. Generic class members
         */
        class OuterGenericClassTp<T1, out T2, in T3> {

            /** Property with type argument is ok (Kotlin In Action, 9.1.1) **/
            var memberPropWithTp : T1? = null
            // var memberPropWithOutTp : T2? = null // TAS: Type parameter T2 is declared as 'out' but occurs in 'invariant' position in type T2?
            // var memberPropWithInTp : T3? = null  // TAS: Type parameter T3 is declared as 'in' but occurs in 'invariant' position in type T3?

            var memberPropWithGenericClassTp: Container<T1>? = null
            var memberPropWithGenericClassTpOutProj: Container<out T1>? = null
            var memberPropWithGenericClassTpInProj: Container<in T1>? = null
            var memberPropWithGenericClassOutTpOutProj: OutContainer<out T1>? = null // TODO: Auto projection ???
            var memberPropWithGenericClassInTpInProj: InContainer<in T1>? = null     // TODO: Auto projection ???
            var memberPropWithGenericClassTpStarProj: Container<*>? = null
            // var memberPropWithGenericClassOutTp: OutContainer<T2>? = null         // TAC: Type parameter T2 is declared as 'out' but occurs in 'invariant' position in type OutContainer<T2>?
            // var memberPropWithGenericClassInTp: InContainer<T3>? = null           // TAC: Type parameter T3 is declared as 'in' but occurs in 'invariant' position in type InContainer<T3>?


            /** Property with type parameter is NOT ok **/
            // var <T> memberPropTp: Container<T>? = null           // TODO: Type parameter of a property must be used in its receiver type
            // var <out T> memberPropOutTp: OutContainer<T>? = null // TODO: Type parameter of a property must be used in its receiver type,
            //  TPF: Variance annotations are only allowed for type parameters of classes and interfaces,
            //  TAC: Type parameter T is declared as 'out' but occurs in 'invariant' position in type OutContainer<T>?
            // var <in T> memberPropInTp: InContainer<T>? = null    // TODO: Type parameter of a property must be used in its receiver type,
            //  TPF:  Variance annotations are only allowed for type parameters of classes and interfaces,
            //  TAC:  Type parameter T is declared as 'in' but occurs in 'invariant' position in type InContainer<T>?


            fun <T> memberFuncTp(t: T): T = t
            // fun <out T> memberFuncOutTp(t: T): T = t // TPF:  Variance annotations are only allowed for type parameters of classes and interfaces
            // fun <in T> memberFuncInTp(t: T): T = t   // TPF:  Variance annotations are only allowed for type parameters of classes and interfaces

            fun memberFuncWithTp(t: T1): T1 = t
            // fun memberFuncWithOutTp(t: T2): T2 = t   // TAS:  Type parameter T2 is declared as 'out' but occurs in 'in' position in type T2
            // fun memberFuncWithInTp(t: T3): T3 = t    // TAS:  Type parameter T is declared as 'in' but occurs in 'out' position in type T


            fun memberFuncWithGenericClassTp1(container: Container<T1>): Container<T1> = container
            fun memberFuncWithGenericClassTp2(container: Container<T1>): Container<out T1> = container
            fun memberFuncWithGenericClassTp3(container: Container<in T1>): Container<in T1> = container
            fun memberFuncWithGenericClassTp4(container: Container<out T1>): Container<out T1> = container
            fun memberFuncWithGenericClassTp5(container: Container<*>): Container<*> = container

            fun memberFuncWithGenericClassOutTp1(container: OutContainer<T1>): OutContainer<T1> = container
            // fun memberFuncWithGenericClassOutTp2(container: OutContainer<T2>): OutContainer<T2> = container    // TODO: Type parameter T2 is declared as 'out' but occurs in 'in' position in type OutContainer<T2>
            fun memberFuncWithGenericClassOutTp3(container: OutContainer<T3>): OutContainer<*> = container
            fun memberFuncWithGenericClassInTp1(container: InContainer<T1>): InContainer<T1> = container
            // fun memberFuncWithGenericClassInTp2(container: InContainer<T3>): InContainer<T3> = container       // TODO: Type parameter T3 is declared as 'in' but occurs in 'out' position in type InContainer<T3>
            fun memberFuncWithGenericClassInTp3(container: InContainer<T2>): InContainer<*> = container
            fun memberFuncWithGenericClassOutTpCast(container: OutContainer<out T3>): OutContainer<*> = container // TODO
            fun memberFuncWithGenericClassInTpCast(container: InContainer<in T2>): InContainer<*> = container     // TODO


            inner class InnerTp<T>
            inner class InnerOutTp<out T>
            inner class InnerInTp<in T>
            inner class InnerExGenericClassTpWithTp<T> : GenericClassTp<T>()
            inner class InnerExGenericClassTpOuterTpAsImmArg : GenericClassTp<T1>()
            inner class InnerExGenericClassOutTpOuterTpAsImmArg : GenericClassOutTp<T1>()
            inner class InnerExGenericClassOutTpOuterOutTpAsImmArg : GenericClassOutTp<T2>()
            inner class InnerExGenericClassInTpOuterTpAsImmArg : GenericClassInTp<T1>()
            inner class InnerExGenericClassInTpOuterInTpAsImmArg : GenericClassInTp<T3>()


            // TODO: Ext function/property with tp
            // var <T> Container<T>.extPropTp
            // var Container<T1>.extPropTpOuterTpAsImmArg
            fun <T> Container<T>.extFuncTp(): T = this.get()
            fun Container<T1>.extFuncTpOuterTpAsImmArg(): T1 = this.get()
        }

        /**
         * 4. Generic class without type parameter
         */
        open class GenericClass {
            open fun <T> call(container: Container<T>) {}
            open fun <T> fetch(any: Any): Container<T> = Container<T>(any)
        }

        class ExGenericClass : GenericClass()

        class ExGenericClassWithTp<T> : GenericClass() {
            override fun <T> call(container: Container<T>) {}
            override fun <R> fetch(any: Any): Container<R> = super.fetch(any)
        }
        class ExGenericClassWithOutTp<out T> : GenericClass()
        class ExGenericClassWithInTp<in T> : GenericClass()
    }
}

/**
 * Subtyping & projections in inheritances of generic class
 */
@RunWith(JUnit4::class)
class KtGenericOverrideTest {
    sealed class ERROR {
        class Syntax : ERROR()
        class TypeMismatch : ERROR()

        /**
         * 'out/in' use cases error
         * 1. Declaration-site variance annotations are only allowed for type parameters of classes and interfaces
         * 2. Projections are not allowed for type parameters, or immediate arguments of a supertype
         */
        class Principles : ERROR()

        /**
         * accidental override
         * 1. For compiler, Foo#call has different function signature with ExFoo#call, thus 'override' can not be added
         * 2. If 'override' is omitted, Foo#call & ExFoo#call are regarded as different functions.
         *    But compiler can infer that Foo#call & ExFoo#call has same jvm signature, thus something called 'accidental override' occurs
         */
        class AccidentalOverride : ERROR()

        /**
        * conflicting overloads
        * JVM allows method overloads with different return type of signature, but it is forbidden by language design & compiler.
        */
        class ConflictingOverloads : ERROR()
    }

    open class User(val name: String)
    class Guest : User("guest")

    open class Container<T>(var value: Any? = null) {
        open fun get(): T = value as T
        open fun set(t: T) {
            value = t
        }
    }

    @Test
    fun genericClassMember() {
        /**
         * Class with generic functions
         */
        open class GenericFuncClass {
            open fun <T> call(t: T) {
                println("I'm ${t.toString()}")
            }

            open fun <T> fetch(any: Any): T = with(any) {
                println("Cast from ${any.javaClass.simpleName}")
                return any as T
            }
        }

        class ExGenericFuncClass : GenericFuncClass() {
            /**********
             ** [ERROR.Syntax]
             */
            // override fun <T> call(t: out T) {}
            // override fun <T> call(t: in T) {}
            // override fun <T> fetch(any: Any): in T = super.fetch(any)
            // override fun <T> fetch(any: Any): out T = super.fetch(any)
            /*********/

            /**********
             ** [ERROR.Principles]
             ** variance annotations are only allowed for type parameters of classes and interfaces
             */
            // override fun <out T> call(t: T) {}
            // override fun <in T> call(t: T) {}
            // override fun <in T> fetch(any: Any): T = super.fetch(any)
            // override fun <out T> fetch(any: Any): T = super.fetch(any)
            /*********/
        }

        /**
         * Class that has generic class as generic function parameter or return
         */
        open class GenericClass {
            open fun <T> call(container: Container<T>) {}
            open fun <T> fetch(any: Any): Container<T> = Container<T>(any)
        }

        class ExGenericClass : GenericClass() {

            /*** ok ***/
            // override fun <T> call(container: Container<T>) {}
            // override fun <R> fetch(any: Any): Container<R> = super.fetch(any)

            /**********
             ** [ERROR.AccidentalOverride]
             ** Compiler takes the generic classes as overloads of different types
             **
             **     'fun (Container<out/in T>)' with
             **     'fun (ContainerT>)'
             */
            // override fun <T> call(container: Container<out T>) {}
            // override fun <T> call(container: Container<in T>) {}
            // fun <T> call(container: Container<out T>) {}
            // fun <T> call(container: Container<in T>) {}
            /**
             **     'fun <T, R> (Container<T/R>)' with
             **     'fun <T> (ContainerT>)'
             */
            // fun <T, R> call(container: Container<T>) {}
            // fun <T, R> call(container: Container<R>) {}
            /*********/

            /**********
             ** [ERROR.TypeMismatch]
             **
             **     Return type is 'Container<out/in T#1 (type parameter of ExGenericClass.fetch)>'
             **     which is not a subtype of
             **     overridden 'fun <T> fetch(any: Any): Container<T#2 (type parameter of GenericClass.fetch)>'
             */
            // override fun <T> fetch(any: Any): Container<out T> = super.fetch(any)
            // override fun <T> fetch(any: Any): Container<in T> = super.fetch(any)
            /**
             **     Return type is 'Container<*>'
             **     which is not a subtype of
             **     overridden 'fun <T> fetch(any: Any): Container<T#1 (type parameter of GenericClass.fetch)>'
             */
            // override fun <T> fetch(any: Any): Container<*> = super.fetch<T>(any)
            /*********/

            /**********
             ** [ERROR.ConflictingOverloads]
             **
             **     'fun fetch(any: Any): Container<*>' with
             **     'fun <T> fetch(any: Any): Container<T>'
             */
            // fun fetch(any: Any): Container<*> = super.fetch<Any>(any)
            /**
             **     'fun <T, R> fetch(any: Any): Container<R>' with
             **     'fun <T> fetch(any: Any): Container<T>'
             */
            // fun <T, R> fetch(any: Any): Container<R> = super.fetch(any)
            // fun <T, R> fetch(any: Any): Container<T> = super.fetch(any)
            /*********/
        }

        class ExGenericClassWithTp<R> : GenericClass() {

            /*** ok ***/
            // override fun <T> call(container: Container<T>) {}
            // override fun <T> fetch(any: Any): Container<T> = super.fetch(any)
            // override fun <R> call(container: Container<R>) {}
            // override fun <R> fetch(any: Any): Container<R> = super.fetch(any)

            /**********
             ** [ERROR.AccidentalOverride]
             */
            // fun call(container: Container<R>) {}
            // fun <T> call(container: Container<R>) {}
            // fun <T, R> call(container: Container<T>) {}
            // fun <T, R> call(container: Container<R>) {}
            //
            // fun <T : User> call(container: Container<T>) {}
            // fun <T : User> fetch(any: Any): Container<T> = super.fetch(any)
            /*********/


            /**********
             ** [ERROR.TypeMismatch]
             **
             **     Return type is 'Container<R>', which is not a subtype of
             **     overridden 'fun <T> fetch(any: Any): Container<T#1 (type parameter of GenericClass.fetch)>'
             */
            // override fun <T> fetch(any: Any): Container<R> = super.fetch(any)
            /**
             **     Return type is 'Container<*>', which is not a subtype of
             **     overridden 'fun <T> fetch(any: Any): Container<T#1 (type parameter of GenericClass.fetch)>'
             */
            // override fun <R> fetch(any: Any): Container<*> = super.fetch<R>(any)
            // override fun <R> fetch(any: Any): Container<out R> = super.fetch(any)
            // override fun <R> fetch(any: Any): Container<in R> = super.fetch(any)
            /*********/

            /**********
             ** [ERROR.ConflictingOverloads]
             *
             *     'fun fetch(any: Any): Container<R> of' with
             *     'fun <T> fetch(any: Any): Container<T>'
             */
            // override fun fetch(any: Any): Container<R> = super.fetch(any)
            // override fun <T, R> fetch(any: Any): Container<T> = super.fetch(any)
            // override fun <T, R> fetch(any: Any): Container<R> = super.fetch(any)
            /*********/
        }
    }

    @Test
    fun genericClass() {
        /**
         * Class that has generic class as parameter or return, also has a type parameter.
         */
        open class GenericClassTp<T> {
            open fun call(container: Container<T>) {}
            open fun fetch(any: Any): Container<T> = Container<T>(any)
        }

        /**
         *        -> R
         * <T>    -> T
         * <R>    -> R
         * <T>    -> R
         * <T, R> -> T
         * <T, R> -> R
         * <R: ?> -> R
         *
         *        -> <out R>
         *        -> <in R>
         *        -> <*>
         */
        class ExGenericClassTpWithTp<R> : GenericClassTp<R>() {

            /*** ok ***/
            // override fun call(container: Container<R>) {}
            // override fun fetch(any: Any): Container<R> = super.fetch(any)

            /**********
             ** [ERROR.AccidentalOverride]
             */
            // fun <T> call(container: Container<T>) {}
            // fun <R> call(container: Container<R>) {}
            // fun <T, R>call(container: Container<R>) {}
            // fun <T, R>call(container: Container<T>) {}
            //
            // fun <R : User> call(container: Container<R>) {}
            //
            // fun call(container: Container<in R>) {}
            // fun call(container: Container<out R>) {}
            // fun call(container: Container<*>) {}
            /*********/

            /**********
             ** [ERROR.TypeMismatch]
             */
            // override fun fetch(any: Any): Container<in R> = super.fetch(any)
            // override fun fetch(any: Any): Container<out R> = super.fetch(any)
            // override fun fetch(any: Any): Container<*> = super.fetch(any)
            /*********/

            /**********
             ** [ERROR.ConflictingOverloads]
             */
            // fun <T> call(container: Container<R>) {}
            // override fun <T> fetch(any: Any): Container<T> = super.fetch(any) as Container<T>
            // override fun <R> fetch(any: Any): Container<R> = super.fetch(any) as Container<R>
            // override fun <T> fetch(any: Any): Container<R> = super.fetch(any)
            // override fun <T, R> fetch(any: Any): Container<R> = super.fetch(any) as Container<R>
            // override fun <T, R> fetch(any: Any): Container<T> = super.fetch(any) as Container<T>
            //
            // override fun <R : User> fetch(any: Any): Container<R> = super.fetch(any)
            /*********/
        }
    }
}