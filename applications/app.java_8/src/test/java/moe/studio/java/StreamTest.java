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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Snippet with {@link Stream} api manipulations.
 *
 * @author Kaede
 * @since 17/9/3
 */
@RunWith(JUnit4.class)
public class StreamTest {
    private static final List<Integer> INT_ARRAYS = Arrays.asList(1, 2, 3, 4);
    private static final List<String> TEXT_ARRAYS = Arrays.asList("hello", "functional", "coding");

    @Test
    public void testReduce() {
        Stream<Integer> intStream = Stream.of(1, 2, 3, 4);
        int acc = intStream.reduce(0, (i, j) -> i + j);
        Assert.assertEquals(acc, 1 + 2 + 3 + 4);

        Stream<String> textStream = Stream.of("hello", "world", "!");
        String combine = textStream.reduce("echo", (i, j) -> i + j);
        Assert.assertEquals(combine, "echo" + "hello" + "world" + "!");

        intStream = Stream.of(1, 2, 3, 4);
        int max = intStream.reduce(0, BinaryOperator.maxBy(Integer::compareTo));
        Assert.assertEquals(max, 4);

        textStream = Stream.of("hello", "world", "!");
        Optional<String> min = textStream.reduce(BinaryOperator.minBy((o1, o2) -> {
            if (o1.equals("world")) return -1;
            if (o2.equals("world")) return 1;
            return 0;
        }));
        Assert.assertTrue(min.isPresent());
        Assert.assertEquals(min.get(), "world");
    }

    @Test
    public void testCollect() {
        Stream<Integer> intStream = Stream.of(1, 2, 3, 4);
        List<Integer> list = intStream.collect(Collectors.toList());
        Assert.assertEquals(list, Arrays.asList(1, 2, 3, 4));

        Set<Integer> set = Stream.of(1, 2, 3, 3).collect(Collectors.toSet());
        Assert.assertTrue(set.contains(1));
        Assert.assertTrue(set.contains(2));
        Assert.assertTrue(set.contains(3));
        Assert.assertFalse(set.contains(4));
        Assert.assertEquals(set.size(), 3);
    }

    @Test
    public void testMap() {
        Stream<Integer> intStream = Stream.of(1, 2, 3, 4);
        List<String> list = intStream.map(String::valueOf).collect(Collectors.toList());
        Assert.assertEquals(list, Arrays.asList("1", "2", "3", "4"));
    }

    @Test
    public void testFlatMap() {
        List<Integer> list1 = Arrays.asList(1, 2);
        List<Integer> list2 = Arrays.asList(1, 2, 3);
        List<Integer> list3 = Arrays.asList(1, 2, 3, 4);
        //noinspection Convert2MethodRef
        Stream<Integer> intStream = Stream.of(list1, list2, list3)
                .flatMap(integers -> integers.stream());
        List<Integer> collect = intStream.sorted().collect(Collectors.toList());
        Assert.assertEquals(collect, Arrays.asList(1, 1, 1, 2, 2, 2, 3, 3, 4));
    }

    @Test
    public void testParallel() {
        List<Integer> integerList = Stream.of(1, 2, 3, 4)
                .parallel()
                .filter(integer -> integer % 2 == 1)
                .collect(Collectors.toList());
        Assert.assertEquals(integerList, Arrays.asList(1, 3));

        integerList = INT_ARRAYS.parallelStream()
                .filter(integer -> integer > 1)
                .collect(Collectors.toList());
        Assert.assertEquals(integerList, Arrays.asList(2, 3, 4));

        int[] ints = new int[4];
        Arrays.parallelSetAll(ints, operand -> operand);
        Assert.assertEquals(
                Arrays.stream(ints).boxed().collect(Collectors.toList()),
                Arrays.asList(0, 1, 2, 3)
        );

        Integer[] integers = new Integer[4];
        Arrays.parallelSetAll(integers, operand -> operand);
        Arrays.parallelPrefix(integers, (integer1, integer2) -> integer1 + integer2);
        Assert.assertEquals(
                new ArrayList<>(Arrays.asList(integers)),
                Arrays.asList(0, 1, 3, 6)
        );
    }
}
