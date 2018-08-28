package com.kaedea.kotlin

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
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
        var text : String? = null

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
    fun lockCondition() {
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        var text : String? = null

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
}