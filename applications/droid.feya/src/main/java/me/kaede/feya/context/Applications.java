/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.context;

import android.annotation.SuppressLint;
import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * When the App is running, there must be an application context. {@link Applications#context()} offers
 * an globally accessible method to get the current process application context.
 *
 * @author Kaede
 * @see "http://kaedea.com/2017/04/09/android/global-accessing-context/"
 * @see "https://github.com/oasisfeng/deagle/blob/master/library/src/main/java/com/oasisfeng/android/base/Applications.java"
 * @since 17/4/8
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Applications {

    private static final String TAG = "Applications";

    /**
     * Access a global {@link Application} context from anywhere, such as getting a context in a Library
     * module without attaching it from App module.
     * <p>
     * Note that this method may return null in some cases, such as working with a hotfix framework
     * or access when the App is terminated.
     */
    @NonNull
    public static Application context() {
        if (CURRENT != null) {
            return CURRENT;
        }
        if (sAttached != null) {
            Log.w(TAG, "Seems CURRENT is null here, you may call Applications#context() before or " +
                    "inside Application#attachBaseContext(Context), which is not recommended.");
            return sAttached;
        }
        throw new IllegalStateException("Please make sure you do not call Applications#context() " +
                "before or inside Application#attachBaseContext(Context). " +
                "If you have to, please call Applications#attach(Application) first.");
    }

    @SuppressLint("StaticFieldLeak")
    private static final Application CURRENT;
    @SuppressLint("StaticFieldLeak")
    private static Application sAttached;

    static {
        /*
         * The following 'Magic Code' is going to access the Application context from ActivityThread.
         * For now, it works only after Applications#attach(Application).
         *
         * Note that if you call this method before or inside Applications#attach(Application),
         * {@link Applications#CURRENT} will always be null.
         */
        try {
            Object app = autoAttach();
            if (app == null) {
                throw new IllegalStateException("Can not get Application context, " +
                        "pls make sure that you didn't call this method before or inner " +
                        "Application#attachBaseContext(Context)");
            }
            CURRENT = (Application) app;

            //noinspection ConstantConditions
            if (CURRENT == null) {
                throw new IllegalStateException("Can not access Application context from ActivityThread, " +
                        "please make sure that you did not call this method before or inside Application#attachBaseContext(Context).");
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Can not access Application context by magic code, boom!", e);
        }
    }

    private static Object autoAttach() throws ReflectiveOperationException {
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
        return app;
    }

    /**
     * Manually attach an Application context for {@link Applications#sAttached}. If the above
     * Magic Code works, this method is not necessary.
     *
     * @see #autoAttach()
     */
    public void attach(@NonNull Application app) {
        if (sAttached == null) {
            sAttached = app;
        }
    }
}
