package com.kaedea.kotlin

import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.properties.Delegates
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*
import kotlin.test.*

/**
 * Kotlin reflect tweak with apis:
 * - [kotlin.reflect.KAnnotatedElement]
 * - [kotlin.reflect.KClass]
 * - [kotlin.reflect.KParameter]
 * - [kotlin.reflect.KCallable]
 * - [kotlin.reflect.KFunction], [Getter][kotlin.reflect.KProperty.Getter], [Setter][kotlin.reflect.KMutableProperty.Setter]
 * - [kotlin.reflect.KProperty]
 *
 * - [kotlin.reflect.KClassifier]
 * - [kotlin.reflect.KType]
 * - [kotlin.reflect.KVisibility]
 * - [kotlin.reflect.KVariance]
 * - [kotlin.reflect.KTypeParameter]
 * - [kotlin.reflect.KTypeProjection]
 *
 * Extension utils:
 * - [kotlin.reflect.full.KClasses.kt][createInstance]
 * - [kotlin.reflect.full.KAnnotatedElements.kt][findAnnotation]
 * - [kotlin.reflect.full.KCallables.kt][findParameterByName]
 * - [kotlin.reflect.full.KProperties.kt][kotlin.reflect.full.getExtensionDelegate]
 * - [kotlin.reflect.full.KClassifier.kt][createType]
 * - [kotlin.reflect.full.KTypes.kt][kotlin.reflect.full.isSubtypeOf]
 *
 * JVM Utils
 * - [kotlin.reflect.jvm.KCallablesJVM.kt][isAccessible]
 * - [kotlin.reflect.jvm.KClassesJvm.kt][kotlin.reflect.jvm.jvmName]
 * - [kotlin.reflect.jvm.KTypesJvm.kt][kotlin.reflect.jvm.jvmErasure]
 * - [kotlin.reflect.jvm.ReflectJvmMapping.kt][kotlin.reflect.jvm.javaType]
 * - [kotlin.reflect.jvm.ReflectLambdaKt.kt][kotlin.reflect.jvm.reflect]
 *
 * @author Kaede
 * @since  2018/8/13
 */

@RunWith(JUnit4::class)
class KtReflectTest {

    /**
     * @see [kotlin.reflect.KAnnotatedElement]
     * @see [kotlin.reflect.full.KAnnotatedElements.kt]
     */
    @Test
    fun ktAnnotationElement() {
        val annotations = KtReflectTest::class.annotations
        assertNotNull(annotations)

        val runWith = annotations.find { it is RunWith } as RunWith
        assertEquals(JUnit4::class, runWith.value)

        // ext utils
        // kotlin.reflect.full.KAnnotatedElements.kt
        val findAnnotation = KtReflectTest::class.findAnnotation<RunWith>()!!
        assertEquals(JUnit4::class, findAnnotation.value)
    }

