/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.concurrent;

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
            }
        };

        new Thread(runnable).start();

        Thread.sleep(3000);
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
