/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.protobuff;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Environment;
import android.test.InstrumentationTestCase;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import me.kaede.feya.StopWatch;
import me.kaede.feya.protobuff.danmaku.DanmakuDoc;
import me.kaede.feya.protobuff.danmaku.DanmakuItem;

/**
 * @author Kaede
 * @since date 16/8/24
 */
public class CommentParseTest extends InstrumentationTestCase {
    public static final String TAG = "CommentParse";

    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    public void testParseComments() {
        try {
            // list "assets/danmaku/"
            String[] danmakus = mContext.getAssets().list("danmaku");
            for (String xml : danmakus) {
                parseComments("danmaku" + File.separator + xml);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * test parse xml compared with protobuf (wire)
     * @param xml
     */
    private void parseComments(String xml) {
        Pattern pattern = Pattern.compile("<d.+?p=\"(.+?)\">(.*?)</d>");
        Scanner scanner = null;
        StopWatch stopWatch = new StopWatch();
        try {
            AssetFileDescriptor fd = mContext.getAssets().openFd(xml);
            Log.i(TAG, "xml file size = " + fd.getLength());

            // parse xml
            scanner = new Scanner(mContext.getAssets().open(xml));
            List<DanmakuItem> danmakuItems = new ArrayList<>();
            stopWatch.start("parse " + xml);
            while (scanner.findWithinHorizon(pattern, 0) != null) {
                MatchResult match = scanner.match();
                if (match.groupCount() >= 2) {
                    String dmkAttr = unescapeHtmlQuietly(match.group(1));
                    String dmkText = unescapeHtmlQuietly(match.group(2));
                    if (TextUtils.isEmpty(dmkAttr) || TextUtils.isEmpty(dmkText))
                        continue;
                    Log.v(TAG, "dmkAttr = " + dmkAttr + ", dmkText = " + dmkText);
                    DanmakuItem danmakuItem = new DanmakuItem.Builder()
                            .dmk_attr(dmkAttr)
                            .dmk_text(dmkText)
                            .build();
                    danmakuItems.add(danmakuItem);
                }
            }

            // create protobuff object
            DanmakuDoc danmakuDoc = new DanmakuDoc.Builder()
                    .items(danmakuItems)
                    .build();
            stopWatch.split("create DanmakuDoc");
            assertNotNull(danmakuDoc);
            byte[] bytes = DanmakuDoc.ADAPTER.encode(danmakuDoc);
            String bytesContent = new String(bytes);
            Log.v(TAG, "danmakuDoc = " + String.valueOf(danmakuDoc));

            // write bytes to file
            File file = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "danmaku_" + xml + "bytes");
            FileUtils.writeByteArrayToFile(file, bytes);
            stopWatch.split("write bytes");
            Log.i(TAG, "bytes file size = " + file.length());

            // read bytes from file again
            bytes = FileUtils.readFileToByteArray(file);
            stopWatch.split("read bytes");

            // create protobuff object from bytes
            DanmakuDoc danmakuDocBytes = DanmakuDoc.ADAPTER.decode(bytes);
            Log.v(TAG, "danmakuDocBytes = " + String.valueOf(danmakuDocBytes));
            stopWatch.end("parse DanmakuDoc");

            same(danmakuDoc, danmakuDocBytes);

            Log.i(TAG, "stop watch = " + stopWatch);

        } catch (Throwable t) {
            assertNull(t);
            t.printStackTrace();
        } finally {
            if (scanner != null)
                scanner.close();
        }
    }

    private void same(DanmakuDoc first, DanmakuDoc second) {
        assertTrue(first != null && second != null);
        assertTrue(first.items != null && second.items != null);
        assertTrue(first.items.size() == second.items.size());
        for (int i = 0; i < first.items.size(); i++) {
            assertTrue(first.items.get(i).dmk_attr.equals(second.items.get(i).dmk_attr));
            assertTrue(first.items.get(i).dmk_text.equals(second.items.get(i).dmk_text));
        }
    }

    private String unescapeHtmlQuietly(String htmlString) {
        if (TextUtils.isEmpty(htmlString))
            return null;

        return unescapeHtml(htmlString);
    }

    private static final CharSequenceTranslator UNESCAPE_HTML = new LookupTranslator(BASIC_UNESCAPE());

    private static String[][] BASIC_UNESCAPE() {
        String[][] strings = new String[][]{{"\"", "&quot;"}, {"'", "&apos;"}, {" ", "&nbsp;"}, {"&", "&amp;"}, {"<", "&lt;"}, {">", "&gt;"}};
        String[][] newArray = new String[strings.length * 2][2];
        for (int i = 0; i < strings.length; i++) {
            String[] seg = strings[i];
            newArray[i][0] = seg[1].toUpperCase();
            newArray[i][1] = seg[0];
            newArray[i + strings.length][1] = seg[0];
            newArray[i + strings.length][0] = seg[1];
        }
        return newArray;
    }

    private static final int TOO_LARGE_STRING = 1 << 20;
    static ThreadLocal<StringBuilderWriter> sLocalWriter = new ThreadLocal<StringBuilderWriter>() {
        @Override
        protected StringBuilderWriter initialValue() {
            return new StringBuilderWriter(new StringBuilder(1024));
        }
    };

    private static String unescapeHtml(String input) {
        if (input == null) return null;
        if (input.indexOf('&') < 0) return input;
        if (input.length() > TOO_LARGE_STRING) {
            return input;
        }
        try {
            StringBuilderWriter writer = getStringBuilderWriter(input);
            UNESCAPE_HTML.translate(input, writer);
            StringBuilder builder = writer.getBuilder();
            try {
                return builder.toString();
            } finally {
                builder.setLength(0);
            }
        } catch (Exception e) {
            return input;
        }
    }

    private static StringBuilderWriter getStringBuilderWriter(String input) {
        StringBuilderWriter writer = sLocalWriter.get();
        writer.getBuilder().ensureCapacity(input.length());
        return writer;
    }

}
