package com.kaedea.kotlin

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.test.assertEquals
import kotlin.test.todo

/**
 * Kotlin annotation apis tweaking:
 * - [Target]
 * - [Retention]
 * - [Repeatable]
 * - [MustBeDocumented]
 *
 * @author Kaede
 * @since  2018/8/14
 */

@RunWith(JUnit4::class)
class KtAnnotateTest {
    @Test
    fun ktSerialize() {
        val string = Person("Kaede", 17).serialize()
        assertEquals("{\"age\":\"17\",\"name\":\"Kaede\"}", string)
    }

    @Test
    fun ktAnnotated() {
        Person::class.findAnnotation<MyAnn>()!!

        Person::class.constructors.find {
            it.parameters.any {
                it.findAnnotation<Exclude>() != null
            }
        }!!

        todo {
            // annotation is wtf missing!!
            Person::class.constructors.find {
                it.parameters.any {
                    it.findAnnotation<JsonName>() != null
                }
            }!!

            Person::class.members.find {
                it.findAnnotation<Exclude>() != null
            }!!

            Person::class.members.find {
                it.findAnnotation<JsonName>() != null
            }!!
        }
    }

    @Test
    fun ktSerializeWithAnnotate() {
        todo {
            val kClass = Person::class

            // annotation is wtf missing!!
            val excluded = kClass.memberProperties.find {
                it.findAnnotation<Exclude>() != null
            }!!

            val field = kClass.java.fields.find {
                it.getAnnotation(Exclude::class.java) != null
            }!!

            val string = Person("Kaede", 17).serializeWithAnn()
            assertEquals("{\"name\":\"Kaede\"}", string)
        }
    }

    @Test
    fun ktSerializeWithAnnotate2() {
        todo {
            val kClass = Person::class

            // annotation is wtf missing!!
            val excluded = kClass.memberProperties.find {
                it.findAnnotation<JsonName>() != null
            }!!

            val field = kClass.java.fields.find {
                it.getAnnotation(JsonName::class.java) != null
            }!!

            val string = Person("Kaede", 17).serializeWithAnn2()
            assertEquals("{\"age\":\"17\",\"username\":\"Kaede\"}", string)
        }
    }

    @Test
    fun ktDeserialize() {
        todo {}
    }
}

fun Any.serialize(): String =
        this.javaClass.kotlin.memberProperties.joinToString(
                separator = ",",
                prefix = "{",
                postfix = "}") { prop ->
            """
            "${prop.name}":"${prop.get(this)}"
            """.trimIndent()
        }

fun Any.serializeWithAnn(): String =
        this.javaClass.kotlin.memberProperties
                .filter { it.findAnnotation<Exclude>() != null }
                .joinToString(separator = ",", prefix = "{", postfix = "}") { prop ->
                    """
                    "${prop.name}":"${prop.get(this)}"
                    """.trimIndent()
                }

fun Any.serializeWithAnn2(): String =
        this.javaClass.kotlin.memberProperties.joinToString(
                separator = ",",
                prefix = "{",
                postfix = "}") { prop ->
            """
            "${prop.findAnnotation<JsonName>()?.name ?: prop.name}":"${prop.get(this)}"
            """.trimIndent()
        }

@java.lang.annotation.Target(ElementType.FIELD)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
annotation class Exclude

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonName(val name: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MyAnn

@MyAnn
data class Person(@JsonName("username") val name: String, @Exclude val age: Int)
