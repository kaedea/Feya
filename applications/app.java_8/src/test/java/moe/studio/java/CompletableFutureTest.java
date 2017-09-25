/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package moe.studio.java;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Snippets of {@link java.util.concurrent.CompletableFuture}.
 *
 * @author Kaede
 * @since 17/9/10
 */
@RunWith(JUnit4.class)
public class CompletableFutureTest {

    @Test
    public void testCreateCompletableFuture() throws ExecutionException, InterruptedException {
        // CompletableFuture::new, CompletableFuture#completedFuture
        CompletableFuture<Integer> emptyFuture = new CompletableFuture<>();
        CompletableFuture<Integer> completedFuture = CompletableFuture.completedFuture(10086);
        Assert.assertFalse(emptyFuture.isDone());
        Assert.assertTrue(completedFuture.isDone());

        // CompletableFuture#anyOf, allOf
        CompletableFuture anyFuture = CompletableFuture.anyOf(emptyFuture, completedFuture);
        CompletableFuture allFuture = CompletableFuture.allOf(emptyFuture, completedFuture);
        Assert.assertTrue(anyFuture.isDone());
        Assert.assertFalse(allFuture.isDone());

        // CompletableFuture#supplyAsync, runAsync
        CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(() -> "CompletableFuture");
        Assert.assertEquals("CompletableFuture", supplyAsync.get());
        AtomicReference<String> reference = new AtomicReference<>();
        CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> reference.set("CompletableFuture"));
        runAsync.get();
        Assert.assertEquals("CompletableFuture", reference.get());
    }

    @Test
    public void testTerminateCompletableFuture() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> intFuture;

        // CompletableFuture#complete
        intFuture = new CompletableFuture<>();
        CompletableFuture<String> thenApplyFuture = intFuture.thenApply(Object::toString);
        Assert.assertFalse(intFuture.isDone());
        Assert.assertFalse(thenApplyFuture.isDone());
        intFuture.complete(0);
        Assert.assertTrue(intFuture.isDone());
        Assert.assertTrue(thenApplyFuture.isDone());
        Assert.assertEquals("0", thenApplyFuture.get());
        intFuture.complete(1);
        Assert.assertTrue(intFuture.isDone());
        Assert.assertTrue(thenApplyFuture.isDone());
        Assert.assertEquals("0", thenApplyFuture.get());

        // CompletableFuture#completeExceptionally
        intFuture = new CompletableFuture<>();
        CompletableFuture<Integer> exceptionFuture = intFuture.exceptionally(throwable -> 10086);
        intFuture.completeExceptionally(new RuntimeException());
        Assert.assertTrue(intFuture.isCompletedExceptionally());
        Assert.assertEquals(10086, exceptionFuture.get().intValue());
        // CompletableFuture#cancel
        intFuture = new CompletableFuture<>();
        Assert.assertFalse(intFuture.isCancelled());
        intFuture.cancel(false);
        Assert.assertTrue(intFuture.isCancelled());

        // CompletableFuture#obtrudeValue, obtrudeException
        final int[] completeAcc = {0};
        final int[] exceptionAcc = {0};
        intFuture = new CompletableFuture<>();
        intFuture.thenApply(integer -> completeAcc[0] += integer);
        intFuture.exceptionally(integer -> exceptionAcc[0]++);
        intFuture.obtrudeValue(10);
        Assert.assertTrue(intFuture.isDone());
        Assert.assertEquals(10, completeAcc[0]);
        Assert.assertEquals(0, exceptionAcc[0]);
        intFuture.obtrudeValue(20);
        intFuture.obtrudeException(new RuntimeException());
        Assert.assertTrue(intFuture.isDone());
        Assert.assertEquals(10, completeAcc[0]);
        Assert.assertEquals(0, exceptionAcc[0]);

        // CompletableFuture#get, getNow
        CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
            return "supplyAsync";
        });
        Assert.assertFalse(supplyAsync.isDone());
        Assert.assertEquals("absent", supplyAsync.getNow("absent"));
        Assert.assertEquals("supplyAsync", supplyAsync.get());
        // CompletableFuture#join
        // 'join' is similar to 'get' but throws unchecked exceptions.
        supplyAsync = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
            return "supplyAsync";
        });
        Assert.assertFalse(supplyAsync.isDone());
        Assert.assertEquals("absent", supplyAsync.getNow("absent"));
        Assert.assertEquals("supplyAsync", supplyAsync.join());
    }

    @Test
    public void testCombineCompletableFuture() throws ExecutionException, InterruptedException {
        AtomicInteger intSupply = new AtomicInteger();
        AtomicReference<String> textSupply = new AtomicReference<>();
        CompletableFuture<Integer> intFuture;
        CompletableFuture<Integer> intFuture2;
        CompletableFuture<String> textFuture;

        // CompletableFuture#thenApply
        // T -> U
        intFuture = new CompletableFuture<>();
        CompletableFuture<String> thenApplyFuture = intFuture.thenApply(Object::toString);
        intFuture.complete(10086);
        Assert.assertEquals("10086", thenApplyFuture.get());
        // CompletableFuture#applyToEither
        intFuture = new CompletableFuture<>();
        intFuture2 = new CompletableFuture<>();
        CompletableFuture<String> applyToEither = intFuture.applyToEither(intFuture2, Object::toString);
        intFuture.complete(10010);
        Assert.assertEquals("10010", applyToEither.get());
        intFuture = new CompletableFuture<>();
        intFuture2 = new CompletableFuture<>();
        applyToEither = intFuture.applyToEither(intFuture2, Object::toString);
        intFuture.complete(10086);
        Assert.assertEquals("10086", applyToEither.get());

        // CompletableFuture#thenAccept
        // T -> void
        intSupply.set(0);
        intFuture = new CompletableFuture<>();
        CompletableFuture<Void> thenAccept = intFuture.thenAccept(intSupply::set);
        intFuture.complete(10010);
        Assert.assertEquals(10010, intSupply.get());
        Assert.assertEquals(null, thenAccept.get()); // We can not get instance of Void.
        // CompletableFuture#acceptEither
        intSupply.set(0);
        intFuture = new CompletableFuture<>();
        intFuture2 = new CompletableFuture<>();
        intFuture.acceptEither(intFuture2, intSupply::set);
        intFuture.complete(10010);
        Assert.assertEquals(10010, intSupply.get());
        intSupply.set(0);
        intFuture = new CompletableFuture<>();
        intFuture2 = new CompletableFuture<>();
        intFuture.acceptEither(intFuture2, intSupply::set);
        intFuture2.complete(10086);
        Assert.assertEquals(10086, intSupply.get());
        intSupply.set(0);
        // CompletableFuture#thenAcceptBoth
        intFuture = new CompletableFuture<>();
        intFuture2 = new CompletableFuture<>();
        intFuture.thenAcceptBoth(intFuture2, (t, u) -> intSupply.set(t + u));
        intFuture.complete(10010);
        Assert.assertEquals(0, intSupply.get());
        intFuture2.complete(10086);
        Assert.assertEquals(10010 + 10086, intSupply.get());

        // CompletableFuture#thenRun
        // T -> void
        intFuture = new CompletableFuture<>();
        intFuture.thenRun(() -> textSupply.set("10086"));
        intFuture.complete(10010);
        Assert.assertEquals("10086", textSupply.get());
        // CompletableFuture#runAfterBoth
        textSupply.set(null);
        intFuture = new CompletableFuture<>();
        textFuture = new CompletableFuture<>();
        intFuture.runAfterBoth(textFuture, () -> textSupply.set("10086"));
        intFuture.complete(10010);
        Assert.assertEquals(null, textSupply.get());
        textFuture.complete("10010");
        Assert.assertEquals("10086", textSupply.get());
        // CompletableFuture#runAfterEither
        textSupply.set(null);
        intFuture = new CompletableFuture<>();
        textFuture = new CompletableFuture<>();
        intFuture.runAfterEither(textFuture, () -> textSupply.set("10086"));
        intFuture.complete(10010);
        Assert.assertEquals("10086", textSupply.get());

        // CompletableFuture#thenCompose
        // T -> U
        intFuture = new CompletableFuture<>();
        CompletableFuture<Integer> finalEmptyFuture = intFuture;
        CompletableFuture<String> thenCompose = intFuture.thenCompose(
                integer -> finalEmptyFuture.thenApply(Object::toString)
        );
        intFuture.complete(10086);
        Assert.assertEquals("10086", thenCompose.get());

        // CompletableFuture#thenCombine
        // T, U -> V
        intFuture = new CompletableFuture<>();
        CompletableFuture<String> thenCombine = intFuture.thenCombine(
                intFuture.thenApply(Object::toString),
                (a, b) -> a + b
        );
        intFuture.complete(65535);
        Assert.assertEquals(65535 + "65535", thenCombine.get());

        // CompletableFuture#whenComplete
        // T, E -> void
        intFuture = new CompletableFuture<>();
        intSupply.set(0);
        textSupply.set(null);
        intFuture.whenComplete((integer, throwable) -> intSupply.set(integer));
        intFuture.whenComplete((integer, throwable) -> textSupply.set(throwable.getMessage()));
        intFuture.complete(10086);
        Assert.assertEquals(10086, intSupply.get());
        Assert.assertEquals(null, textSupply.get());
        intFuture = new CompletableFuture<>();
        intSupply.set(0);
        intFuture.whenComplete((integer, throwable) -> intSupply.set(integer));
        intFuture.whenComplete((integer, throwable) -> textSupply.set(throwable.getMessage()));
        intFuture.completeExceptionally(new RuntimeException("10010"));
        Assert.assertEquals(0, intSupply.get());
        Assert.assertEquals("10010", textSupply.get());

        // CompletableFuture#getNumberOfDependents
        intFuture = new CompletableFuture<>();
        intFuture.thenRun(() -> textSupply.set("10086"));
        Assert.assertEquals(1, intFuture.getNumberOfDependents());
        intFuture.thenRun(() -> textSupply.set("10086"));
        intFuture.thenRun(() -> textSupply.set("10086"));
        intFuture.thenRun(() -> textSupply.set("10086"));
        intFuture.thenRun(() -> textSupply.set("10086"));
        Assert.assertEquals(1 + 4, intFuture.getNumberOfDependents());
    }

    @Test
    public void testCompletableFutureExceptions() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> intFuture;
        AtomicReference<String> textSupply = new AtomicReference<>();

        // CompletableFuture#exceptionally
        intFuture = new CompletableFuture<>();
        CompletableFuture<Integer> exceptionFuture = intFuture.exceptionally(throwable -> 10086);
        intFuture.completeExceptionally(new RuntimeException());
        Assert.assertTrue(intFuture.isCompletedExceptionally());
        Assert.assertEquals(10086, exceptionFuture.get().intValue());

        // CompletableFuture#handle
        // T, E -> U
        intFuture = new CompletableFuture<>();
        CompletableFuture<String> handle = intFuture.handle(
                (integer, throwable) -> throwable.getLocalizedMessage()
        );
        intFuture.completeExceptionally(new RuntimeException("10010"));
        Assert.assertEquals("10010", handle.get());

        intFuture = new CompletableFuture<>();
        textSupply.set("");
        CompletableFuture<Integer> handleFuture = intFuture.handle((integer, throwable) -> {
            if (throwable != null) {
                textSupply.set(textSupply.get() + "handle");
                return -1;
            }
            return integer * 2;
        });
        intFuture.exceptionally(throwable -> {
            textSupply.set(textSupply.get() + "exceptionally");
            return -1;
        });
        intFuture.complete(10086);
        Assert.assertEquals("", textSupply.get());
        Assert.assertEquals(10086 * 2, handleFuture.get().intValue());
        intFuture = new CompletableFuture<>();
        textSupply.set("");
        handleFuture = intFuture.handle((integer, throwable) -> {
            if (throwable != null) {
                textSupply.set(textSupply.get() + "handle");
                return -1;
            }
            return integer * 2;
        });
        intFuture.exceptionally(throwable -> {
            textSupply.set(textSupply.get() + "exceptionally");
            return -1;
        });
        intFuture.completeExceptionally(new RuntimeException());
        Assert.assertEquals("exceptionally" + "handle", textSupply.get());
        Assert.assertEquals(-1, handleFuture.get().intValue());
    }
}