    /**
     * @see [kotlin.reflect.KClass]
     * @see [kotlin.reflect.KClassifier]
     * @see [kotlin.reflect.full.KClasses.kt]
     */
    @Test
    fun ktClassOrObject() {
        // KClass of class
        val userClass = User::class
        val user1 = userClass.createInstance()
        val user2 = userClass.objectInstance

        assertTrue(userClass is KClass<User>)
        assertTrue(userClass is KClassifier)
        assertNotNull(user1)
        assertNull(user2)

        // KClass of object
        val guestClass = Guest::class // object is regarded as KClass
        try {
            val guest1 = guestClass.createInstance()
            fail()
        } catch (ignored: Exception) {
        }
        val guest2 = guestClass.objectInstance
        assertTrue(userClass is KClass<*>) // info of KClass<Guest> is erased
        assertTrue(userClass is KClassifier)
        assertNotNull(guest2)

        // KClass
        assertEquals("User", userClass.simpleName)
        assertEquals("com.kaedea.kotlin.User", userClass.qualifiedName)
        assertEquals(KVisibility.PUBLIC, userClass.visibility)
        assertTrue(!userClass.isFinal)
        assertTrue(userClass.isOpen)
        assertTrue(!userClass.isData)
        assertTrue(!userClass.isSealed)
        assertTrue(!userClass.isAbstract)
        assertTrue(!userClass.isInner)
        assertNull(userClass.objectInstance)
        assertNotNull(guestClass.objectInstance)
        assertTrue(!userClass.isCompanion)
        assertTrue(!guestClass.isCompanion)

        assertEquals(2, userClass.constructors.size)
        arrayOf("_name", "_age", "name", "age", "changeName",
                "toString", "equals", "hashCode").asSequence().forEach { name ->
            userClass.members.find { it.name == name }!!
        }
        assertEquals(emptyList(), userClass.nestedClasses)
        assertEquals(emptyList(), userClass.typeParameters)
        assertEquals(Any::class.createType(), userClass.supertypes.first())

        // ext utils
        // kotlin.reflect.full.KClasses.kt
        val user = userClass.createInstance()
        assertEquals("n/a", user.name)
        assertNull(userClass.primaryConstructor)
        assertNull(userClass.companionObject)
        assertNull(userClass.companionObjectInstance)
        arrayOf("_name", "_age", "name", "age", "changeName", "toString").asSequence().forEach { name ->
            userClass.declaredMembers.find { it.name == name }!!
        }

        // member/static/ext functions
        arrayOf("changeName", "toString", "equals", "hashCode").asSequence().forEach { name ->
            userClass.functions.find { it.name == name }!!
        }
        arrayOf("changeName", "toString", "equals", "hashCode").asSequence().forEach { name ->
            userClass.memberFunctions.find { it.name == name }!!
        }
        arrayOf("changeName", "toString").asSequence().forEach { name ->
            userClass.declaredFunctions.find { it.name == name }!!
        }
        arrayOf("changeName", "toString").asSequence().forEach { name ->
            userClass.declaredMemberFunctions.find { it.name == name }!!
        }
        assertEquals(emptyList(), userClass.staticFunctions)
        assertEquals(emptyList(), userClass.memberExtensionFunctions)
        assertEquals(emptyList(), userClass.declaredMemberExtensionFunctions)

        // member/static/ext properties
        arrayOf("_age", "_name", "age", "name", "sex").asSequence().forEach { name ->
            userClass.memberProperties.find { it.name == name }!!
        }
        arrayOf("email", "id").asSequence().forEach { name ->
            userClass.memberExtensionProperties.find { it.name == name }!!
        }
        arrayOf("_age", "_name", "age", "name", "sex").asSequence().forEach { name ->
            userClass.declaredMemberProperties.find { it.name == name }!!
        }
        arrayOf("email", "id").asSequence().forEach { name ->
            userClass.declaredMemberExtensionProperties.find { it.name == name }!!
        }
        assertEquals(emptyList(), userClass.staticProperties)

        // casting
        assertEquals(listOf(User::class), guestClass.superclasses)
        assertEquals(
                listOf(User::class, Any::class),
                guestClass.allSuperclasses
        )
        assertEquals(
                listOf(User::class.createType(), Any::class.createType()),
                guestClass.allSupertypes
        )
        assertTrue(guestClass.isSubclassOf(userClass))
        assertTrue(userClass.isSuperclassOf(guestClass))
        val any = user as Any
        assertEquals("n/a", userClass.cast(any).name)
        assertNull(guestClass.safeCast(any))
    }

    /**
     * @see [kotlin.reflect.KParameter]
     */
    @Test
    fun ktParam() {
        val func = ::info

        val param = func.parameters.find {
            it.index == 0
        }!!

        assertEquals("user", param.name)
        assertTrue(param.type == User::class.createType())
        assertTrue(param.kind == KParameter.Kind.VALUE)
        assertTrue(!param.isOptional)
        assertTrue(!param.isVararg)

        val type = func.returnType
        assertEquals(String::class, type.classifier)
    }

