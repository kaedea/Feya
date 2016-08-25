/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.protobuff;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;

import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * @author kaede
 * @version date 16/8/24
 */
public class CommentParseTest extends InstrumentationTestCase {
    public static final String TAG = "CommentParse";

    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    public void testParseCommentFile() {
        Pattern pattern = Pattern.compile("<d.+?p=\"(.+?)\">(.*?)</d>");
        Scanner scanner = null;
        try {
            scanner = new Scanner(mContext.getAssets().open("1221.xml"));
            while (scanner.findWithinHorizon(pattern, 0) != null) {
                MatchResult match = scanner.match();
                if (match.groupCount() >= 2) {
                    String dmkAttr = unescapeHtmlQuietly(match.group(1));
                    String dmkText = unescapeHtmlQuietly(match.group(2));
                    if (TextUtils.isEmpty(dmkAttr) || TextUtils.isEmpty(dmkText))
                        continue;
                    Log.i(TAG, "dmkAttr = " + dmkAttr + ", dmkText = " + dmkText);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (scanner != null)
                scanner.close();
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
