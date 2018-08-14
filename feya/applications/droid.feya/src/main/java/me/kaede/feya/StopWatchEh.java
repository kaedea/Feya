/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Watch Dog enhanced with tags holder.
 * Created by Kaede on 16/8/17.
 */
public class StopWatchEh extends StopWatch {

    protected Map<String, Long> tagMaps;

    /**
     * {name → [tag1 = 100 ms] → [tag1 = 100 ms] ：all = 200 ms}
     */
    public StopWatchEh() {
        ticker = new MillisTicker();
        tagMaps = new HashMap<>();
    }

    public StopWatchEh(@NonNull Ticker ticker) {
        this.ticker = ticker;
        tagMaps = new HashMap<>();
    }

    @Override
    public StopWatchEh split(String tag, long splitTime) {
        if (!isEnd) {
            endTime = splitTime;
            long interval = ticker.getInterval(this.startTime, splitTime);
            tagMaps.put(tag, interval);
            stringBuilder.append(String.format(" → [%s = %s ms]", tag, interval));
        }
        return this;
    }

    @Override
    public String end(String tag) {
        if (!isEnd) {
            isEnd = true;
            split(tag);
            long interval = ticker.getInterval(startTime, endTime);
            stringBuilder.append(String.format(" ：all = %s ms}", interval));
            tagMaps.put(tag, interval);
        }
        return stringBuilder.toString();
    }

    public long getTag(String tag) {
        return tagMaps.get(tag);
    }

    public Map<String, Long> getTagMaps() {
        return tagMaps;
    }
}
