/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.aysnc;

import android.content.Context;
import android.os.Looper;
import android.test.InstrumentationTestCase;
import android.text.TextUtils;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Bolts is a collection of low-level libraries designed to make developing mobile apps easier.
 * {@link "https://github.com/BoltsFramework/Bolts-Android"}
 *
 * @author kaede
 * @version date 16/8/26
 */
public class BoltsTest extends InstrumentationTestCase {

    public static final String TAG = "Bolts";

    Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    public void testCall() {
        Task<Integer> integerTask = Task.call(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 10086;
            }
        });
        int result = integerTask.getResult();
        assertTrue(result == 10086);
    }

    public void testCallInBackground() {
        Task<Integer> integerTask = Task.callInBackground(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {

                // assert in background
                assertTrue(Looper.myLooper() == null);
                return 10086;
            }
        });

        assertTrue(integerTask.getResult() == null);

        try {
            integerTask.waitForCompletion();
            assertTrue(integerTask.getResult() == 10086);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testCallWithExecutor() {
        Task<Integer> integerTask = Task.call(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 10086;
            }
        }, Task.UI_THREAD_EXECUTOR);

        assertTrue(integerTask.getResult() == null);

        try {
            integerTask.waitForCompletion();
            assertTrue(integerTask.getResult() == 10086);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testContinueWith1() {
        String result = Task.call(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 10086;
            }
        }).continueWith(new Continuation<Integer, String>() {
            @Override
            public String then(Task<Integer> task) throws Exception {
                Integer integer = task.getResult();
                return "on get " + integer;
            }
        }).getResult();

        assertTrue(!TextUtils.isEmpty(result));
    }

    /**
     * equal with {@link BoltsTest#testContinueWith1}
     */
    public void testContinueWith2() {
        Callable<Integer> integerCallable = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 10086;
            }
        };

        Continuation<Integer, String> stringContinuation = new Continuation<Integer, String>() {
            @Override
            public String then(Task<Integer> task) throws Exception {
                Integer integer = task.getResult();
                return "on get " + integer;
            }
        };

        Task<Integer> integerTask = Task.call(integerCallable);
        Task<String> stringTask = integerTask.continueWith(stringContinuation);
        String result = stringTask.getResult();
        assertTrue(!TextUtils.isEmpty(result));
    }
}
