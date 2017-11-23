/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com>.
 *
 */

package me.kaede.feya.regex;

import android.support.test.runner.AndroidJUnit4;
import android.util.Patterns;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Matcher;

/**
 * @author Kaede
 * @since 17/4/9
 */
@RunWith(AndroidJUnit4.class)
public class RegexTest {

    @Test
    public void testAndroidPattens() {
        Matcher matcher = Patterns.WEB_URL.matcher("http://www.kaedea.com");
        Assert.assertTrue(matcher.find());
        matcher = Patterns.WEB_URL.matcher("http://xxx/");
        Assert.assertTrue(!matcher.find());
    }

}
