/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com).
 */

package me.kaede.feya.aysnc;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import bolts.AggregateException;
import bolts.CancellationToken;
import bolts.CancellationTokenSource;
import bolts.Capture;
import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Bolts api
 * Bolts is a collection of low-level libraries designed to make developing mobile apps easier.
 * see {@link "https://github.com/BoltsFramework/Bolts-Android"}
 * see api test & example code in {@link BoltsApiTest}
 * see official api test in {@link BoltsApiTest}
 *
 * @author kaede
 * @version date 16/8/31 bolts 1.4.0
 */
public class BoltsApis {
    /**
     * bolts.Task
     * Represents the result of an asynchronous operation.
     *
     * 1. get a Task instance (represent an operation or function)
     *
     * {@link Task#call(Callable)}
     * {@link Task#call(Callable, Executor)}
     * {@link Task#call(Callable, CancellationToken)}
     * {@link Task#call(Callable, Executor, CancellationToken)}
     *
     * {@link Task#callInBackground(Callable)}
     * {@link Task#callInBackground(Callable, CancellationToken)}
     *
     * {@link Task#continueWith(Continuation)}
     * {@link Task#continueWith(Continuation, Executor)}
     * {@link Task#continueWith(Continuation, CancellationToken)}
     * {@link Task#continueWith(Continuation, Executor, CancellationToken)}
     * {@link Task#continueWith(Continuation, Executor, CancellationToken)}
     *
     * {@link Task#continueWithTask(Continuation)}
     * {@link Task#continueWithTask(Continuation, Executor)}
     * {@link Task#continueWithTask(Continuation, CancellationToken)}
     * {@link Task#continueWithTask(Continuation, Executor, CancellationToken)}
     *
     * {@link Task#onSuccess(Continuation)}
     * {@link Task#onSuccess(Continuation, Executor)}
     * {@link Task#onSuccess(Continuation, CancellationToken)}
     * {@link Task#onSuccess(Continuation, Executor, CancellationToken)}
     *
     * {@link Task#onSuccessTask(Continuation)}
     * {@link Task#onSuccessTask(Continuation, Executor)}
     * {@link Task#onSuccessTask(Continuation, CancellationToken)}
     * {@link Task#onSuccessTask(Continuation, Executor, CancellationToken)}
     *
     * {@link Task#continueWhile(Callable, Continuation)}
     * {@link Task#continueWhile(Callable, Continuation, Executor)}
     * {@link Task#continueWhile(Callable, Continuation, CancellationToken)}
     * {@link Task#continueWhile(Callable, Continuation, Executor, CancellationToken)}
     *
     * 2. wait for a task's result
     *
     * {@link Task#waitForCompletion()}
     * {@link Task#waitForCompletion(long, TimeUnit)}
     *
     * {@link Task#whenAll(Collection)}
     * {@link Task#whenAllResult(Collection)}
     *
     * {@link Task#whenAny(Collection)}
     * {@link Task#whenAnyResult(Collection)}
     *
     * 3. get a simple Task instance from result
     *
     * {@link Task#forResult(Object)}
     * {@link Task#forError(Exception)}
     * {@link Task#cancelled()}
     *
     * {@link Task#cast()}
     * {@link Task#makeVoid()}
     *
     * 4. access a task's fields
     *
     * {@link Task#isCompleted()}
     * {@link Task#isFaulted()}
     * {@link Task#isCancelled()}
     *
     * {@link Task#getResult()}
     * {@link Task#getError()}
     *
     * 5. listen task's fail event
     *
     * {@link Task#setUnobservedExceptionHandler(Task.UnobservedExceptionHandler)}
     *
     * 6. others
     *
     * {@link Task#delay(long)}
     * {@link Task#delay(long, CancellationToken)}
     *
     * {@link Task#setUnobservedExceptionHandler(Task.UnobservedExceptionHandler)}
     * {@link Task#getUnobservedExceptionHandler()}
     *
     * {@link Task#create()}
     */


    /**
     * bolts.Continuation
     * A function to be called after a task completes.
     *
     * {@link Continuation#then(Task)}
     * */


    /**
     * bolts.AggregateException
     * Aggregate the errors when 2 or more task failed in multi-task operation.
     *
     * {@link AggregateException#getInnerThrowables()}
     * */


    /**
     * bolts.TaskCompletionSource
     * It represents the producer side of a Task<TResult>.
     *
     * 1. get an empty task instance
     * {@link TaskCompletionSource#getTask()}
     *
     * 2. complete an task
     *
     * {@link TaskCompletionSource#setResult(Object)}
     * {@link TaskCompletionSource#setCancelled()}
     * {@link TaskCompletionSource#setError(Exception)}
     * */


    /**
     * bolts.CancellationToken
     * bolts.CancellationTokenSource
     * bolts.CancellationTokenRegistration
     * Cancel a task and notify the observers that the task is canceled.
     *
     * 1. cancel a task
     *
     * {@link CancellationTokenSource#cancel()}
     * {@link CancellationTokenSource#cancelAfter(long)}
     *
     * 2. check if a task is canceled in an observer
     *
     * {@link CancellationToken#isCancellationRequested()}
     *
     * 3. do something when a task is canceled in an observer
     *
     * {@link CancellationToken#register(Runnable)}
     * */


    /**
     * bolts.Capture
     * Capture variables inner different variable scope of tasks.
     *
     * {@link Capture#get()}
     * {@link Capture#set(Object)}
     */
}
