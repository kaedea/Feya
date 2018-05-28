/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.log;

import android.test.InstrumentationTestCase;

import moe.studio.log.BLog;
import moe.studio.log.LogSetting;

/**
 * repo: https://github.com/kaedea/b-log
 * <p>
 * Copycat of  {@link
 * "https://github.com/kaedea/b-log/blob/master/library/src/androidTest/java/moe/kaede/log/BLogApiTest.java"}
 *
 * @author Kaede
 * @since date 16/9/25
 */
public class BLogApiTest extends InstrumentationTestCase {

    public static final String TAG = "BLogApiTest";

    public void setUp() {
        LogSetting setting = new LogSetting.Builder(getInstrumentation().getTargetContext())
                .showThreadInfo(true)
                .build();
        BLog.initialize(setting);
    }

    public void tearDown() {
        BLog.shutdown();
    }

    public void testLogBasic() {
        BLog.v(TAG, "log verbose");
        BLog.v("log verbose with default tag");

        BLog.d(TAG, "log debug");
        BLog.d("log debug with default tag");

        BLog.i(TAG, "log info");
        BLog.i("log info with default tag");

        BLog.w(TAG, "log warning");
        BLog.w("log warning with default tag");

        BLog.e(TAG, "log error");
        BLog.e("log error with default tag");

        BLog.wtf(TAG, "log wtf");
        BLog.wtf("log wtf with default tag");
    }

    public void testLogFormat() {
        // use log format, you must offer a tag, even it's null(use default tag)
        BLog.vfmt(TAG, "log %s with format string", "verbose");
        BLog.dfmt(null, "log %s with format string", "debug");
        BLog.ifmt(TAG, "log %s with format string", "info");
        BLog.wfmt(null, "log %s with format string", "warning");
        BLog.efmt(TAG, "log %s with format string", "error");
        BLog.wtffmt(null, "log %s with format string", "wtf");

        // test error format args
        // 1. error format msg
        BLog.dfmt(null, "error format msg", "debug");
        // 2. error format args
        BLog.dfmt(null, "%s format msg", "error", "error", "error");
        BLog.dfmt(null, "%s %s %s format msg", "error");
    }

    public void testLogThrowable() {
        Exception exception = new RuntimeException("一种钦定的感觉");

        BLog.v(TAG, "runtime exception", exception);
        BLog.v(TAG, exception);

        BLog.d(TAG, "runtime exception", exception);
        BLog.d(TAG, exception);

        BLog.i(TAG, "runtime exception", exception);
        BLog.i(TAG, exception);

        BLog.w(TAG, "runtime exception", exception);
        BLog.w(TAG, exception);

        BLog.e(TAG, "runtime exception", exception);
        BLog.e(TAG, exception);
    }

    public void testEvent() {
        BLog.event("BLOG", "XX一律不得经商");
        BLog.event("XX发大财才是坠猴的");
        BLog.event("做了一点成绩");
    }
}
