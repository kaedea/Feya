/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package moe.studio.java;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
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
        Func.foo(() -> System.out.print(""));
        Func.foo(text -> Assert.assertEquals(text, "text"));
        Func.foo((text, i) -> {
            Assert.assertEquals(text, "text");
            Assert.assertEquals(i, 10086);
        });
        String result = Func.foo((text, i) -> {
            Assert.assertEquals(text, "text");
            Assert.assertEquals(i, 10086);
            return text + i;
        });
        Assert.assertEquals(result, "text" + 10086);
    }
}
