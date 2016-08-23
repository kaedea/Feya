/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Watch Dog
 * Created by Kaede on 16/8/17.
 */
public class StopWatch implements Serializable {
    private long startTime;
    private long splitTime;
    private long endTime;
    private StringBuilder stringBuilder;
    private Ticker ticker;

    /**
     * {name → [tag1 = 100 ms] → [tag1 = 100 ms] ：all = 200 ms}
     */
    public StopWatch() {
        ticker = new MillisTicker();
    }

    public StopWatch(@NonNull Ticker ticker) {
        this.ticker = ticker;
    }

    public StopWatch start(String name) {
        startTime = ticker.currentTime();
        splitTime = ticker.currentTime();
        endTime = ticker.currentTime();
        stringBuilder = new StringBuilder(String.format("{%s", name));
        return this;
    }

    public StopWatch split() {
        return split(" ");
    }

    public StopWatch split(String tag) {
        endTime = ticker.currentTime();
        String interval = ticker.getInterval(splitTime, endTime);
        stringBuilder.append(String.format(" → [%s = %s ms]", tag, interval));
        splitTime = endTime;
        return this;
    }

    public String end() {
        return end(" ");
    }

    public String end(String tag) {
        split(tag);
        endTime = ticker.currentTime();
        String interval = ticker.getInterval(startTime, endTime);
        stringBuilder.append(String.format(" ：all = %s ms}", interval));
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        if (stringBuilder == null) {
            return "not start yet";
        }
        return String.format("stop watch = %s", stringBuilder.toString());
    }

    public interface Ticker {
        long currentTime();

        String getInterval(long start, long end);
    }

    /**
     * Milli Second
     */
    public static class MillisTicker implements Ticker {
        @Override
        public long currentTime() {
            return System.currentTimeMillis();
        }

        @Override
        public String getInterval(long start, long end) {
            return String.valueOf(end - start);
        }
    }

    /**
     * Nano Second
     */
    public static class NanoTicker implements Ticker {
        @Override
        public long currentTime() {
            return System.nanoTime();
        }

        @Override
        public String getInterval(long start, long end) {
            long l = end - start;
            return String.valueOf(l / 1000000f);
        }
    }
}
