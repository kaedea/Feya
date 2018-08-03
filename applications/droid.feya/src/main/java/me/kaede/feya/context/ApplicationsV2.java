/*
 * Copyright (c) 2018. Kaede <kidhaibara@gmail.com>.
 *
 */

package me.kaede.feya.context;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.app.Application;
import android.support.annotation.NonNull;

/**
 * When the App is running, there must be an application context. {@link ApplicationsV2#context()} offers
 * an globally accessible method to get the current process application context.
 *
 * @author Kaede
 * @see Applications
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ApplicationsV2 {

    private static final String TAG = "ApplicationsV2";

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
        throw new IllegalStateException("Please make sure you do not call Applications#context() " +
                "before or inside Application#attachBaseContext(Context). " +
                "If you have to, please call Applications#attach(Application) first.");
    }

    @SuppressLint("StaticFieldLeak")
    private static final Application CURRENT;

    static {
        try {
            Application app = ActivityThread.currentApplication();
            if (app == null) {
                throw new IllegalStateException("Can not get Application context, " +
                        "pls make sure that you didn't call this method before or inner " +
                        "Application#attachBaseContext(Context)");
            }
            CURRENT = app;
        } catch (Throwable e) {
            throw new IllegalStateException("Can not access Application context by magic code, boom!", e);
        }
    }

}
