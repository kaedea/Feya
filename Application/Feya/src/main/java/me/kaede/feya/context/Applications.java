/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.context;

import android.annotation.SuppressLint;
import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * When the App is running, there must be an application context.
 *
 * @author Kaede
 * @see "https://github.com/oasisfeng/deagle/blob/master/library/src/main/java/com/oasisfeng/android/base/Applications.java"
 * @since 17/4/8
 */
@SuppressWarnings("WeakerAccess")
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
        return CURRENT;
    }

    @SuppressLint("StaticFieldLeak")
    private static final Application CURRENT;

    static {
        Application app = null;
        try {
            app = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null);
            if (app == null)
                throw new IllegalStateException("Static initialization of Applications must be on main thread.");
        } catch (final Exception e) {
            // Alternative path
            Log.e(TAG, "Failed to get current application from AppGlobals.", e);
            try {
                app = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
            } catch (final Exception ex) {
                Log.e(TAG, "Failed to get current application from ActivityThread.", e);
            }
        } finally {
            CURRENT = app;
        }
    }
}
