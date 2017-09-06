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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
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
    public void testCreateStream() {
        // 1. Stream#of
        Stream<Integer> intStream1 = Stream.of(1, 2, 3, 4);
        Assert.assertNotNull(intStream1);
        List<Integer> list1 = intStream1.collect(Collectors.toList());
        // 2. Arrays#stream
        Stream<Integer> intStream2 = Arrays.stream(new Integer[]{1, 2, 3, 4});
        Assert.assertNotNull(intStream2);
        List<Integer> list2 = intStream2.collect(Collectors.toList());
        // 3. Stream#generate, infinite sequential unordered stream
        final AtomicInteger autoInt = new AtomicInteger(1);
        Stream<Integer> intStream3 = Stream.generate(autoInt::getAndIncrement).limit(4);
        Assert.assertNotNull(intStream3);
        List<Integer> list3 = intStream3.collect(Collectors.toList());
        // 4. Stream#iterate, infinite sequential ordered stream
        Stream<Integer> intStream4 = Stream.iterate(1, integer -> integer + 1).limit(4);
        Assert.assertNotNull(intStream4);
        List<Integer> list4 = intStream4.collect(Collectors.toList());
        // 5. Collection#stream (instance method)
        Stream<Integer> intStream5 = INT_ARRAYS.stream();
        Assert.assertNotNull(intStream5);
        List<Integer> list5 = intStream5.collect(Collectors.toList());

        Assert.assertEquals(list1, list2);
        Assert.assertEquals(list2, list3);
        Assert.assertEquals(list3, list4);
        Assert.assertEquals(list4, list5);

        // Stream#empty
        Stream<Integer> emptyStream = Stream.empty();
        Assert.assertNotNull(emptyStream);
        List<Integer> emptyList = emptyStream.collect(Collectors.toList());
        Assert.assertEquals(0, emptyList.size());
        // Stream#concat
        Stream<String> textStream1 = INT_ARRAYS.stream().map(Object::toString);
        Stream<String> textStream2 = TEXT_ARRAYS.stream();
        Stream<String> concatStream = Stream.concat(textStream1, textStream2);
        Assert.assertNotNull(concatStream);
        Assert.assertEquals(
                Arrays.asList("1", "2", "3", "4", "hello", "functional", "coding"),
                concatStream.collect(Collectors.toList())
        );
    }

    @Test
    public void testIntermediateOperations() {
        // Stream#filter
        List<Integer> filterList = INT_ARRAYS.stream()
                .filter(integer -> integer % 2 == 1)
                .collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList(1, 3), filterList);
        // Stream#map
        List<String> mapList = INT_ARRAYS.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList("1", "2", "3", "4"), mapList);
        // Stream#flatMap
        List<Integer> list1 = Arrays.asList(1, 2);
        List<Integer> list2 = Arrays.asList(1, 2, 3);
        List<Integer> list3 = Arrays.asList(1, 2, 3, 4);
        //noinspection Convert2MethodRef
        Stream<Integer> intStream = Stream.of(list1, list2, list3)
                .flatMap(integers -> integers.stream());
        List<Integer> collect = intStream.sorted().collect(Collectors.toList());
        Assert.assertEquals(collect, Arrays.asList(1, 1, 1, 2, 2, 2, 3, 3, 4));
        // Stream#distinct
        List<Integer> distinctList = Stream.of(1, 2, 2, 3, 3, 3)
                .distinct()
                .collect(Collectors.toList());
        Assert.assertEquals(distinctList, Arrays.asList(1, 2, 3));
        // Stream#peek
        AtomicInteger acc = new AtomicInteger(0);
        INT_ARRAYS.stream()
                .peek(acc::addAndGet)
                .count();
        Assert.assertEquals(1 + 2 + 3 + 4, acc.get());
        // Stream#skip
        List<Integer> skipList = INT_ARRAYS.stream()
                .skip(2)
                .collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList(3, 4), skipList);
        // Stream#limit
        List<Integer> limitList = INT_ARRAYS.stream()
                .limit(3)
                .collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList(1, 2, 3), limitList);
        // Stream#sorted
        List<Integer> sortedList = Stream.of(2, 4, 1, 3)
                .sorted()
                .collect(Collectors.toList());
        List<Integer> reverseSortedList = Stream.of(2, 4, 1, 3)
                .sorted((o1, o2) -> -o1.compareTo(o2))
                .collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList(1, 2, 3, 4), sortedList);
        Assert.assertEquals(Arrays.asList(4, 3, 2, 1), reverseSortedList);
    }

    @Test
    @SuppressWarnings({"SimplifyStreamApiCallChains", "SpellCheckingInspection"})
    public void testTerminalOperations() {
        // Stream#collect
        List<Integer> intList = INT_ARRAYS.stream().collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList(1, 2, 3, 4), intList);
        Set<Integer> intSet = Stream.of(1, 2, 3, 3).collect(Collectors.toSet());
        Assert.assertTrue(intSet.contains(1));
        Assert.assertTrue(intSet.contains(2));
        Assert.assertTrue(intSet.contains(3));
        Assert.assertFalse(intSet.contains(4));
        Assert.assertEquals(3, intSet.size());
        // Stream#count
        long count = TEXT_ARRAYS.stream().count();
        Assert.assertEquals(count, 3);
        // Stream#max, min
        Optional<Integer> max = INT_ARRAYS.stream().max(Integer::compareTo);
        Assert.assertTrue(max.isPresent());
        Assert.assertEquals(4, max.get().intValue());
        Optional<Integer> min = INT_ARRAYS.stream().min(Integer::compareTo);
        Assert.assertTrue(min.isPresent());
        Assert.assertEquals(1, min.get().intValue());
        // Stream#forEach, forEachSorted
        List<String> finalTextList = new ArrayList<>();
        TEXT_ARRAYS.stream().forEach(finalTextList::add);
        Assert.assertEquals(Arrays.asList("hello", "functional", "coding"), finalTextList);
        List<Integer> finalIntList = new ArrayList<>();
        Stream.of(2, 1, 3).parallel().forEachOrdered(finalIntList::add);
        Assert.assertEquals(Arrays.asList(2, 1, 3), finalIntList);
        // Stream#toArray
        String[] textArray = TEXT_ARRAYS.stream().toArray(String[]::new);
        Assert.assertEquals(new String[]{"hello", "functional", "coding"}, textArray);
        Integer[] intArray = INT_ARRAYS.stream().toArray(Integer[]::new);
        Assert.assertEquals(new Integer[]{1, 2, 3, 4}, intArray);
        // Stream#iterator
        Iterator<Integer> iterator = INT_ARRAYS.stream().iterator();
        Assert.assertTrue(iterator.hasNext());
        intList = new ArrayList<>();
        iterator.forEachRemaining(intList::add);
        Assert.assertEquals(Arrays.asList(1, 2, 3, 4), intList);
        // Stream#spliterator
        Spliterator<Integer> spliterator = INT_ARRAYS.stream().spliterator();
        Assert.assertEquals(4, spliterator.estimateSize());
        intList = new ArrayList<>();
        Assert.assertTrue(spliterator.tryAdvance(intList::add));
        spliterator.forEachRemaining(intList::add);
        Assert.assertEquals(Arrays.asList(1, 2, 3, 4), intList);
    }

    @Test
    public void testShortCircuitingTerminalOperations() {
        // Stream#allMatch
        boolean allMatch = INT_ARRAYS.stream().allMatch(it -> it < 4);
        Assert.assertFalse(allMatch);
        // Stream#anyMatch
        boolean anyMatch = TEXT_ARRAYS.stream().anyMatch(it -> it.contains("h"));
        Assert.assertTrue(anyMatch);
        // Stream#noneMatch
        boolean noneMatch = INT_ARRAYS.stream().noneMatch(it -> it <= 0);
        Assert.assertTrue(noneMatch);
        // Stream#findAny
        Optional<Integer> any = INT_ARRAYS.stream().filter(it -> it > 3).findAny();
        Assert.assertTrue(any.isPresent());
        Assert.assertEquals(4, any.get().intValue());
        Optional<Object> empty = Stream.empty().findAny();
        Assert.assertFalse(empty.isPresent());
        // Stream#findFirst
        Optional<String> first = TEXT_ARRAYS.stream().filter(it -> it.contains("o")).findFirst();
        Assert.assertTrue(first.isPresent());
        Assert.assertEquals("hello", first.get());
    }

    @Test
    public void testReduce() {
        Stream<Integer> intStream = INT_ARRAYS.stream();
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