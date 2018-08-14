/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package moe.studio.java;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Test snippet with lambdas & {@link FunctionalInterface}.
 *
 * @author Kaede
 * @since 17/9/2
 */
@RunWith(JUnit4.class)
public class LambdaTest {

    /**
     * Interface static methods.
     */
    static class Func {
        static void foo(ICustomFunc func) {
            func.foo();
        }

        static void foo(ICustomFuncWithArg func) {
            func.foo("text");
        }

        static void foo(ICustomFuncWithArgs func) {
            func.foo("text", 10086);
        }

        static String foo(ICustomFuncWithReturn func) {
            return func.foo("text", 10086);
        }

        static boolean fooPredicate(Predicate<String> predicate) {
            return predicate.test("text");
        }

        static void fooConsumer(Consumer<String> consumer) {
            consumer.accept("text");
        }

        static int fooFunction(Function<String, Integer> function) {
            return function.apply("text");
        }

        static int fooSupplier(Supplier<Integer> supplier) {
            return supplier.get();
        }

        static boolean fooUnaryOperator(UnaryOperator<Boolean> unaryOperator) {
            return unaryOperator.apply(false);
        }

        static Integer fooBinaryOperator(BinaryOperator<Integer> binaryOperator) {
            return binaryOperator.apply(10086, 10010);
        }
    }

    @FunctionalInterface
    interface ICustomFunc {
        void foo();
    }

    @FunctionalInterface
    interface ICustomFuncWithArg {
        void foo(String text);
    }

    @FunctionalInterface
    interface ICustomFuncWithArgs {
        void foo(String text, int i);
    }

    @FunctionalInterface
    interface ICustomFuncWithReturn {
        String foo(String text, int i);
    }

    @Test
    public void testFunctionInterface() {
        // Custom FunctionInterface
        Func.foo(() -> System.out.print(""));
        Func.foo(text -> Assert.assertEquals(text, "text"));
        Func.foo((text, i) -> {
            Assert.assertEquals(text, "text");
            Assert.assertEquals(i, 10086);
        });
        String result1 = Func.foo((text, i) -> {
            Assert.assertEquals(text, "text");
            Assert.assertEquals(i, 10086);
            return text + i;
        });
        Assert.assertEquals(result1, "text" + 10086);

        // Predicate
        boolean predicate = Func.fooPredicate(text -> text.equals("text"));
        Assert.assertTrue(predicate);
        predicate = Func.fooPredicate(text -> text.equals("10086"));
        Assert.assertFalse(predicate);

        // Consumer
        Func.fooConsumer(text -> Assert.assertEquals(text, "text"));

        // Function
        int result2 = Func.fooFunction(text -> {
            Assert.assertEquals(text, "text");
            return 10086;
        });
        Assert.assertEquals(result2, 10086);

        // Supplier
        result2 = Func.fooSupplier(() -> 10086);
        Assert.assertEquals(result2, 10086);

        // UnaryOperator
        Assert.assertTrue(Func.fooUnaryOperator(bool -> !bool));
        Assert.assertFalse(Func.fooUnaryOperator(UnaryOperator.identity()));

        // BinaryOperator
        result2 = Func.fooBinaryOperator((i, j) -> i + j);
        Assert.assertEquals(result2, 10086 + 10010);
        result2 = Func.fooBinaryOperator(BinaryOperator.minBy(Integer::compareTo));
        Assert.assertEquals(result2, 10010);
        result2 = Func.fooBinaryOperator(BinaryOperator.maxBy(Integer::compareTo));
        Assert.assertEquals(result2, 10086);
    }
}
