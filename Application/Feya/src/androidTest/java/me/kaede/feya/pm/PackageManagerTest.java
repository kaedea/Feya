/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.pm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.test.InstrumentationTestCase;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * test PackageManager Api
 * Created by Kaede on 16/8/1.
 */
public class PackageManagerTest extends InstrumentationTestCase {

    public static final String TAG = "PackageManagerTest";
    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    public void testIsPackageInstalled() {
        String packageName = "tv.danmaku.bili";
        PackageInfo packageInfo = null;
        try {
            packageInfo = mContext.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "is " + packageName + " installed = " + (packageInfo != null));
        if (packageInfo != null) {
            Log.d(TAG, packageName + "'s packageInfo = " + packageInfo.toString());
        }
    }

    public void testGetInstallPackageInfo() throws NoSuchFieldException, IllegalAccessException {
        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        assertNotNull(packages);

        Log.i(TAG, "all package num = " + packages.size());
        for (PackageInfo packageInfo : packages) {
            Log.i(TAG, "-");

            // PackageInfo
            Log.v(TAG, "packageName = " + packageInfo.packageName);
            Log.v(TAG, "version name = " + packageInfo.versionName);
            Log.v(TAG, "version code = " + packageInfo.versionCode);

            Field field = findField(packageInfo, "coreApp");
            boolean coreApp = (boolean) field.get(packageInfo);
            if (coreApp) {
                Log.d(TAG, "coreApp = " + true);
            } else {
                Log.v(TAG, "coreApp = " + false);
            }

            Log.d(TAG, "Package info = " + packageInfo.toString());

            // PackageInfo#ApplicationInfo
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            Log.v(TAG, "App name = " + applicationInfo.loadLabel(pm));
            Log.v(TAG, "App Icon = " + String.valueOf(applicationInfo.loadIcon(pm)));
            Log.v(TAG, "sourceDir = " + applicationInfo.sourceDir);
            Log.v(TAG, "publicSourceDir = " + applicationInfo.publicSourceDir);
            Log.v(TAG, "dataDir = " + applicationInfo.dataDir);
            Log.v(TAG, "nativeDir = " + applicationInfo.nativeLibraryDir);

            boolean isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            if (isSystemApp) {
                Log.d(TAG, "Is system app = " + true);
            } else {
                Log.v(TAG, "Is system app = " + false);
            }
            if (applicationInfo.enabled) {
                Log.d(TAG, "Is enabled = " + true);
            } else {
                Log.v(TAG, "Is enabled = " + false);
            }

            Log.i(TAG, "application info = " + applicationInfo.toString());
            Log.i(TAG, "-");
        }
    }

    public void testGetUnInstallPackageInfo() {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo("", 0);
        assertNotNull(packageInfo);

        Log.i(TAG, "-");
        try {
            // PackageInfo
            String packageName = packageInfo.packageName;
            Log.d(TAG, "packageName = " + packageName);
            // Package's ApplicationInfo
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            Log.d(TAG, "publicSourceDir = " + applicationInfo.publicSourceDir);
            boolean isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            Log.w(TAG, "is system app = " + isSystemApp);
            Log.d(TAG, "application info = " + applicationInfo.toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "package info =" + packageInfo.toString());
        Log.i(TAG, "-");
    }

    /**
     * Locates a given field anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the field into.
     * @param name field name
     * @return a field object
     * @throws NoSuchFieldException if the field cannot be located
     */
    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }
        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }
    /**
     * Locates a given method anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the method into.
     * @param name method name
     * @param parameterTypes method parameter types
     * @return a method object
     * @throws NoSuchMethodException if the method cannot be located
     */
    private static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }
        throw new NoSuchMethodException("Method " + name + " with parameters " +
                Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }
}
