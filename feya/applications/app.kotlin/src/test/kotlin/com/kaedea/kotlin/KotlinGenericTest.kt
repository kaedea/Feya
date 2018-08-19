package com.kaedea.kotlin

import com.kaedea.kotlin.utils.Values
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
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

@RunWith(JUnit4::class)
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


        fun <T> get(t: T) = t
        val get = get<User>(User("a"))

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

    inline fun <reified T : Any> isGivenTypeReified(instance: Any): Boolean {
        return instance is T &&                       // 'is T' is ok when reified
                instance.javaClass.kotlin == T::class // 'T::class' is ok when reified
    }

    inline fun <reified T> reifiedInsteadOfClassLiteral(): String? {
        println("Loading class : ${T::class.simpleName}")
        return T::class.simpleName
    }
}

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

        /* 1. Constructor : invariant */
        val userContainer1: Container<User> = Container<User>(User("Kaede"))
        val guestContainer1: Container<Guest> = Container<Guest>(Guest())
        val intContainer: Container<Int> = Container<Int>(Integer.valueOf(2233))
        // error
        // val userContainer2: Container<User> = Container<Guest>(User("Kaede"))
        // val guestContainer2: Container<Guest> = Container<User>(User("Kaede"))
        // val anyContainer: Container<Any?> = Container<Int>(Integer.valueOf(2233))


        /* 2.1 Member param : covariant */
        Container<User>().set(User("Kaedea"))
        Container<User>().set(Guest())
        Container<Guest>().set(Guest())
        Container<Any?>().set(Integer.valueOf(2233))
        // error
        // Container<Guest>().set(User("Kaede"))

        /* 2.2 Member return : covariant  */
        val user1: User = Container<User>(User("Kaede")).get()
        val user2: User = Container<Guest>(Guest()).get()
        val guest1: Guest = Container<Guest>(Guest()).get()
        val any: Any? = Container<Int>(Integer.valueOf(2233)).get()
        // error
        // val guest2: Guest = Container<User>(User("Kaede")).get()


        fun <T> call(container: Container<T>) {
            println("I'm ${container.get()} from ${container}")
        }

        /* 3.1 Func param : invariant */
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

        /* 3.2 Func return : invariant */
        val userContainerFetch1: Container<User> = fetch<User>(User("Kaede"))
        val guestContainerFetch1: Container<Guest> = fetch<Guest>(Guest())
        val anyFetch: Any = fetch<User>(User("Kaede"))
        // error
        // val userContainerFetch2: Container<User> = fetch<Guest>(Guest())
        // val guestContainerFetch2: Container<Guest> = fetch<User>(User("Kaede"))
        // val anyContainerFetch: Container<Any> = fetch<User>(User("Kaede"))


        class ExContainer<T>(value: Any?) : Container<T>(value)

        /* 4.1 Ext constructor : invariant */
        val userExContainer1: ExContainer<User> = ExContainer<User>(User("Kaede"))
        val userContainerCast1: Container<User> = ExContainer<User>(User("Kaede"))
        val guestExContainer1: ExContainer<Guest> = ExContainer<Guest>(Guest())
        val guestContainerCast2: Container<Guest> = ExContainer<Guest>(Guest())
        // error
        // val userExContainer2: ExContainer<User> = ExContainer<Guest>(Guest())
        // val userContainerCast2: Container<User> = ExContainer<Guest>(Guest())

        /* 4.2 Ext func param : invariant */
        call<User>(ExContainer<User>(User("Kaede")))
        call<Guest>(ExContainer<Guest>(Guest()))
        // error
        // call<Guest>(ExContainer<User>(User("Kaede")))
        // call<User>(ExContainer<Guest>(Guest()))

        /* 4.2 Ext func param : invariant */
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

        /* 1. Constructor : covariant */
        val userContainer1: Container<User> = Container<User>(User("Kaede"))
        val guestContainer1: Container<Guest> = Container<Guest>(Guest())
        val intContainer: Container<Int> = Container<Int>(Integer.valueOf(2233))
        val userContainer2: Container<User> = Container<Guest>(User("Kaede"))
        val anyContainer: Container<Any?> = Container<Int>(Integer.valueOf(2233))
        // error
        // val guestContainer2: Container<Guest> = Container<User>(User("Kaede"))

        /* 2.1 Member param : not support */
        /* 2.2 Member return : covariant  */
        val user1: User = Container<User>(User("Kaede")).get()
        val user2: User = Container<Guest>(Guest()).get()
        val guest1: Guest = Container<Guest>(Guest()).get()
        val any: Any? = Container<Int>(Integer.valueOf(2233)).get()
        // error
        // val guest2: Guest = Container<User>(User("Kaede")).get()


        fun <T> call(container: Container<out T>) {
            println("I'm ${container.get()} from ${container}")
        }

        /* 3.1 Func param : covariant */
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

        /* 3.2 Func return : covariant */
        val userContainerFetch1: Container<User> = fetch<User>(User("Kaede"))
        val guestContainerFetch1: Container<Guest> = fetch<Guest>(Guest())
        val userContainerFetch2: Container<User> = fetch<Guest>(Guest())
        val anyContainerFetch: Container<Any> = fetch<User>(User("Kaede"))
        // error
        // val guestContainerFetch2: Container<Guest> = fetch<User>(User("Kaede"))
        val anyFetch: Any = fetch<User>(User("Kaede"))


        class ExContainer<out T>(value: Any?) : Container<T>(value)

        /* 4.1 Ext constructor : covariant */
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

        /* 4.2 Ext func param : covariant */
        call<User>(ExContainer<User>(User("Kaede")))
        call<Guest>(ExContainer<Guest>(Guest()))
        call<User>(ExContainer<Guest>(Guest()))
        // error
        // call<Guest>(ExContainer<User>(User("Kaede")))

        /* 4.2 Ext func return : n/a */
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

        /* 1. Constructor : contravariant */
        val userContainer1: Container<User> = Container<User>(User("Kaede"))
        val guestContainer1: Container<Guest> = Container<Guest>(Guest())
        val intContainer: Container<Int> = Container<Int>(Integer.valueOf(2233))
        val guestContainer2: Container<Guest> = Container<User>(User("Kaede"))
        val guestContainer3: Container<Guest> = Container<Any?>()
        // error
        // val userContainer2: Container<User> = Container<Guest>(User("Kaede"))
        // val anyContainer: Container<Any?> = Container<Int>(Integer.valueOf(2233))

        /* 2.1 Member param : covariant */
        /* 2.2 Member return : not support */
        Container<User>().set(User("Kaedea"))
        Container<User>().set(Guest())
        Container<Guest>().set(Guest())
        Container<Any?>().set(Integer.valueOf(2233))
        // error
        // Container<Guest>().set(User("Kaede"))


        fun <T> call(container: Container<in T>) {
            println("I'm ${container}")
        }

        /* 3.1 Func param : contravariant */
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

        /* 3.2 Func return : contravariant */
        val userContainerFetch1: Container<User> = fetch<User>(User("Kaede"))
        val guestContainerFetch1: Container<Guest> = fetch<Guest>(Guest())
        val guestContainerFetch2: Container<Guest> = fetch<User>(User("Kaede"))
        // error
        // val userContainerFetch2: Container<User> = fetch<Guest>(Guest())
        // val anyContainerFetch: Container<Any> = fetch<User>(User("Kaede"))
        val anyFetch: Any = fetch<User>(User("Kaede"))


        class ExContainer<in T>(value: Any? = null) : Container<T>(value)

        /* 4.1 Ext constructor : contravariant */
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

        /* 4.2 Ext func param : contravariant */
        call<User>(ExContainer<User>(User("Kaede")))
        call<Guest>(ExContainer<Guest>(Guest()))
        call<Guest>(ExContainer<User>(User("Kaede")))
        call<Guest>(ExContainer<Any?>())
        // error
        // call<User>(ExContainer<Guest>(Guest()))

        /* 4.2 Ext func return : n/a */
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

        // out projection with upper bound of Container<T>
        fun callOutProjectBound(container: Container<out User>) {
            println("I'm ${container.get()} from ${container}")
        }

        callOutProjectBound(Container<User>())
        callOutProjectBound(Container<Guest>())
        // error
        // callOutProjectBound(Container<Any>())


        /**
         * The following function use __type projection__ to make it become covariant
         * @see [covariant]
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
     * Type projections
     * Type parameters
     * Type arguments
     * Variance annotations
     */
    fun projections2() {
        open class User(val name: String)
        class Guest : User("guest")

        /**
         * Type projections not work in inheritances
         *
         * ERROR 1: accidental override
         * 1. For compiler, Foo#call has different function signature with ExFoo#call, thus 'override' can not be added
         * 2. If 'override' is omitted, Foo#call & ExFoo#call are regarded as different functions.
         *    But compiler can infer that Foo#call & ExFoo#call has same jvm signature, thus something called 'accidental override' occurs
         *
         * ERROR 2: conflicting overloads
         * JVM allows method overloads with different return type of signature, but it is forbidden by language design & compiler.
         *
         * ERROR 4: Type projections can only apply/occur in 'out/in' positions
         * ERROR 0: Syntax error
         *
         * ERROR 3: Declaration-site variance error
         * 1. Declaration-site variance annotations are only allowed for type parameters of classes and interfaces
         */

        open class Container<T>(var value: Any? = null) {
            open fun get(): T = value as T
            open fun set(t: T) {
                value = t
            }
        }

        /**
         * Class that has generic class as parameter or return
         */
        open class GenericClass {
            open fun <T> call(container: Container<T>) {}
            open fun <T> fetch(any: Any): Container<T> = Container<T>(any)
        }

        class ExGenericClass : GenericClass() {

            /** ok **/
            // override fun <T> call(container: Container<T>) {}
            // override fun <R> fetch(any: Any): Container<R> = super.fetch(any)

            /**********
             * ERROR 1, accidental override
             * Compiler takes the generic classes as overloads of different types
             *
             *     'fun (Container<out/in T>)' with
             *     'fun (ContainerT>)'
             */
            // override fun <T> call(container: Container<out T>) {}
            // override fun <T> call(container: Container<in T>) {}
            // fun <T> call(container: Container<out T>) {}
            // fun <T> call(container: Container<in T>) {}
            /**
             *     'fun <T, R> (Container<T/R>)' with
             *     'fun <T> (ContainerT>)'
             */
            // fun <T, R> call(container: Container<T>) {}
            // fun <T, R> call(container: Container<R>) {}
            /**
             *********/

            // ERROR 2, return type mismatch
            //
            //     Return type is 'Container<out/in T#1 (type parameter of ExGenericClass.fetch)>'
            //     which is not a subtype of
            //     overridden 'fun <T> fetch(any: Any): Container<T#2 (type parameter of GenericClass.fetch)>'
            // override fun <T> fetch(any: Any): Container<out T> = super.fetch(any)
            // override fun <T> fetch(any: Any): Container<in T> = super.fetch(any)
            //
            //     Return type is 'Container<*>'
            //     which is not a subtype of
            //     overridden 'fun <T> fetch(any: Any): Container<T#1 (type parameter of GenericClass.fetch)>'
            // override fun <T> fetch(any: Any): Container<*> = super.fetch<T>(any)

            // ERROR 3, Conflicting overloads
            //
            //     'fun fetch(any: Any): Container<*>' with
            //     'fun <T> fetch(any: Any): Container<T>'
            // fun fetch(any: Any): Container<*> = super.fetch<Any>(any)
            //     'fun <T, R> fetch(any: Any): Container<R>' with
            //     'fun <T> fetch(any: Any): Container<T>'
            // fun <T, R> fetch(any: Any): Container<R> = super.fetch(any)
            // fun <T, R> fetch(any: Any): Container<T> = super.fetch(any)

        }

        class ExGenericClassWithTp<R> : GenericClass() {

            // ok
            // override fun <T> call(container: Container<T>) {}
            // override fun <T> fetch(any: Any): Container<T> = super.fetch(any)
            // override fun <R> call(container: Container<R>) {}
            // override fun <R> fetch(any: Any): Container<R> = super.fetch(any)

            // ERROR 1, accidental override
            //
            // fun call(container: Container<R>) {}
            // fun <T> call(container: Container<R>) {}
            // fun <T, R> call(container: Container<T>) {}
            // fun <T, R> call(container: Container<R>) {}
            //
            // fun <T : User> call(container: Container<T>) {}
            // fun <T : User> fetch(any: Any): Container<T> = super.fetch(any)

            // ERROR 2, return type mismatch
            //
            //     Return type is 'Container<R>', which is not a subtype of
            //     overridden 'fun <T> fetch(any: Any): Container<T#1 (type parameter of GenericClass.fetch)>'
            // override fun <T> fetch(any: Any): Container<R> = super.fetch(any)
            //
            //     Return type is 'Container<*>', which is not a subtype of
            //     overridden 'fun <T> fetch(any: Any): Container<T#1 (type parameter of GenericClass.fetch)>'
            // override fun <R> fetch(any: Any): Container<*> = super.fetch<R>(any)
            // override fun <R> fetch(any: Any): Container<out R> = super.fetch(any)
            // override fun <R> fetch(any: Any): Container<in R> = super.fetch(any)

            // ERROR 3, Conflicting overloads
            //
            //     'fun fetch(any: Any): Container<R> of' with
            //     'fun <T> fetch(any: Any): Container<T>'
            // override fun fetch(any: Any): Container<R> = super.fetch(any)
            // override fun <T, R> fetch(any: Any): Container<T> = super.fetch(any)
            // override fun <T, R> fetch(any: Any): Container<R> = super.fetch(any)
        }


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

            // ok
            // override fun call(container: Container<R>) {}
            // override fun fetch(any: Any): Container<R> = super.fetch(any)

            // ERROR 1, accidental override
            //
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

            // ERROR 2, return type mismatch
            //
            // override fun fetch(any: Any): Container<in R> = super.fetch(any)
            // override fun fetch(any: Any): Container<out R> = super.fetch(any)
            // override fun fetch(any: Any): Container<*> = super.fetch(any)

            // ERROR 3, Conflicting overloads
            //
            // fun <T> call(container: Container<R>) {}
            // override fun <T> fetch(any: Any): Container<T> = super.fetch(any) as Container<T>
            // override fun <R> fetch(any: Any): Container<R> = super.fetch(any) as Container<R>
            // override fun <T> fetch(any: Any): Container<R> = super.fetch(any)
            // override fun <T, R> fetch(any: Any): Container<R> = super.fetch(any) as Container<R>
            // override fun <T, R> fetch(any: Any): Container<T> = super.fetch(any) as Container<T>
            //
            // override fun <R : User> fetch(any: Any): Container<R> = super.fetch(any)
        }

        // class ExGenericClassTpOutPrjWithTp<R> : GenericClassTp<out R>()
        // class ExGenericClassTpInProjWithTp<R> : GenericClassTp<in R>()
        // class ExGenericClassTpStarProjWithTp<R> : GenericClassTp<*>()

        // class ExGenericClassTpWithTpOfOutProj<out R> : GenericClassTp<R>()
        // class ExGenericClassTpWithTpOfinProj<in R> : GenericClassTp<R>()
        // class ExGenericClassTpWithTpOfStarProj<*> : GenericClassTp<*>()

        class ExGenericClassTpWithTpBound<R : User> : GenericClassTp<R>()
        class ExGenericClassTpReif : GenericClassTp<User>()
        class ExGenericClassTpReifWithTp<R> : GenericClassTp<User>()
        class ExGenericClassTpReifWithTpOfOutProj<out R> : GenericClassTp<User>()
        class ExGenericClassTpReifWithTpOfinProj<in R> : GenericClassTp<User>()
        // class ExGenericClassTpReifWithTpofStarPrj<*> : GenericClassTp<User>()


        open class GenericFunc {
            open fun <T> call(t: T) {
                println("I'm ${t.toString()}")
            }

            open fun <T> fetch(any: Any): T = with(any) {
                println("Cast from ${any.javaClass.simpleName}")
                return any as T
            }
        }

        class ExGenericFunc : GenericFunc() {
            // error 3, syntax
            // override fun <T> call(t: out T) {}
            // override fun <T> call(t: in T) {}
            // override fun <T> fetch(any: Any): in T = super.fetch(any)
            // override fun <T> fetch(any: Any): out T = super.fetch(any)

            // error 4, variance annotations are only allowed for type parameters of classes and interfaces
            // override fun <out T> call(t: T) {}
            // override fun <in T> call(t: T) {}
            // override fun <in T> fetch(any: Any): T = super.fetch(any)
            // override fun <out T> fetch(any: Any): T = super.fetch(any)
        }
    }
}

