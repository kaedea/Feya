/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.retrofit.ex;

import java.util.Map;

/**
 * @author Kaede
 * @since date 2017/1/11
 */
public class Comment {

    public boolean success;
    public String msg;
    public int status;
    public DataBean data;

    public static class DataBean {

        public int totalPage;
        public int pageSize;
        public int page;
        public int allCount;
        public int totalCount;
        public boolean desc;
        public Map<String, CommentItem> commentContentArr;

        public static class CommentItem {
            public double cid;
            public int quoteId;
            public String content;
            public String postDate;
            public int userID;
            public String userName;
            public String userImg;
            public int count;
            public int deep;
            public int refCount;
            public int ups;
            public int downs;
            public int nameRed;
            public int avatarFrame;
            public boolean isDelete;
            public boolean isUpDelete;
            public int nameType;
            public int verified;
        }
    }
}
