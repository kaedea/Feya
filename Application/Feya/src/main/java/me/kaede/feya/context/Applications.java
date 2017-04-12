/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.context;

import android.annotation.SuppressLint;
import android.app.Application;
import android.support.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * When the App is running, there must be an application context.
 *
 * @author Kaede
 * @see "https://github.com/oasisfeng/deagle/blob/master/library/src/main/java/com/oasisfeng/android/base/Applications.java"
 * @since 17/4/8
 */
@SuppressWarnings("WeakerAccess")
public class Applications {

    /**
     * Access a global {@link Application} context from anywhere, such as getting a context in a Library
     * module without attaching it from App module.
     * <p>
     * Note that this method may return null in some cases, such as working with a hotfix framework
     * or access when the App is terminated.
     */
    @NonNull
    public static Application context() {
        return CURRENT;
    }

    @SuppressLint("StaticFieldLeak")
    private static final Application CURRENT;

    static {
        try {
            Object activityThread = AndroidHacks.getActivityThread();
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method method = activityThreadClass.getMethod("getApplication");
            method.setAccessible(true);
            Object app = method.invoke(activityThread);

            if (app == null) {
                Field field = activityThreadClass.getField("mInitialApplication");
                field.setAccessible(true);
                app = field.get(activityThread);
            }
            if (app == null) {
                throw new IllegalStateException("Can not get Application context, " +
                        "pls make sure that you didn't call this method before or inner " +
                        "Application#attachBaseContext(Context)");
            }
            CURRENT = (Application) app;
        } catch (Throwable e) {
            throw new IllegalStateException("Can not access Application context by magic code, boom!", e);
        }
    }
}
