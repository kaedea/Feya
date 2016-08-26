/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.aysnc;

import android.content.Context;
import android.os.Looper;
import android.test.InstrumentationTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import bolts.Continuation;
import bolts.Task;

/**
 * Bolts api demo
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
        assertEquals(result, "success get 10086");
    }

    /**
     * {@link Task#delay(long)}
     */
    public void testDelayContinueWith() {
        final long millis = System.currentTimeMillis();
        try {
            Task.delay(3000).continueWith(new Continuation<Void, Object>() {
                @Override
                public Object then(Task<Void> task) throws Exception {
                    assertTrue((System.currentTimeMillis() - millis) >= 3000);
                    return null;
                }
            }).waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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


    public void testLooperTask() {
        final AtomicInteger count = new AtomicInteger(0);

        try {
            Task.forResult(null).continueWhile(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return count.get() < 10;
                }
            }, new Continuation<Void, Task<Void>>() {
                @Override
                public Task<Void> then(Task<Void> task) throws Exception {
                    count.incrementAndGet();
                    return null;
                }
            }).continueWith(new Continuation<Void, Object>() {
                @Override
                public Object then(Task<Void> task) throws Exception {
                    assertEquals(10, count.get());
                    return null;
                }
            }).waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * execute parallel tasks.
     * callback when all tasks are done.
     * {@link Task#whenAll(Collection)}
     */
    public void testParallelTask() {
        Task<Integer> task1 = Task.callInBackground(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 10086;
            }
        });

        Task<Integer> task2 = Task.callInBackground(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 65535;
            }
        });

        final List<Task<?>> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        try {
            // parallel call both task, and wait for both tasks done
            Task.whenAll(tasks).continueWith(new Continuation<Void, Object>() {
                @Override
                public Object then(Task<Void> task) throws Exception {
                    // task1 and task2 are both finished
                    for (int i = 0; i < tasks.size(); i++) {
                        Task<?> item = tasks.get(i);
                        assertTrue(item.getResult().equals(10086) || item.getResult().equals(65535));
                    }
                    return null;
                }
            }).waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * execute parallel tasks.
     * callback when any task is done.
     * {@link Task#whenAny(Collection)}
     */
    public void testParallelTask2() {
        Task<Integer> task1 = Task.callInBackground(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 10086;
            }
        });

        Task<Integer> task2 = Task.callInBackground(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 65535;
            }
        });

        final List<Task<?>> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        try {
            Task.whenAny(tasks).continueWith(new Continuation<Task<?>, Object>() {
                @Override
                public Object then(Task<Task<?>> task) throws Exception {
                    Task<?> taskFinished = task.getResult();
                    assertTrue(taskFinished.getResult().equals(10086) || taskFinished.getResult().equals(65535));
                    return null;
                }
            }).waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
