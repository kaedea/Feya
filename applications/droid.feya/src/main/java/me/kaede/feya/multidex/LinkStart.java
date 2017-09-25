/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.multidex;

import android.os.Build;
import android.os.SystemClock;


import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 林库死大多
 *
 * @author Kaede
 * @since date 2016/10/31
 */

class LinkStart {

    final Ticker mTicker;
    final Session mRoot;

    Session mCurrent;
    boolean mIsFinished = true;


    LinkStart() {
        mTicker = new SystemClockMillisTicker();
        mRoot = new Session("WatchCat");
        mCurrent = mRoot;
    }

    public Session getCurrent() {
        return mCurrent;
    }

    public void setCurrent(Session current) {
        mCurrent = current;
    }

    public LinkStart start() {
        mIsFinished = false;
        mRoot.startTime = mTicker.currentTime();
        return this;
    }

    public Session insert(String session) {
        if (!mIsFinished) {
            Session entry = mCurrent.insert(session);
            mCurrent = entry;
            return entry;
        }

        throw new IllegalStateException("this link already is finished.");
    }

    public Session enter(String session) {
        if (!mIsFinished) {
            if (mCurrent == mRoot) {
                throw new IllegalStateException("root session can not have peer.");
            }

            Session entry = mCurrent.enter(session);
            mCurrent = entry;
            return entry;
        }

        throw new IllegalStateException("this link already is finished.");
    }

    public void end(Session session) {
        session.end();
        mCurrent = session;
    }

    public void finish() {
        mRoot.end();
        mIsFinished = true;
    }

    @Override
    public String toString() {
        if (!mIsFinished) {
            return "watch cat is not finished yet.";
        }

        StringBuilder sb = new StringBuilder("watch cat, time unit = " + mTicker.getUnit() + "\n");
        buildString(mRoot, sb);
        return sb.toString();
    }

    private void buildString(Session session, StringBuilder sb) {
        sb.append(session.toString() + "\n");

        if (session.children != null) {
            for (int i = 0; i < session.children.size(); i++) {
                buildString(session.children.get(i), sb);
            }
        }
    }


    public class Session {
        private static final String PREFIX = "----";
        private static final int MAX_TITLE_LENGTH = 30;

        String name;
        long startTime;
        long endTime;

        int path;
        Session parent;
        List<Session> children;

        Session(String name) {
            this.name = name;
            path = 0; // root
        }

        Session attach(Session parent) {
            this.parent = parent;
            return this;
        }

        Session insert(String childName) {
            Session child = new Session(childName).attach(this);
            child.startTime = mTicker.currentTime();
            child.path = this.path + 1;
            addChild(child);
            return child;
        }

        void addChild(Session child) {
            if (children == null) {
                children = new LinkedList<>();
            }
            children.add(child);
        }

        Session enter(String peerName) {
            if (parent == null) {
                throw new IllegalStateException("this method need a nonnull parent.");
            }

            Session next = new Session(peerName).attach(parent);
            next.startTime = mTicker.currentTime();
            next.path = this.path;
            parent.addChild(next);
            return next;
        }

        Session end() {
            this.endTime = mTicker.currentTime();
            return this;
        }

        @Override
        public String toString() {
            String prefix = "";
            if (path > 0) {
                prefix = StringUtils.repeat(PREFIX, path);
            }

            String postfix;
            if (startTime == 0) {
                postfix = "losing record of bgn time";
            } else if (endTime == 0) {
                postfix = "losing record of end time";
            } else {
                postfix = String.valueOf(mTicker.getInterval(startTime, endTime));
            }

            String title = name;
            if (name.length() > MAX_TITLE_LENGTH) {
                title = name.substring(0, MAX_TITLE_LENGTH - 1);
            } else if (name.length() < MAX_TITLE_LENGTH) {
                title = name + StringUtils.repeat(" ", MAX_TITLE_LENGTH - name.length());
            }

            return "|" + prefix + " " + title + " : " + postfix;
        }
    }

    public interface Ticker {
        long currentTime();

        long getInterval(long start, long end);

        TimeUnit getUnit();
    }

    /**
     * Milli Second
     */
    public static class SystemClockMillisTicker implements Ticker {
        @Override
        public long currentTime() {
            return SystemClock.elapsedRealtime();
        }

        @Override
        public long getInterval(long start, long end) {
            return end - start;
        }

        @Override
        public TimeUnit getUnit() {
            return TimeUnit.MILLISECONDS;
        }
    }

    public static class SystemMillisTicker implements Ticker {
        @Override
        public long currentTime() {
            return System.currentTimeMillis();
        }

        @Override
        public long getInterval(long start, long end) {
            return end - start;
        }

        @Override
        public TimeUnit getUnit() {
            return TimeUnit.MILLISECONDS;
        }
    }

    /**
     * Nano Second
     */
    public static class NanoTicker implements Ticker {
        @Override
        public long currentTime() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return SystemClock.elapsedRealtimeNanos();
            } else {
                return System.nanoTime();
            }
        }

        @Override
        public long getInterval(long start, long end) {
            return end - start;
        }

        @Override
        public TimeUnit getUnit() {
            return TimeUnit.NANOSECONDS;
        }
    }
}
