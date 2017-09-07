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
import java.util.Collections;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kaede
 * @since 17/9/3
 */
@RunWith(JUnit4.class)
public class CollectorTest {
    private static final List<Integer> INT_ARRAYS = Arrays.asList(1, 2, 3, 4);
    private static final List<String> TEXT_ARRAYS = Arrays.asList("hello", "functional", "coding");

    @Test
    public void testToCollection() {
        Supplier<ArrayList> listSupplier = ArrayList::new;
        //noinspection unchecked
        List<Integer> list = Stream
                .of(1, 2, 3, 4)
                .collect(Collectors.toCollection(listSupplier));
        Assert.assertEquals(list, Arrays.asList(1, 2, 3, 4));

        HashSet<Integer> hashSet = Stream
                .of(1, 2, 3, 3)
                .collect(Collectors.toCollection(HashSet::new));
        Assert.assertEquals(hashSet.size(), 3);

        TreeSet<Integer> treeSet = Stream
                .of(1, 2, 2)
                .collect(Collectors.toCollection(TreeSet::new));
        Assert.assertEquals(treeSet.size(), 2);

        Map<Integer, String> map = Stream
                .of(1, 2, 3, 4)
                .collect(Collectors.toMap(
                        i -> i,
                        String::valueOf)
                );
        Assert.assertEquals(map.get(1), "1");
        Assert.assertEquals(map.get(2), "2");
        Assert.assertEquals(map.getOrDefault(0, "default"), "default");
    }

    @Test
    public void testCalculating() {
        Optional<Integer> maxOptional = INT_ARRAYS.stream().collect(
                Collectors.maxBy(Integer::compareTo)
        );
        Assert.assertTrue(maxOptional.isPresent());
        Assert.assertEquals(maxOptional.get().intValue(), 4);

        Optional<Integer> minOptional = INT_ARRAYS.stream().collect(
                Collectors.minBy(Integer::compareTo)
        );
        Assert.assertTrue(minOptional.isPresent());
        Assert.assertEquals(minOptional.get().intValue(), 1);

        IntSummaryStatistics summary = INT_ARRAYS.stream().collect(
                Collectors.summarizingInt(value -> value)
        );
        Assert.assertEquals(summary.getCount(), 4);
        Assert.assertEquals(summary.getSum(), 1 + 2 + 3 + 4);
    }

    @Test
    public void testPartitioning() {
        Map<Boolean, List<Integer>> oddMap = INT_ARRAYS.stream().collect(
                Collectors.partitioningBy(x -> (x % 2) == 1)
        );
        Assert.assertEquals(oddMap.get(true), Arrays.asList(1, 3));
        Assert.assertEquals(oddMap.get(false), Arrays.asList(2, 4));
    }

    @Test
    public void testGrouping() {
        Map<Integer, List<Integer>> intMap = INT_ARRAYS.stream().collect(
                Collectors.groupingBy(Integer::new)
        );
        Assert.assertEquals(intMap.get(1), Collections.singletonList(1));
        Assert.assertEquals(intMap.get(2), Collections.singletonList(2));
        Assert.assertEquals(intMap.keySet().size(), 4);
    }

    @Test
    public void testStringJoining() {
        String combine = TEXT_ARRAYS.stream().collect(Collectors.joining());
        Assert.assertEquals(combine, "hello" + "functional" + "coding");

        combine = TEXT_ARRAYS.stream().collect(
                Collectors.joining(",", "[", "]")
        );
        Assert.assertEquals(combine, "[hello,functional,coding]");
    }

    @Test
    public void testMapping() {
        Map<Boolean, List<String>> oddMap = INT_ARRAYS.stream().collect(Collectors.groupingBy(
                integer -> integer % 2 == 1,
                Collectors.mapping(Object::toString, Collectors.toList())
        ));
        Assert.assertEquals(oddMap.keySet().size(), 2);
        Assert.assertEquals(oddMap.get(true), Arrays.asList("1", "3"));
        Assert.assertEquals(oddMap.get(false), Arrays.asList("2", "4"));
    }


    /**
     * See {@link StreamTest#testReduceOperation()}
     */
    @Test
    @SuppressWarnings("PointlessArithmeticExpression")
    public void testReducingCollector() {
        // Collectors#reducing(BinaryOperator)
        Optional<Integer> min = INT_ARRAYS.stream().collect(Collectors.reducing((x, y) -> {
            if (x > y) return y;
            return x;
        }));
        Assert.assertTrue(min.isPresent());
        Assert.assertEquals(1, min.get().intValue());

        // Collectors#reducing(T, BinaryOperator<T>)
        Integer acc = INT_ARRAYS.stream().collect(Collectors.reducing(0, (x, y) -> x + y));
        Assert.assertEquals(acc.intValue(), 0 + 1 + 2 + 3 + 4);

        // Collectors#reducing(U, Function<? super T, ? extends U>, BinaryOperator<U>)
        Integer max = INT_ARRAYS.stream().collect(Collectors.reducing(
                0,
                integer -> integer,
                (x, y) -> {
                    if (x > y) return x;
                    return y;
                }

        ));
        Assert.assertEquals(max.intValue(), 4);
    }
}
