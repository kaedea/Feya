package com.kaedea.kotlin

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.test.assertEquals

/**
 * @author Kaede
 * @since  2018/8/28
 */

@RunWith(JUnit4::class)
class KtConcurrentTest {

    @Test
    fun waitNotify() {
        val lock = Object()
        var text: String? = null

        Thread {
            Thread.sleep(100)
            text = "2233"

            synchronized(lock) {
                lock.notifyAll()
            }
        }.start()

        while (text == null) {
            synchronized(lock) {
                lock.wait()
            }
        }

        assertEquals("2233", text)
    }

    @Test
    fun waitNotify2() {
        fun Object.await(block: () -> Unit) {
            synchronized(this) {
                wait()
            }
            block()
        }

        fun Object.signal() {
            synchronized(this) {
                notify()
            }
        }

        val lock = Object()
        var text: String? = null

        Thread {
            Thread.sleep(100)
            text = "2233"

            lock.signal()
        }.start()

        lock.await {
            assertEquals("2233", text)
            text = "3322"
        }
        assertEquals("3322", text)
    }


    @Test
    fun lockCondition() {
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        var text: String? = null

        Thread {
            Thread.sleep(100)
            text = "2233"

            lock.lock()
            try {
                condition.signalAll()
            } finally {
                lock.unlock()
            }
        }.start()

        while (text == null) {
            lock.lock()
            try {
                condition.await()
            } finally {
                lock.unlock()
            }
        }

        assertEquals("2233", text)
    }

    @Test
    fun lockCondition2() {
        fun Lock.wait(condition: Condition, block: () -> Unit) {
            lock()
            try {
                condition.await()
            } finally {
                unlock()
            }
            block()
        }

        fun Lock.notify(condition: Condition) {
            lock()
            try {
                condition.signal()
            } finally {
                unlock()
            }
        }

        val lock = ReentrantLock()
        val condition = lock.newCondition()
        var text: String? = null

        Thread {
            Thread.sleep(100)
            text = "2233"

            lock.notify(condition)
        }.start()

        lock.wait(condition) {
            assertEquals("2233", text)
            text = "3322"
        }
        assertEquals("3322", text)
    }
}