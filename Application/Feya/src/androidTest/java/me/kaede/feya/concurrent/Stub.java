/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.concurrent;

import android.os.Handler;
import android.os.Looper;

/**
 * @author Kaede
 * @since 17/4/10
 */
class Stub {

    private static String FOO;

    static String getFoo() {
        if (FOO == null) {
            synchronized (Stub.class) {
                if (FOO != null) return FOO;

                final String[] o = {null};
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        o[0] = "HELLO";
                        synchronized (Stub.class) {
                            Stub.class.notify();
                        }
                    }
                });

                synchronized (Stub.class) {
                    try {
                        // Wait for handler to get OBJECT;
                        Stub.class.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                FOO = o[0];
            }
        }

        return FOO;
    }
}