    /**
     * @see [kotlin.reflect.KCallable]
     * @see [kotlin.reflect.KFunction]
     * @see [kotlin.reflect.full.KCallables.kt]
     */
    @Test
    fun ktFunc() {
        val func = ::info

        assertTrue(func is KCallable<*>)
        assertTrue {
            !func.isOpen &&
                    !func.isAbstract &&
                    func.isFinal

        }
        assertEquals("info", func.name)
        assertEquals(KVisibility.PUBLIC, func.visibility)
        assertEquals("kaede(17)", func.call(User("kaede", 17)))
        assertEquals("kaede(17)", func.callBy(mapOf(
                func.parameters[0] to User("kaede", 17)
        )))
        func.returnType

        assertTrue(func is KFunction<*>)
        assertTrue {
            !func.isInline &&
                    !func.isExternal &&
                    !func.isOperator &&
                    !func.isInfix &&
                    !func.isSuspend
        }

        // ext utils
        // kotlin.reflect.full.KCallables.kt
        val param = func.findParameterByName("user")!!
        assertTrue(param.type == User::class.createType())
        assertTrue(param.kind == KParameter.Kind.VALUE)

        assertNull(func.instanceParameter)
        assertNull(func.extensionReceiverParameter)
        assertEquals(listOf(param), func.valueParameters)
        assertTrue {
            func.valueParameters.all { it.kind == KParameter.Kind.VALUE }
        }

        val extFunc = User::copy
        assertEquals(emptyList(), extFunc.valueParameters)
        assertNull(extFunc.instanceParameter)
        assertTrue(extFunc.extensionReceiverParameter!!.type == User::class.createType())
        assertTrue(extFunc.extensionReceiverParameter!!.kind == KParameter.Kind.EXTENSION_RECEIVER)

        val insFunc = User::changeName
        assertTrue(insFunc.instanceParameter!!.type == User::class.createType())
        assertTrue(insFunc.instanceParameter!!.kind == KParameter.Kind.INSTANCE)
        assertNull(insFunc.extensionReceiverParameter)
    }

    /**
     * @see [kotlin.reflect.KProperty]
     * @see [kotlin.reflect.full.KProperties.kt]
     */
    @Test
    fun ktProp() {
        // KProperty
        val prop = User::name
        assertEquals(Guest.name, prop.get(Guest))
        assertEquals(prop.call(Guest), prop.get(Guest))
        assertTrue(!prop.isLateinit)
        assertTrue(!prop.isConst)

        val getter = prop.getter
        assertTrue(getter is KFunction<*>)
        assertEquals(Guest.name, getter.invoke(Guest))
        assertEquals(prop, getter.property)

        // KMutableProperty
        val mutProp = User::age
        val user = User()
        mutProp.set(user, 17)
        assertEquals(17, user.age)
        val setter = mutProp.setter
        assertTrue(setter is KFunction<*>)
        setter.call(user, 20)
        assertEquals(20, user.age)

        // Ext Property
        val extProperty = User::class.memberExtensionProperties.find {
            it.name == "email"
        } as KProperty2<User, User, String>
        assertEquals("guest@gmail.com", extProperty.get(Guest, Guest))

        // Delegated Property
        assertNull(prop.getDelegate(user))
        val propProxy = User::sex
        propProxy.isAccessible = true
        val propDelegate = propProxy.getDelegate(Guest) as ObservableProperty<String>
        assertNotNull(propDelegate)
        assertEquals("male", propDelegate.getValue(user, propProxy))
        propDelegate.setValue(user, propProxy, "female")
        assertEquals("female", propDelegate.getValue(user, propProxy))
        try {
            propDelegate.setValue(user, propProxy, "otoko no ko")
            fail()
        } catch (e: Exception) {
        }

        // ext utils
        // kotlin.reflect.full.KProperties.kt
        val extPropProxy = User::class.memberExtensionProperties.find {
            it.name == "id"
        }!!
        extPropProxy.isAccessible = true
        val extPropDelegate = extPropProxy.getExtensionDelegate(user) as ReadWriteProperty<User, Int>
        try {
            extPropDelegate.getValue(user, extPropProxy)
            fail("NotNullVar delegated")
        } catch (e: Exception) {
        }
        extPropDelegate.setValue(user, extPropProxy, 2233)
        assertEquals(2233, extPropDelegate.getValue(user, extPropProxy))
    }

