/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package moe.studio.java.test;

public class Entry {

    public static void main(String[] args) {
        // write your code here
        System.out.println("Hello, IntelliJ!");

        // MultiDex
        // MethodsChain.createChainMethods(300);

        // BsDiff
        //new BsDiff().diff();

        // Channels
        // Channels.go();

        // Archive Diff
        String[] urls = new String[]{
                "xxxx.apk",
                "xxxx.apk"};
        ArchiveDiff.go(urls);
    }
}
