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
import java.util.concurrent.Executor;

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

    /**
     * test api
     * {@link Task#call(Callable)}
     * {@link Task#getResult()}
     */
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

    /**
     * test api
     * {@link Task#callInBackground(Callable)}
     * {@link Task#waitForCompletion()}
     */
    public void testCallInBackground() {
        Task<Integer> integerTask = Task.callInBackground(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                // assert in background
                assertTrue(Looper.myLooper() == null);
                Thread.sleep(1000);
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

    /**
     * test api
     * {@link Task#call(Callable, Executor)}
     */
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

    /**
     * {@link Task#continueWith(Continuation)}
     * {@link Continuation#then(Task)}
     */
    public void testContinueWith() {
        String result = Task.call(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 10086;
            }
        }).continueWith(new Continuation<Integer, String>() {
            @Override
            public String then(Task<Integer> task) throws Exception {
                if (task.isFaulted()) {
                    // fail
                    return "fail";
                }
                if (task.isCancelled()) {
                    // canceled
                    return "canceled";
                }
                Integer integer = task.getResult();
                return "success get " + integer;
            }
        }).getResult();

        assertEquals(result, "success get 10086");
    }

    /**
     * equal with {@link BoltsTest#testContinueWith}
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
                if (task.isFaulted()) {
                    // fail
                    return "fail";
                }
                if (task.isCancelled()) {
                    // canceled
                    return "canceled";
                }
                Integer integer = task.getResult();
                return "success get " + integer;
            }
        };

        Task<Integer> integerTask = Task.call(integerCallable);
        Task<String> stringTask = integerTask.continueWith(stringContinuation);
        String result = stringTask.getResult();
        assertTrue(!TextUtils.isEmpty(result));
    }

    /**
     * {@link Task#onSuccess(Continuation)}
     */
    public void testOnSuccess() {
        String result = Task.call(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 10086;
            }
        }).onSuccess(new Continuation<Integer, String>() {
            @Override
            public String then(Task<Integer> task) throws Exception {
                if (task.isFaulted()) {
                    // fail
                    return "fail";
                }
                if (task.isCancelled()) {
                    // canceled
                    return "canceled";
                }
                Integer integer = task.getResult();
                return "success get " + integer;
            }
        }).getResult();

        assertEquals(result, "success get 10086");

        result = Task.call(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                throw new RuntimeException("fail");
            }
        }).onSuccess(new Continuation<Integer, String>() {
            @Override
            public String then(Task<Integer> task) throws Exception {
                if (task.isFaulted()) {
                    // fail
                    return "fail";
                }
                if (task.isCancelled()) {
                    // canceled
                    return "canceled";
                }
                Integer integer = task.getResult();
                return "success get " + integer;
            }
        }).getResult();

        assertEquals(result, null);
    }
}
