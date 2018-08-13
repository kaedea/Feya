package com.kaedea.kotlin

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.reflect.*
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Kotlin Reflect Tweak with:
 * - [kotlin.reflect.KAnnotatedElement]
 * - [kotlin.reflect.KClass]
 * - [kotlin.reflect.KParameter]
 * - [kotlin.reflect.KCallable]
 * - [kotlin.reflect.KFunction], [Getter][kotlin.reflect.KProperty.Getter], [Setter][kotlin.reflect.KMutableProperty.Setter]
 * - [kotlin.reflect.KProperty]
 *
 * - [kotlin.reflect.KType]
 * - [kotlin.reflect.KClassifier]
 * - [kotlin.reflect.KVisibility]
 * - [kotlin.reflect.KVariance]
 * - [kotlin.reflect.KTypeParameter]
 *
 * @author Kaede
 * @since  2018/8/13
 */

@RunWith(JUnit4::class)
class KtReflectTest {

    /**
     * @see [kotlin.reflect.KAnnotatedElement]
     */
    @Test
    fun ktAnnotationElement() {
        val annotations = KtReflectTest::class.annotations
        assertNotNull(annotations)
        assertTrue {
            annotations.any {
                it.annotationClass == JUnit4::class
            }
        }
    }

    open class User {
        val name
            get() = _name ?: "n/a"
        val age
            get() = _age ?: 0

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
    }

    object Guest : User()

    public final fun info(user: User) = user.toString()

    /**
     * @see [kotlin.reflect.KClass]
     * @see [kotlin.reflect.KClassifier]
     */
    @Test
    fun ktClassOrObject() {
        val userClass = User::class
        val user1 = userClass.createInstance()
        val user2 = userClass.objectInstance

        val guestClass = Guest::class // object is regarded as KClass
        val guest1 = guestClass.createInstance()
        val guest2 = guestClass.objectInstance
    }

    /**
     * @see [kotlin.reflect.KParameter]
     */
    @Test
    fun ktParam() {
        val func = ::info

        val param = func.parameters.find { it is User }!!
        assertTrue {
            param.index == 0 &&
                    param.type == User::class &&
                    param.kind == KParameter.Kind.INSTANCE &&
                    !param.isOptional &&
                    !param.isVararg
        }

        val typeParameters = func.typeParameters
        assertEquals(emptyList(), typeParameters)

        val type = func.returnType
        assertEquals(String::class, type.classifier)
    }

    /**
     * @see [kotlin.reflect.KCallable]
     * @see [kotlin.reflect.KFunction]
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
    }

    /**
     * @see [kotlin.reflect.KProperty]
     */
    @Test
    fun ktProp() {

    }

    /**
     * @see [kotlin.reflect.KClassifier]
     * @see [kotlin.reflect.KType]
     */
    @Test
    fun ktType() {
        val classifier = User::class as KClassifier
        val type = classifier.createType()
        assertTrue {
            !type.isMarkedNullable &&
                    type.arguments === emptyList<KTypeProjection>() &&
                    type.classifier == User::class
        }
    }
}