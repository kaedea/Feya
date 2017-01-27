/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.retrofit;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

import me.kaede.feya.retrofit.ex.AcCommentService;
import me.kaede.feya.retrofit.ex.Comment;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

/**
 * @author Kaede
 * @since date 2017/1/11
 */
@RunWith(JUnit4.class)
public class RetrofitApiTest {

    private Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void getGetAcComments() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.acfun.cn/")
                .addConverterFactory(FastJsonConverterFactory.create())
                .build();

        AcCommentService service = retrofit.create(AcCommentService.class);
        Call<Comment> call = service.getComment("3392462", 1);
        Assert.assertNotNull(call);

        Comment body = call.execute().body();
        Assert.assertNotNull(body);
    }

}
