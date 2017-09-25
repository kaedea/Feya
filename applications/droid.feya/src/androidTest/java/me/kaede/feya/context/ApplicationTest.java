/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.context;

import android.app.Application;
import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.kaede.feya.BaseApplication;
import me.kaede.feya.Feya;

/**
 * @author Kaede
 * @since 17/4/8
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest {

    public static final String TAG = "ApplicationTest";

    @Test
    public void testGetBaseApplication() {
        Context context = BaseApplication.getContext();
        Assert.assertNotNull(context);
        Log.i(TAG, String.valueOf(context));
        Assert.assertTrue(context instanceof Feya);
    }

    @Test
    public void testGetGlobalContext() {
        Application context = Applications.context();
        Assert.assertNotNull(context);
        Log.i(TAG, String.valueOf(context));
        Assert.assertTrue(context instanceof Feya);
    }

    @Test
    public void testGetGlobalContextInBackground() throws InterruptedException {
        final Application[] context = {null};
        new Thread(new Runnable() {
            @Override
            public void run() {
                context[0] = Applications.context();
            }
        }).start();

        Thread.sleep(200);

        Assert.assertNotNull(context[0]);
        Log.i(TAG, String.valueOf(context[0]));
        Assert.assertTrue(context[0] instanceof Feya);
    }
}