    /**
     * @see [kotlin.reflect.KType]
     * @see [kotlin.reflect.full.KTypes.kt]
     */
    @Test
    fun ktType() {
        val type = ::info.returnType
        assertTrue {
            !type.isMarkedNullable &&
                    type.arguments === emptyList<KTypeProjection>() &&
                    type.classifier == String::class
        }

        // kotlin.reflect.full.KTypes.kt
        val guestType = Guest::class.createType()
        assertTrue(guestType.isSubtypeOf(User::class.createType()))
        assertTrue(!guestType.isSupertypeOf(User::class.createType()))
        assertTrue(!guestType.isMarkedNullable)
        val nullableType = guestType.withNullability(true)
        assertTrue(nullableType.isMarkedNullable)
    }

    /**
     * Note that [KClass] & [KTypeParameter] are both [KClassifier],
     * [KTypeParameter] is not [KParameter], but a special builtin parameter of generic class/callable.
     *
     * @see [kotlin.reflect.KClassifier]
     * @see [kotlin.reflect.KTypeParameter]
     * @see [kotlin.reflect.full.KClassifier.kt]
     */
    @Test
    fun ktTypeParameter() {
        // KClassifier
        val userClass = User::class
        assertTrue(userClass is KClassifier)

        // KTypeParameter
        val listClass = List::class
        val typedParam = listClass.typeParameters.single()
        assertEquals("E", typedParam.name)
        assertEquals(KVariance.OUT, typedParam.variance)
        assertEquals(
                Any::class.createType().withNullability(true),
                typedParam.upperBounds.single()
        )
        assertTrue(!typedParam.isReified)

        // ext utils
        // kotlin.reflect.full.KClassifier.kt
        val guestType = Guest::class.createType()
        assertEquals(Guest::class, guestType.classifier)
        val starProjectedType = Guest::class.starProjectedType
        assertEquals(Guest::class, starProjectedType.classifier)
    }

    /**
     * JavaClass is instance of [Class],
     * while KotlinClass is instance of [kotlin.reflect.jvm.internal.KClassImpl]
     */
    @Test
    fun ktClassLiteral() {
        // KClass
        assertNotNull(User::class as KClass<*>)
        assertEquals(Guest::class, Guest.javaClass.kotlin)
        val annotation = KtReflectTest::class.findAnnotation<RunWith>()!!
        assertEquals(RunWith::class, annotation.annotationClass)

        // Class
        assertNotNull(User::class.java as Class<*>)
        assertEquals(Guest::class.java, Guest.javaClass)
        assertEquals(null, String::class.javaPrimitiveType)
        assertEquals(String::class.java, String::class.javaObjectType)
        assertEquals("int", Int::class.javaPrimitiveType.toString())
        assertEquals(java.lang.Integer::class.java, Int::class.javaObjectType as Class<Integer>)

        // Class of KClass : Class<KClassImpl>
        val kClass = User::class
        val kClassInstance = kClass as KClassifier
        val javaClassOfKClass = kClassInstance.javaClass
        val classKClassImpl = Class.forName("kotlin.reflect.jvm.internal.KClassImpl")!!
        assertEquals(classKClassImpl, javaClassOfKClass)

        // KClass of Class<KClassImpl>
        // assertEquals(KClassImpl::class, javaClassOfKClass.kotlin)
        assertEquals("KClassImpl", javaClassOfKClass.kotlin.simpleName)
    }
}