@RunWith(JUnit4::class)
class KtGenericInheritingTest {

    @Test
    @Ignore
    fun classInheriting() {
        open class User(val name: String)
        class Guest : User("guest")

        open class Container<T>(var value: Any? = null) {
            open fun get(): T = value as T
            open fun set(t: T) {
                value = t
            }
        }

        open class GenericClass {
            open fun <T> call(container: Container<T>) {}
            open fun <T> fetch(any: Any): Container<T> = Container<T>(any)
        }

        class ExGenericClass : GenericClass()

        class ExGenericClassWithTp<T> : GenericClass() {
            override fun <T> call(container: Container<T>) {}
            override fun <R> fetch(any: Any): Container<R> = super.fetch(any)
        }

        class ExGenericClassWithTpOfOutProj<out T> : GenericClass()
        class ExGenericClassWithTpOfInProj<in T> : GenericClass()
        // class ExGenericClassWithTpOfStarProj<*> : GenericClass()


        // ----------
        // GenericClass With Type Parameter
        // 1. Depends on generic class (Container<T>)
        // 2. Has type parameter (T)
        // ----------

        /**
         * # Principles of Generic Class Declaration
         *
         * ## Keywords
         * Declaration-site:
         * 1. TPC  : type parameter declaration of class/interface
         * 2. TPF  : type parameter declaration of function
         *
         * Use-site:
         * 1. TAS  : type argument single
         * 2. TAC  : type argument of class
         * 3. TAF  : type argument of function/property
         * 4. TAI  : type argument immediate
         *
         * in/out position
         * invariant position
         *
         * ## Use Cases:
         * For class
         * ```
         *     class ClassName<TPC>
         *     class ExClassName<TPC> : ClassName<TAI>
         * ```
         *
         * For function
         * ```
         *     // Declaration
         *     fun <TPF> functionName(TAS): TAR
         *     fun <TPF> functionName(ClassName<TAC>): TAS
         *     fun <TPF> functionName(ClassName<TAC>): ClassName<TAC>
         *     fun <TPF> functionName(TAS): ClassName<TAC>
         *
         *     // Usage
         *     // functionName<TAFP>()
         * ```
         *
         * For reference
         * ```
         *     TAS = ...
         *     ClassName<TAC> = ...
         * ```
         *
         * ## Principles
         */

        // | Position   | out/in        | <*>            | <T : Bound> |
        // |------------|---------------|----------------|-------------|
        // |   TPC      | √             | ×              | √           |
        // |   TPF      | ×             | ×              | √           |
        // |------------|---------------|----------------|-------------|
        // |   TAS      | √             | √              |             |
        // |   TAC      | √             | √              |             |
        // |   TAF      |               |                |             |
        // |   TAI      | ×             | ×              | ×           |

        // ----------
        // Generic Function
        // ----------
        fun <T> funcTp(t: T): T = t
        fun <out T> funcTpOutProj(t: T): T = t  // Variance annotations are only allowed for type parameters of classes and interfaces
        fun <*> funcTpStarProj(t: Any): Any = t // Type parameter name expected
        fun <T : User> funcTpBound(t: T): T = t // Type parameter name expected

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

        fun <T> funcTpWithTpRef(t: T) {
            val ref: T = funcTp(t)
        }


        // ----------
        // Generic Class Declaration
        // ----------
        class GenericClassDeclarationTp<T>
        class GenericClassDeclarationTpOutProj<out T>
        class GenericClassDeclarationTpInProj<in T>
        class GenericClassDeclarationTpStarProj<*>   // Type parameter name expected, Start projection not allowed in TAD
        class GenericClassDeclarationTpBound<out T : User>
        class GenericClassDeclarationTpOutProjBound<out T : User>
        class GenericClassDeclarationTpInProjBound<in T : User>
        class GenericClassDeclarationTpBound1<out User> // User is TP here, not class
        class GenericClassDeclarationTpBound2<in User>  // User is TP


        // ----------
        // Generic Class(Invariant) Inheritance
        // ----------
        open class GenericClassTp<T>

        class ExGenericClassTp : GenericClassTp<T>()
        class ExGenericClassTpOutPrj : GenericClassTp<out T>()
        class ExGenericClassTpInProj : GenericClassTp<in T>() // Projections are not allowed for immediate arguments of a supertype
        class ExGenericClassTpStarProj : GenericClassTp<*>()  // Projections are not allowed for immediate arguments of a supertype
        class ExGenericClassTpBound : GenericClassTp<T : User>()

        class ExGenericClassTpWithTp<R> : GenericClassTp<R>()
        class ExGenericClassTpOutPrjWithTp<R> : GenericClassTp<out R>() // Projections are not allowed for immediate arguments of a supertype
        class ExGenericClassTpInProjWithTp<R> : GenericClassTp<in R>()
        class ExGenericClassTpStarProjWithTp<R> : GenericClassTp<*>()   // Projections are not allowed for immediate arguments of a supertype
        class ExGenericClassTpBoundWithTp<R> : GenericClassTp<R : User>()

        class ExGenericClassTpWithTpOfOutProj<out R> : GenericClassTp<R>() // Type parameter R is declared as 'out' but occurs in 'invariant' position in type GenericClassTp<R>
        class ExGenericClassTpWithTpOfinProj<in R> : GenericClassTp<R>()
        class ExGenericClassTpWithTpOfStarProj<*> : GenericClassTp<*>()
        class ExGenericClassTpWithTpBound<R : User> : GenericClassTp<R>()

        class ExGenericClassTpReif : GenericClassTp<User>()
        class ExGenericClassTpReifWithTp<R> : GenericClassTp<User>()
        class ExGenericClassTpReifWithTpOfOutProj<out R> : GenericClassTp<User>()
        class ExGenericClassTpReifWithTpOfinProj<in R> : GenericClassTp<User>()
        class ExGenericClassTpReifWithTpofStarPrj<*> : GenericClassTp<User>()
        class ExGenericClassTpReifWithTpBound<R : User> : GenericClassTp<User>()
        class ExGenericClassTpReifWithTpOutProjBound<out R : User> : GenericClassTp<User>()


        // ----------
        // Generic Class(Variant) Inheritance
        // ----------
        open class GenericClassOutTp<out T>
        class ExGenericClassOutTpWithTp<R> : GenericClassOutTp<R>() // ???
        class ExGenericClassOutTpWithOutTp<out R> : GenericClassOutTp<R>()
        class ExGenericClassOutTpWithOutToBound<out R : User> : GenericClassOutTp<R>()
        class ExGenericClassOutTpReif: GenericClassOutTp<User>()

        open class GenericClassInTp<in T>
        class ExGenericClassInTpWithTp<R> : GenericClassInTp<R>()
        class ExGenericClassInTpWithInTp<in R> : GenericClassInTp<R>()
        class ExGenericClassInTpWithInTpBound<in R : User> : GenericClassInTp<R>()
        class ExGenericClassInTpReif : GenericClassInTp<User>()

        open class GenericClassBoundTp<T : User>
        class ExGenericClassBoundTpWithTp<R> : GenericClassBoundTp<R>()                  // Type argument is not within its bounds.
        class ExGenericClassBoundTpWithTpBound<R: User> : GenericClassBoundTp<R>()
        class ExGenericClassBoundTpWithTpLowerBound<R: Guest> : GenericClassBoundTp<R>() // 'Guest' is a final type, and thus a value of the type parameter is predetermined
        class ExGenericClassBoundTpReifWithTp<R> : GenericClassBoundTp<User>()
        class ExGenericClassBoundTpReifWithOutTp<out R> : GenericClassBoundTp<User>()
        class ExGenericClassBoundTpReifWithTpBound<R: User> : GenericClassBoundTp<User>()
        class ExGenericClassBoundTpReifWithInTpBound<in R: User> : GenericClassBoundTp<User>()
        class ExGenericClassBoundTpReifWithTpHigherBound<R: Any> : GenericClassBoundTp<Guest>()

        open class GenericClassOutBoundTp<out T : User>
        class ExGenericClassOutBoundTpWithTpBound<R : User> : GenericClassOutBoundTp<R>()
        class ExGenericClassOutBoundTpWithTpLowerBound<R : Guest> : GenericClassOutBoundTp<R>()
        class ExGenericClassOutBoundTpWithOutTp<out R : User> : GenericClassOutBoundTp<R>()
        class ExGenericClassOutBoundTpReif : GenericClassOutBoundTp<User>()

        open class GenericClassInBoundTp<in T : User>
        class ExGenericClassInBoundTpWithTpBound<R : User> : GenericClassInBoundTp<R>()
        class ExGenericClassInBoundTpWithTpLowerBound<R : Guest> : GenericClassInBoundTp<R>()
        class ExGenericClassInBoundTpWithInTp<in R : User> : GenericClassInBoundTp<R>()
        class ExGenericClassInBoundTpReif : GenericClassInBoundTp<User>()


        // ----------
        // Generic Class Member
        // ----------
        class OutterGenericDeclarationTp<T> {
            var member: Container<T>? = null
            open inner class Inner<T>
            inner class ExInnerTpWithTp<T> : Inner<T>()
            inner class ExInnerTpOuterTpAsImmArgWithTp<R> : Inner<T>()

            fun memerFuncTp(t: T): T = t
            fun <T> memerFuncWithTp(t: T): T = t
        }
    }
}

@RunWith(JUnit4::class)
class KtGenericFuncParamTest {

    @Test
    @Ignore("TP as func param is not invariant")
    fun invariant() {
    }

    @Test
    fun covariant() {
        open class User(val name: String)
        class Guest : User("guest")


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
    @Ignore("TP as func param is not contravariant")
    fun contravariant() {
    }
}

@RunWith(JUnit4::class)
class KtGenericFuncReturnTest {

    @Test
    @Ignore("TP as func return is not invariant")
    fun invariant() {
    }

    @Test
    fun covariant() {
        open class User(val name: String)
        class Guest : User("guest")


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

    @Test
    @Ignore("TP as func return is not contravariant")
    fun contravariant() {
    }
}