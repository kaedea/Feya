/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.frontia;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

/**
 * get classloader info
 * Created by Kaede on 16/8/1.
 */
public class ClassLoaderTest extends InstrumentationTestCase {

    public static final String TAG = "ClassLoaderTest";
    Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    public void testAppClassLoaderInstance() {
        ClassLoader classLoader = mContext.getClassLoader();
        assertTrue(ClassLoaderTest.class.getClassLoader() == classLoader);
    }

    public void testListClassLoaderInstance() {
        ClassLoader classLoader = mContext.getClassLoader();
        Log.i(TAG, "=================== classloader info ===================");
        while (classLoader != null) {
            Log.d(TAG, classLoader.toString());
            classLoader = classLoader.getParent();
        }
        Log.i(TAG, "========================================================");
    }
}
