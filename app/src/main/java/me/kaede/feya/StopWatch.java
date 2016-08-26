/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Watch Dog
 * Created by Kaede on 16/8/17.
 */
public class StopWatch implements Serializable {

    protected long startTime;
    protected long endTime;
    protected boolean isEnd;
    protected Ticker ticker;
    protected StringBuilder stringBuilder;

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
        isEnd = false;
        startTime = ticker.currentTime();
        endTime = ticker.currentTime();
        stringBuilder = new StringBuilder(String.format("{%s", name));
        return this;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public StopWatch split() {
        return split(" ");
    }

    public StopWatch split(String tag) {
        return split(tag, ticker.currentTime());
    }

    public StopWatch split(String tag, long splitTime) {
        if (!isEnd) {
            endTime = splitTime;
            long interval = ticker.getInterval(this.startTime, splitTime);
            stringBuilder.append(String.format(" → [%s = %s ms]", tag, interval));
        }
        return this;
    }

    public String end() {
        return end(" ");
    }

    public String end(String tag) {
        if (!isEnd) {
            isEnd = true;
            split(tag);
            long interval = ticker.getInterval(startTime, endTime);
            stringBuilder.append(String.format(" ：all = %s ms}", interval));
        }
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

        long getInterval(long start, long end);

        TimeUnit getUnite();
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
        public long getInterval(long start, long end) {
            return end - start;
        }

        @Override
        public TimeUnit getUnite() {
            return TimeUnit.MILLISECONDS;
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
        public long getInterval(long start, long end) {
            return end - start;
        }

        @Override
        public TimeUnit getUnite() {
            return TimeUnit.NANOSECONDS;
        }
    }
}