class KtReflectJvmTest {

    /**
     * @see [kotlin.reflect.jvm.KCallablesJVM.kt]
     */
    @Test
    fun ktCallable() {
        val prop = User::class.memberProperties.find {
            it.name == "_name"
        }!!
        assertTrue(!prop.isAccessible)
        try {
            prop.get(Guest)
            fail()
        } catch (e: Exception) {
        }
        prop.isAccessible = true
        assertEquals("guest", prop.get(Guest))
    }

    /**
     * @see [kotlin.reflect.jvm.KClassesJvm.kt]
     */
    @Test
    fun ktClass() {
        assertEquals("com.kaedea.kotlin.User", User::class.jvmName)
        assertEquals("int", Int::class.jvmName)
        assertEquals("java.lang.String", String::class.jvmName)
    }

    /**
     * @see [kotlin.reflect.jvm.KTypesJvm.kt]
     */
    @Test
    fun ktType() {
        val listClass = List::class
        assertTrue(listClass.typeParameters.isNotEmpty())

        val typedParam = listClass.typeParameters.single()
        val listClassErased = typedParam.createType().jvmErasure
        assertEquals(emptyList(), listClassErased.typeParameters)
    }


    /**
     * @see [kotlin.reflect.jvm.ReflectJvmMapping.kt]
     */
    @Test
    fun ktJvmMapping() {
        // kotlin -> java
        assertEquals("int", Int::class.defaultType.javaType.typeName)
        assertEquals(String::class.java, String::class.defaultType.javaType)

        val construct = User::class.constructors.first()
        val constructor = construct.javaConstructor!!
        assertEquals("n/a", constructor.newInstance().name)

        val func = ::info
        val infoMethod = func.javaMethod!!
        assertEquals("guest(0)", infoMethod.invoke(Guest, Guest))

        assertNull(User::name.javaField)
        val nameProp = User::class.declaredMembers.first {
            it.name == "_name"
        } as KProperty
        val nameFiled = nameProp.javaField!!
        nameFiled.isAccessible = true
        assertEquals("guest", nameFiled.get(Guest))

        // java -> kotlin
        kotlin.run {
            val construct = User::class.java.getConstructor()
            val constructor = construct.kotlinFunction!!
            assertEquals("n/a", constructor.call().name)

            val method = User::class.java.getDeclaredMethod("changeName", String::class.java)
            val func = method.kotlinFunction!!
            val user = User()
            func.call(user, "user")
            assertEquals("user", user.name)

            val field = User::class.java.getDeclaredField("_age")!!
            val property = field.kotlinProperty!!
            property.isAccessible = true
            assertEquals(0, property.getter.call(Guest))
        }
    }

    /**
     * @see [kotlin.reflect.jvm.ReflectLambdaKt.kt]
     */
    @Test
    @Ignore("experimental api")
    fun ktLambda() {

    }
}

// ----------
// Materials for test
// ----------
open class User {
    val name
        get() = _name ?: "n/a"
    var age
        get() = _age ?: 0
        set(value) {
            _age = value
        }
    public var sex: String by Delegates.observable("male", { pro, old, new ->
        if (new != "male" && new != "female") throw IllegalArgumentException("Unknown sex")
    })

    private var _name: String? = null
    private var _age: Int? = null

    constructor()
    constructor(name: String, age: Int) {
        _name = name
        _age = age
    }

    fun changeName(name: String) {
        _name = name
    }

    override fun toString() = "$name($age)"

    val User.email get() = "$name@gmail.com"
    val User.id by Delegates.notNull<Int>()
}

object Guest : User("guest", 0)

fun info(user: User) = user.toString()

fun User.copy() = User(name, age)