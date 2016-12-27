/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package moe.studio.java.test;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;

import moe.studio.java.test.Utils.Logger;

import static moe.studio.java.test.Utils.FileUtils.checkCreateFile;
import static moe.studio.java.test.Utils.FileUtils.closeQuietly;

/**
 * @author Kaede.
 * @version 2016/12/27.
 */
public class Channels {

    private static final String TAG = "channels.helper";
    private static final String DEFAULT_CHANNEL = "master";
    private static final byte[] MAGIC = new byte[]{0x40, 0x42, 0x49, 0x4c, 0x49, 0x21}; //"@BILI!".getBytes();
    private static final byte[] EMPTY_COMMENT = new byte[]{0x00, 0x00};

    public static void go() {
        try {
            File originApk = new File("/Users/Kaede/Desktop/BSDIFF/Channels/no-channel.apk");
            String originMD5 = getFileMD5(originApk);
            Logger.d(TAG, "Origin apk's md5 = " + originMD5);

            String[] channels = new String[]{"aisuru", "beki", "n", "kimi"};
            for (String item : channels) {
                File output = new File("/Users/Kaede/Desktop/BSDIFF/Channels", item + ".apk");
                Utils.FileUtils.copyFile(originApk, output);

                Channels.writeChannel(output, item);
                String channel = Channels.readChannel(output);
                if (!channel.equals(item)) {
                    throw new Exception("Write channel fail.");
                }

                File pick = new File("/Users/Kaede/Desktop/BSDIFF/Channels", "pick_from_" + item + ".apk");
                removeChannel(output, pick);
                if (originApk.length() != pick.length()) {
                    throw new Exception("Remove channel fail.");
                }
                String pickMD5 = getFileMD5(pick);
                Logger.d(TAG, "Picked apk's md5 = " + pickMD5);
                if (!originMD5.equals(pickMD5)) {
                    throw new Exception("MD5 diff.");
                }
            }
        } catch (Exception e) {
            Logger.w(TAG, e);
        }
    }

    static String readChannel(File file) {
        try {
            return readPackageChannel(file);
        } catch (Exception e) {
            e.printStackTrace();
            return DEFAULT_CHANNEL;
        }
    }

    private static String readPackageChannel(File file) throws Exception {
        RandomAccessFile in = null;
        try {
            in = new RandomAccessFile(file, "r");
            long index = in.length();
            byte[] buffer = new byte[MAGIC.length];
            index -= buffer.length /*bytes*/;
            // read magic bytes
            in.seek(index);
            in.readFully(buffer);
            if (!Arrays.equals(MAGIC, buffer)) {
                throw new Exception("No channel.");
            }
            index -= 2 /*sizeof short*/;
            in.seek(index);
            short len = readShort(in); // should equals to (zipCommentLen - MAGIC.length)
            if (len <= 0) {
                throw new Exception("Bad channel.");
            }
            index -= len;
            in.seek(index);
            final short zipCommentLen = readShort(in);
            if (zipCommentLen - len != MAGIC.length) {
                throw new Exception("Bad channel.");
            }
            buffer = new byte[len - 2/*sizeof short*/];
            in.readFully(buffer);
            return new String(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignored) {
            }
        }

        throw new Exception("Bad channel.");
    }

    static void writeChannel(File file, String channel) {
        RandomAccessFile out = null;
        try {
            out = new RandomAccessFile(file, "rw");
            out.seek(file.length() - 2);
            byte[] data = channel.getBytes(Charset.forName("UTF-8"));
            // write zip comment length
            // (content field length + length field length + magic field length)
            writeShort(data.length + 2 + MAGIC.length, out);
            // write content
            writeBytes(data, out);
            // write content length
            writeShort(data.length + 2, out);
            // write magic bytes
            writeBytes(MAGIC, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    static void removeChannel(File originApk, File outputApk) throws Exception {

        try {
            String channel = readPackageChannel(originApk);
            byte[] data = channel.getBytes(Charset.forName("UTF-8"));
            long commentLength = 2 + data.length + 2 + MAGIC.length;
            long nonCommentLength = originApk.length() - commentLength;

            // Copy file.
            checkCreateFile(outputApk);
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(originApk);
                out = new FileOutputStream(outputApk);
                FileDescriptor fd = ((FileOutputStream) out).getFD();
                out = new BufferedOutputStream(out);

                byte[] buffer = new byte[4 * 1024];
                int len, total = 0;
                while (total <= (nonCommentLength - buffer.length)) {
                    len = in.read(buffer);
                    out.write(buffer, 0, len);
                    total += len;
                    out.flush();
                }
                buffer = new byte[(int) (nonCommentLength - total)];
                len = in.read(buffer);
                total += len;
                out.write(buffer, 0, len);
                out.flush();
                fd.sync();

                if (total == nonCommentLength) {
                    out.write(EMPTY_COMMENT);
                    fd.sync();
                }

            } catch (IOException e) {
                Logger.w(TAG, e);
            } finally {
                closeQuietly(in);
                closeQuietly(out);
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (!isNonChannel(originApk)) {
                throw new Exception("Bad channel.");
            }
            // No channel.
        }
    }

    static boolean isNonChannel(File file) {

        RandomAccessFile in = null;
        try {
            in = new RandomAccessFile(file, "r");
            long index = in.length();
            byte[] buffer = new byte[2];
            index -= buffer.length;
            in.seek(index);
            in.readFully(buffer);
            return Arrays.equals(EMPTY_COMMENT, buffer);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignored) {
            }
        }

        return false;
    }

    private static short readShort(DataInput in) throws IOException {
        int b1 = in.readByte();
        int b2 = in.readByte();
        return (short) (b1 + (b2 << 8));
    }

    private static void writeShort(int val, DataOutput out) throws IOException {
        out.write((val >>> 0) & 0xFF);
        out.write((val >>> 8) & 0xFF);
    }

    private static void writeBytes(byte[] data, DataOutput out) throws IOException {
        out.write(data);
    }

    private static String getFileMD5(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        String md5 = DigestUtils.md5Hex(fis);
        closeQuietly(fis);
        return md5;
    }
}
