package me.kaede.feya.pm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.test.InstrumentationTestCase;
import android.util.Log;

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

    public void testGetInstalledPackageInfo() {
        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        assertNotNull(packages);

        Log.d(TAG, "all package num = " + packages.size());
        for (PackageInfo packageInfo : packages) {
            Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> package info bgn <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
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
            Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> package info end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }
    }
}
