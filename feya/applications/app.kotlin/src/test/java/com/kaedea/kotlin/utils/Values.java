package com.kaedea.kotlin.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kaede
 * @since 2018/8/15
 */
public class Values {
    public static String nullString() {
        return null;
    }

    public static ArrayList<String> stringListWithInt() {
        //noinspection unchecked
        return intList();
    }

    private static ArrayList intList() {
        List list = new ArrayList();
        list.add(22);
        list.add(33);
        return (ArrayList) list;
    }
}
