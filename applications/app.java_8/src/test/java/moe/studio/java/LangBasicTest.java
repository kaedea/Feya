/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package moe.studio.java;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kaede
 * @since 17/9/3
 */
@RunWith(JUnit4.class)
public class LangBasicTest {

    interface IFunc {
        default int foo() {
            return 10086;
        }
    }

    @Test
    public void testDefaultMethod() {
        Assert.assertEquals(new IFunc() {}.foo(), 10086);
        Assert.assertEquals(new IFunc() {
            @Override
            public int foo() {
                return 10010;
            }
        }.foo(), 10010);
    }

    @Test
    public void testMethodReference() {
        // Static Method
        List<String> list = Stream.of(1, 2, 3)
                .map(String::valueOf) // Equals 'x -> String.valueOf(x)'
                .collect(Collectors.toList());
        Assert.assertEquals(list, Arrays.asList("1", "2", "3"));

        Function<Object, String> method = String::valueOf;
        Assert.assertEquals(method.apply(1), "1");
        Assert.assertEquals(method.apply("2"), "2");
        Assert.assertEquals(method.apply(3.14f), "3.14");

        // Instance Method
        IntFunction<String> intToString = Integer::toString;
        Assert.assertEquals(intToString.apply(10086), "10086");

        Integer integer = 10086;
        IntSupplier supplier = integer::intValue;
        Assert.assertEquals(supplier.getAsInt(), 10086);

        // Constructor Method
        Function<String, Integer> intConstructor = Integer::new;
        integer = intConstructor.apply("10086");
        Assert.assertEquals(integer, Integer.valueOf("10086"));
        Assert.assertEquals(integer.intValue(), 10086);

        // Combined Usage
        Supplier<ArrayList> listSupplier = ArrayList<Integer>::new;
        ArrayList arrayList = listSupplier.get();
        Consumer<Integer> listAdd1 = arrayList::add;
        listAdd1.accept(1);
        listAdd1.accept(2);
        Assert.assertEquals(arrayList, Arrays.asList(1, 2));
        BiPredicate<ArrayList, Integer> listAdd2 = ArrayList::add;
        listAdd2.test(arrayList, 3);
        Assert.assertEquals(arrayList, Arrays.asList(1, 2, 3));
        Function<ArrayList, String> listToString = ArrayList::toString;
        String text = listToString.apply(arrayList);
        Assert.assertEquals(text, Arrays.asList(1, 2, 3).toString());
    }

    @Test
    @SuppressWarnings("UseSparseArrays")
    public void testMapApi() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "1");
        map.put(2, "2");
        map.put(3, "3");

        Assert.assertEquals(map.getOrDefault(1, "default"), "1");
        Assert.assertEquals(map.getOrDefault(4, "default"), "default");
        Assert.assertEquals(map.computeIfAbsent(4, Object::toString), "4");
        Assert.assertEquals(map.getOrDefault(4, "default"), "4");

        Assert.assertEquals(map.compute(1, (integer, s) -> {
            Assert.assertEquals(s, "1");
            return "new 1";
        }), "new 1");
        Assert.assertEquals(map.getOrDefault(1, "default"), "new 1");

        Assert.assertEquals(map.compute(5, (integer, s) -> {
            Assert.assertEquals(s, null);
            return "5";
        }), "5");
        Assert.assertEquals(map.getOrDefault(5, "default"), "5");
    }
}
