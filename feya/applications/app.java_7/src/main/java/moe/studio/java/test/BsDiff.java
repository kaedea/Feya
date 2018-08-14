/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package moe.studio.java.test;

import org.apache.http.util.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kaede.
 * @since 2016/12/15.
 */
public class BsDiff {

    private final Runtime mRuntime;
    private ApkInfo mLatestApk;
    private List<ApkInfo> mOldApks;

    public BsDiff() {
        mRuntime = Runtime.getRuntime();
    }

    public void latestApk() {
        try {
            mLatestApk = createPathInfo("/Users/Kaede/Desktop/BSDIFF/4.31.1.apk");
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void oldApks() {
        try {
            File oldDir = new File("/Users/Kaede/Desktop/BSDIFF/old");
            File[] oldApks = oldDir.listFiles();

            for (File item : oldApks) {
                if (item.getName().endsWith(".apk")) {
                    ApkInfo pathInfo = createPathInfo(item.getAbsolutePath());
                    System.out.println(pathInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void diff() {
        // diff("/Users/Kaede/Desktop/BSDIFF/old/4.10.0.apk");
        // diff("/Users/Kaede/Desktop/BSDIFF/old/4.29.0.apk");
        // diff("/Users/Kaede/Desktop/BSDIFF/old/4.31.1.apk");

        patch();
    }

    public void patch() {
        try {
            mOldApks = new ArrayList<ApkInfo>();
            File oldDir = new File("/Users/Kaede/Desktop/BSDIFF/old");
            File[] oldApks = oldDir.listFiles();

            for (File item : oldApks) {
                if (item.getName().endsWith(".apk")) {
                    // BSPATCH
                    File patchDir = new File("/Users/Kaede/Desktop/BSDIFF/patch");
                    File[] patchs = patchDir.listFiles();

                    for (File patch : patchs) {
                        if (patch.getName().contains("_to_")) {
                            if (patch.getName().startsWith(item.getName())) {
                                ApkInfo oldApkInfo = createPathInfo(item.getAbsolutePath());
                                ApkInfo patchInfo = createPathInfo(patch.getAbsolutePath());
                                patchInfo.version = "";
                                oldApkInfo.patchInfo = patchInfo;

                                System.out.println("bspatch " + patch.getAbsolutePath());
                                String mergePath = "/Users/Kaede/Desktop/BSDIFF/merge/" + patch.getName() + "_merge.apk";
                                long currents = System.currentTimeMillis();
                                bspatch(oldApkInfo.path, mergePath, patchInfo.path);
                                oldApkInfo.mergeInfo = createPathInfo(mergePath);
                                oldApkInfo.mergeInfo.timeConsumed = String.format("%06d", System.currentTimeMillis() - currents);

                                mOldApks.add(oldApkInfo);
                                System.out.println(oldApkInfo.toString());
                                System.out.println();
                            }
                        }
                    }
                }
            }
            System.out.println("RESULT");
            for (ApkInfo item : mOldApks) {
                System.out.println(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void diff(String newApk) {
        try {
            mLatestApk = createPathInfo(newApk);
            System.out.println(mLatestApk.toString());
            System.out.println();

            mOldApks = new ArrayList<ApkInfo>();
            File oldDir = new File("/Users/Kaede/Desktop/BSDIFF/old");
            File[] oldApks = oldDir.listFiles();

            for (File item : oldApks) {
                if (item.getName().endsWith(".apk")) {
                    // BSDIFF
                    System.out.println("bsdiff " + item.getAbsolutePath());
                    ApkInfo oldApkInfo = createPathInfo(item.getAbsolutePath());
                    String patchPath = "/Users/Kaede/Desktop/BSDIFF/patch/" + oldApkInfo.version + "_to_"
                            + mLatestApk.version + "_patch";
                    long currents = System.currentTimeMillis();
                    bsdiff(oldApkInfo.path, mLatestApk.path, patchPath);
                    oldApkInfo.patchInfo = createPathInfo(patchPath);
                    oldApkInfo.patchInfo.timeConsumed = String.format("%06d", System.currentTimeMillis() - currents);

                    System.out.println("bspatch " + item.getAbsolutePath());
                    String mergePath = "/Users/Kaede/Desktop/BSDIFF/merge/" + oldApkInfo.version + "_to_"
                            + mLatestApk.version + "_merge.apk";
                    currents = System.currentTimeMillis();
                    bspatch(oldApkInfo.path, mergePath, patchPath);
                    oldApkInfo.mergeInfo = createPathInfo(mergePath);
                    oldApkInfo.mergeInfo.timeConsumed = String.format("%06d", System.currentTimeMillis() - currents);

                    oldApkInfo.mergeInfo.ratio = 1 - ((float) Long.parseLong(oldApkInfo.patchInfo.fileSize.trim()))
                            / ((float) Long.parseLong(oldApkInfo.mergeInfo.fileSize.trim()));

                    mOldApks.add(oldApkInfo);
                    System.out.println(oldApkInfo.toString());
                    System.out.println();
                }
            }

            System.out.println("RESULT");
            for (ApkInfo item : mOldApks) {
                System.out.println(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    private void bsdiff(String old, String latest, String patch) throws Exception {
        String execute = execute(mRuntime, "bsdiff " + old + " " + latest + " " + patch);
    }

    private void bspatch(String old, String latest, String patch) throws Exception {
        String execute = execute(mRuntime, "bspatch " + old + " " + latest + " " + patch);
    }

    public ApkInfo createPathInfo(String path) throws Exception {
        File file = new File(path);
        ApkInfo apkInfo = new ApkInfo();
        apkInfo.version = file.getName();
        apkInfo.fileSize = String.format("%8d", Long.parseLong(getFileSize(file.getAbsolutePath().trim())));
        apkInfo.md5 = getFileMd5(file.getAbsolutePath());
        apkInfo.path = path;
        return apkInfo;
    }

    private String getFileMd5(String path) throws Exception {
        String execute = execute(mRuntime, "md5 " + path);
        return execute.substring(execute.indexOf("= ") + 2);
    }

    private String getFileSize(String path) throws Exception {
        String execute = execute(mRuntime, "ls -l " + path);
        return execute.substring(execute.indexOf("staff  ") + 7, execute.indexOf(" Dec"));
    }

    private String execute(Runtime runtime, String cmd) throws Exception {
        String result = null;
        String error = null;
        BufferedReader stdInput = null;
        BufferedReader stdError = null;

        try {
            String s = null;
            Process p = runtime.exec(cmd);
            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read the output from the command
            StringBuilder sb = new StringBuilder();
            // System.out.println("Execute " + cmd + " :");
            while ((s = stdInput.readLine()) != null) {
                sb.append(s);
                // System.out.println(s);
            }
            result = sb.toString();

            // read any errors from the attempted command
            sb = new StringBuilder();
            // System.out.println("Here is the standard error of the command (if any):");
            while ((s = stdError.readLine()) != null) {
                sb.append(s);
                // System.out.println(s);
            }
            error = sb.toString();

        } catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        } finally {
            if (stdInput != null) {
                stdInput.close();
            }
            if (stdError != null) {
                stdError.close();
            }
        }

        if (!TextUtils.isEmpty(error)) {
            throw new Exception(error);
        }

        return result;
    }

    public static class ApkInfo {
        public String version;
        public String fileSize;
        public String md5;
        public String path;
        public String timeConsumed = String.format("%06d", 0L);

        public ApkInfo patchInfo;
        public ApkInfo mergeInfo;
        public float ratio;

        @Override
        public String toString() {
            String apkInfo = "ApkInfo{" +
                    "version='" + version + '\'' +
                    ", fileSize='" + fileSize + '\'' +
                    ", md5='" + md5 + '\'' +
                    ", path='" + path + '\'' +
                    ", timeConsumed=" + timeConsumed +
                    ", ratio=" + ratio +
                    '}';
            if (patchInfo != null) {
                apkInfo = apkInfo + ", PATCH = " + patchInfo.toString();
            }
            if (mergeInfo != null) {
                apkInfo = apkInfo + ", MERGE = " + mergeInfo.toString();
            }

            return apkInfo;
        }
    }
}
