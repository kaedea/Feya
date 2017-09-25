/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package moe.studio.java.test;

import org.apache.commons.io.IOUtils;
import org.apache.http.util.TextUtils;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.commons.io.FileUtils.deleteQuietly;

/**
 * @author Kaede.
 * @since 2016/12/27.
 */
class Utils {
    final static class FileUtils {

        private static final String TAG = "plugin.files";

        static void closeQuietly(Closeable closeable) {
            IOUtils.closeQuietly(closeable);
        }

        static boolean delete(String path) {
            if (TextUtils.isEmpty(path)) {
                return false;
            }
            return delete(new File(path));
        }

        static boolean delete(File file) {
            return deleteQuietly(file);
        }

        static boolean exist(String path) {
            return !TextUtils.isEmpty(path) && (new File(path).exists());
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        static void checkCreateFile(File file) throws IOException {
            if (file == null) {
                throw new IOException("File is null.");
            }
            if (file.exists()) {
                delete(file);
            }
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!file.createNewFile()) {
                throw new IOException("Create file fail, file already exists.");
            }
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        static void checkCreateDir(File file) throws IOException {
            if (file == null) {
                throw new IOException("Dir is null.");
            }
            if (file.exists()) {
                if (file.isDirectory()) {
                    return;
                }
                if (!delete(file)) {
                    throw new IOException("Fail to delete existing file, file = "
                            + file.getAbsolutePath());
                }
                file.mkdir();
            } else {
                file.mkdirs();
            }
            if (!file.exists() || !file.isDirectory()) {
                throw new IOException("Fail to create dir, dir = " + file.getAbsolutePath());
            }
        }

        static void copyFile(File sourceFile, File destFile) throws IOException {
            if (sourceFile == null) {
                throw new IOException("Source file is null.");
            }
            if (destFile == null) {
                throw new IOException("Dest file is null.");
            }
            if (!sourceFile.exists()) {
                throw new IOException("Source file not found.");
            }

            checkCreateFile(destFile);
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(sourceFile);
                out = new FileOutputStream(destFile);
                FileDescriptor fd = ((FileOutputStream) out).getFD();
                out = new BufferedOutputStream(out);
                IOUtils.copy(in, out);
                out.flush();
                fd.sync();
            } catch (IOException e) {
                Log.w(TAG, e);
            } finally {
                closeQuietly(in);
                closeQuietly(out);
            }
        }

        static void dumpFiles(File file) {
            if (!Log.DEBUG) {
                return;
            }

            boolean isDirectory = file.isDirectory();
            Log.v(TAG, "path = " + file.getAbsolutePath() + ", isDir = " + isDirectory);
            if (isDirectory) {
                File[] childFiles = file.listFiles();
                if (childFiles != null && childFiles.length > 0) {
                    for (File childFile : childFiles) {
                        dumpFiles(childFile);
                    }
                }
            }
        }
    }

    final static class Log {

        static final boolean DEBUG = true;

        static void v(String TAG, String msg) {
            if (!DEBUG) return;
            System.out.println(TAG + " " + msg);
        }

        static void d(String TAG, String msg) {
            if (!DEBUG) return;
            System.out.println(TAG + " " + msg);
        }

        static void i(String TAG, String msg) {
            if (!DEBUG) return;
            System.out.println(TAG + " " + msg);
        }

        static void w(String TAG, String msg) {
            System.out.println(TAG + " " + msg);
        }

        static void w(String TAG, Throwable throwable) {
            System.out.println(TAG + " " + throwable.toString());
        }
    }
}
