/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.retrofit.ex;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author kaede
 * @version date 2017/1/15
 */ // http://www.acfun.cn/comment_list_json.aspx?contentId=3392462&currentPage=1
public interface AcCommentService {
    @GET("comment_list_json.aspx")
    Call<Comment> getComment(@Query("contentId") String contentId, @Query("currentPage") int page);
}
