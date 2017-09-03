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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    @Test
    public void testToCollection() {
        Supplier<ArrayList> listSupplier = ArrayList::new;
        //noinspection unchecked
        List<Integer> list = Stream
                .of(1,2,3,4)
                .collect(Collectors.toCollection(listSupplier));
        Assert.assertEquals(list, Arrays.asList(1,2,3,4));

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
}
