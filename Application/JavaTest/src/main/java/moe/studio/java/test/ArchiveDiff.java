/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package moe.studio.java.test;

import com.google.archivepatcher.applier.FileByFileV1DeltaApplier;
import com.google.archivepatcher.generator.FileByFileV1DeltaGenerator;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import moe.studio.java.test.Utils.Log;
import sun.security.action.GetPropertyAction;

/**
 * @author Kaede
 * @since date 2017/1/5
 */
public class ArchiveDiff {

    public static final String TAG = "archive.diff";

    public static void go(String[] args) {
        if (args == null || args.length < 2) {
            throw new RuntimeException("Bad arguments.");
        }

        try {
            File cacheDir = new File(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")));
            File outputDir = new File(cacheDir, "archive_patch");
            Utils.FileUtils.checkCreateDir(outputDir);

            String url = args[0];
            String md5 = getMd5(download(url, File.createTempFile("new_", ".apk")));
            Apk newApk = new Apk(url, md5);
            List<Apk> oldApks = new ArrayList<>();

            for (int i = 1; i < args.length; i++) {
                url = args[i];
                md5 = getMd5(download(url, File.createTempFile("new_", ".apk")));
                oldApks.add(new Apk(url, md5));
            }

            diff(newApk, oldApks, outputDir);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void diff(Apk newApk, List<Apk> oldApks, File outputDir) {
        if (newApk == null || oldApks == null || outputDir == null) {
            throw new RuntimeException("Bad arguments.");
        }

        Log.d(TAG, "Start diff, new apk = " + newApk);

        try {
            FileUtils.cleanDirectory(outputDir);
            File cacheDir = new File(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")));
            File newApkFile = getApkFile(newApk, cacheDir);

            for (Apk item : oldApks) {
                Log.d(TAG, "Start diff, old apk = " + item);
                try {
                    File oldApkFile = getApkFile(item, cacheDir);
                    File patchFile = new File(outputDir, getPatchName(newApk, item));

                    // Diff
                    Log.d(TAG, "Do archive diff.");
                    diff(oldApkFile, newApkFile, patchFile);
                    // Test patch
                    File merge = File.createTempFile("patch_", "_test", cacheDir);
                    Log.d(TAG, "Do archive patch.");
                    patch(oldApkFile, merge, patchFile);

                    if (!getMd5(merge).equalsIgnoreCase(newApk.md5)) {
                        throw new Exception("Merge file's md5 not match.");
                    }

                    // Diff success
                    Log.i(TAG, "Diff success, patch file = " + patchFile.getAbsolutePath());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // All done
            Log.i(TAG, "All done, list patch files : ");
            File[] patchs = outputDir.listFiles();
            if (patchs != null) {
                for (File patch : patchs) {
                    Log.i(TAG, patch.getAbsolutePath());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Can not get new apk file.", e);
        }
    }

    private static void diff(File oldFile, File newFile, File patchFile) throws Exception {
        Deflater compressor = new Deflater(9, true);
        try {
            FileOutputStream patchOut = new FileOutputStream(patchFile);
            DeflaterOutputStream compressedPatchOut = new DeflaterOutputStream(patchOut, compressor, 32768);
            new FileByFileV1DeltaGenerator().generateDelta(oldFile, newFile, compressedPatchOut);
            compressedPatchOut.finish();
            compressedPatchOut.flush();
        } finally {
            compressor.end();
        }

    }

    private static void patch(File oldFile, File newFile, File patchFile) throws Exception {
        Inflater uncompressor = new Inflater(true);
        try {
            FileInputStream compressedPatchIn = new FileInputStream(patchFile);
            InflaterInputStream patchIn = new InflaterInputStream(compressedPatchIn, uncompressor, 32768);
            FileOutputStream newFileOut = new FileOutputStream(newFile);
            new FileByFileV1DeltaApplier().applyDelta(oldFile, patchIn, newFileOut);
        } finally {
            uncompressor.end();
        }
    }

    private static String getPatchName(Apk newApk, Apk oldApk) throws Exception {
        return getApkName(oldApk.url) + "_to_" + getApkName(newApk.url) + "_patch";
    }

    private static String getApkName(String url) throws Exception {
        if (url.contains("/") && url.contains(".")) {
            int beginIndex = url.lastIndexOf("/") + 1;
            int endIndex = url.lastIndexOf(".") - 1;
            if (beginIndex < endIndex) {
                return url.substring(beginIndex, endIndex);
            }
        }
        throw new Exception("Bad url, url = " + url);
    }

    private static File getApkFile(Apk apk, File tempDir) throws Exception {
        File apkFile = new File(tempDir, getApkName(apk.url));
        if (apkFile.exists()) {
            String md5 = getMd5(apkFile);
            if (md5.equals(apk.md5)) {
                return apkFile;
            }
        }

        download(apk.url, apkFile);

        String md5 = getMd5(apkFile);
        if (md5.equals(apk.md5)) {
            return apkFile;
        }

        throw new Exception("Bad md5.");
    }

    private static File download(String url, File file) throws IOException {
        Log.d(TAG, "Download file, url = " + url);
        FileUtils.copyURLToFile(new URL(url), file);
        return file;
    }

    private static String getMd5(File file) throws IOException {
        Log.d(TAG, "Get file md5, file = " + file.getAbsolutePath());
        FileInputStream in = new FileInputStream(file);
        String md5Hex = DigestUtils.md5Hex(in);
        in.close();
        return md5Hex;
    }

    public static class Apk {

        public String url;
        public String md5;

        public Apk(String url, String md5) {
            this.url = url;
            this.md5 = md5;
        }

        @Override
        public String toString() {
            return "Apk{" +
                    "url='" + url + '\'' +
                    ", md5='" + md5 + '\'' +
                    '}';
        }
    }
}
