/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya;

import android.content.Context;

import me.kaede.feya.multidex.MultiDexApplication;

/**
 * @author Kaede
 * @since 17/4/8
 */
public class BaseApplication extends MultiDexApplication {

    private static Context sContext;

    public BaseApplication() {
        sContext = this;
    }

    public static Context getContext() {
        return sContext;
    }
}
