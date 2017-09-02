/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com>.
 *
 */

package moe.studio.java.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kaede
 * @since 2017/5/27
 */
public class RemoveLines {

    static List<String> removedIds;

    static void foo(String[] args) {
        try {
            BufferedReader reader = null;
            BufferedWriter writer = null;
            String currentLine;

            File removedIdsFile = new File(args[0]);
            reader = new BufferedReader(new FileReader(removedIdsFile));
            removedIds = new ArrayList<>(0);

            while((currentLine = reader.readLine()) != null) {
                // drawable/ic_group_comment_grey declared here is not defined.
                removedIds.add(currentLine.substring(currentLine.indexOf("/") + 1, currentLine.indexOf(" declared")));
            }
            reader.close();

            for (String item : removedIds) {
                System.out.println(item);
            }

            File inputFile = new File(args[1]);
            File outputFile = new File(args[1].substring(0, args[1].lastIndexOf(".")) + "-removed.xml");

            reader = new BufferedReader(new FileReader(inputFile));
            writer = new BufferedWriter(new FileWriter(outputFile));

            while((currentLine = reader.readLine()) != null) {
                if (currentLine.contains("type") && currentLine.contains("name") && currentLine.contains("id")) {
                    // <public type="attr" name="constraintSet" id="0x7f010000" />
                    String id = currentLine.substring(currentLine.indexOf("name=\"") + "name=\"".length(), currentLine.indexOf("\" id=\""));
                    if (removedIds.contains(id)) {
                        continue;
                    }
                }
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            reader.close();
            writer.flush();
            writer.close();
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
