/*
 * Copyright (c) 2017. Kaede <kidhaibara@gmail.com>.
 *
 */

package me.kaede.feya.regex;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.util.Patterns;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kaede
 * @since 17/4/9
 */
@RunWith(AndroidJUnit4.class)
public class RegexTest {

    @Test
    public void testAndroidPattens() {
        Pattern urlPattern = Patterns.WEB_URL;
        Log.d("RegexTest", "Android web url regex patterns = " + urlPattern.pattern());
        System.out.println("Android web url regex patterns = " + urlPattern);
        Matcher matcher = urlPattern.matcher("http://www.kaedea.com");
        Assert.assertTrue(matcher.find());
        matcher = urlPattern.matcher("http://xxx/");
        Assert.assertTrue(!matcher.find());
    }

}
