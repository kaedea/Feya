/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package moe.studio.java.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Kaede.
 * @versuib 2016/12/15.
 */
public class MethodsChain {

    public static void createChainMethods(int count) {
        for (int i = 0; i < count; i++) {
            writeChainMethods(i);
        }
    }

    private static void writeChainMethods(int index) {
        String fileName = "/Users/Kaede/Desktop/MultiDex/ChainMethods/Methods" + index + ".java";
        String clazz = "package tv.danmaku.methods.chain;\n" +
                "\n" +
                "import android.util.Log;\n" +
                "\n" +
                "public class Methods" + index + " {\n" +
                "    public void chaining() {\n" +
                "        Log.v(\"multidex\", \"Method" + index + " chaining.\");\n" +
                "        new Methods" + (index + 1) + "().chaining();\n" +
                "    }\n" +
                "}\n";

        try {
            File file = new File(fileName);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            System.out.println("Creating " + fileName);

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(clazz);
            bw.close();
            //System.out.println("Done writing to " + fileName); //For testing
        } catch (IOException e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }
    }
}
