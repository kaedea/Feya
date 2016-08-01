package me.kaede.feya.storage;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.test.InstrumentationTestCase;
import android.text.format.Formatter;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * use StorageManager to get device's storage info;
 * Created by Kaede on 16/7/29.
 */
public class StorageManagerTest extends InstrumentationTestCase {

    public static final String TAG = "StorageManagerTest";

    public Context context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = getInstrumentation().getTargetContext();
    }


    /**
     * list of paths for all mountable volumes.
     */
    public void testGetDeviceStorageInfo() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ArrayList<StorageEntity> storageEntities = new ArrayList<StorageEntity>();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        String[] paths = (String[]) storageManager.getClass().getMethod("getVolumePaths", null).invoke(storageManager, null);
        assertNotNull(paths);
        for (String path : paths) {
            StatFs mStatFs = new StatFs(path);
            long blockSize = mStatFs.getBlockSize();
            long totalBlocks = mStatFs.getBlockCount();
            long availableBlocks = mStatFs.getAvailableBlocks();
            long totalCapacity = blockSize * totalBlocks;
            long availableCapacity = availableBlocks * blockSize;
            Boolean isEnable = false;
            String totalCapacityMessage = Formatter.formatFileSize(context, totalCapacity);
            String availableCapacityMessage = Formatter.formatFileSize(context, availableCapacity);

            String status = (String) storageManager.getClass().getMethod("getVolumeState", String.class).invoke(storageManager, path);
            assertNotNull(status);
            if (status.equals(Environment.MEDIA_MOUNTED)) {
                isEnable = true;
            }
            StorageEntity storageEntity = new StorageEntity(isEnable, path, totalCapacity, totalCapacityMessage, availableCapacity,
                    availableCapacityMessage);
            storageEntities.add(storageEntity);
        }
        assertTrue(storageEntities.size() > 0);

        // print infos
        Log.i(TAG, "[testGetDeviceStorageInfo]storage count = " + storageEntities.size());
        for (StorageEntity item : storageEntities) {
            Log.d(TAG, "storage info = " + item.toString());
        }
    }

    public static class StorageEntity {
        public Boolean isEnable;
        public String path;
        public long totalCapacity;
        public String totalCapacityMessage;
        public long availableCapacity;
        public String availableCapacityMessage;

        public StorageEntity(Boolean isEnable,
                             String path,
                             long totalCapacity,
                             String totalCapacityMessage,
                             long availableCapacity,
                             String availableCapacityMessage) {
            this.isEnable = isEnable;
            this.path = path;
            this.totalCapacity = totalCapacity;
            this.totalCapacityMessage = totalCapacityMessage;
            this.availableCapacity = availableCapacity;
            this.availableCapacityMessage = availableCapacityMessage;
        }

        @Override
        public String toString() {
            return "StorageEntity{" +
                    "isEnable=" + isEnable +
                    ", path='" + path + '\'' +
                    ", totalCapacity=" + totalCapacity +
                    ", totalCapacityMessage='" + totalCapacityMessage + '\'' +
                    ", availableCapacity=" + availableCapacity +
                    ", availableCapacityMessage='" + availableCapacityMessage + '\'' +
                    '}';
        }
    }
}
