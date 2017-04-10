/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.concurrent;

import android.os.Handler;
import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Kaede
 * @since 17/4/9
 */
@RunWith(AndroidJUnit4.class)
public class StaticPuzzle {

    @Test
    public void testStaticSynchronous() {
        String foo = Foo.foo();
        Assert.assertEquals("HELLO", foo);
    }

    @Test
    public void testStaticSynchronousWorkerThread() throws InterruptedException {
        final String[] foo = new String[1];
        Runnable runnable = new Runnable() {
            public void run() {
                foo[0] = Foo.foo();
                synchronized (StaticPuzzle.class) {
                    StaticPuzzle.class.notify();
                }
            }
        };

        new Thread(runnable).start();

        synchronized (StaticPuzzle.class) {
            StaticPuzzle.class.wait();
        }
        Assert.assertEquals("HELLO", foo[0]);
    }

    @Test
    public void testStaticSynchronousWithHandler() {
        // Get stuck here sometime and somehow. (╯>д<)╯⁽˙³˙⁾ SUCK!!!
        // Work well with 7.1.1, but stuck with 4.1.1.
        // Maybe due to the difference native implementation of MessageQueue.
        String foo = FooWithHandler.foo();
        Assert.assertEquals("HELLO", foo);
    }

    @Test
    public void testStaticSynchronousWorkerThreadWithHandler() throws InterruptedException {
        final String[] foo = new String[1];
        Runnable runnable = new Runnable() {
            public void run() {
                // Get stuck here sometime and somehow. (╯>д<)╯⁽˙³˙⁾ SUCK!!!
                foo[0] = FooWithHandler.foo();
                synchronized (StaticPuzzle.class) {
                    StaticPuzzle.class.notify();
                }
            }
        };

        new Thread(runnable).start();

        synchronized (StaticPuzzle.class) {
            StaticPuzzle.class.wait();
        }
        Assert.assertEquals("HELLO", foo[0]);
    }

    static class Foo {
        private static final String OBJECT;
        static {
            final String[] o = {null};
            new Thread("worker-1"){
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        o[0] = "HELLO";
                        synchronized (Foo.class) {
                            Foo.class.notify();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            synchronized (Foo.class) {
                try {
                    // Wait for worker-1 to get OBJECT;
                    Foo.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            OBJECT = o[0];
        }

        static String foo() {
            return OBJECT;
        }
    }

    static class FooWithHandler {
        private static final String OBJECT;
        static {
            final String[] o = {null};
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    o[0] = "HELLO";
                    synchronized (Foo.class) {
                        Foo.class.notify();
                    }
                }
            });

            synchronized (Foo.class) {
                try {
                    // Wait for handler to get OBJECT;
                    Foo.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            OBJECT = o[0];
        }

        static String foo() {
            return OBJECT;
        }
    }
}
