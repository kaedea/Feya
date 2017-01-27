package com.kaedea.media;

import android.app.Application;

import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.kaedea.media.mediaplayer.BoxingFrescoLoader;

/**
 * @author Kaede
 * @since 2017/1/24
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        IBoxingMediaLoader loader = new BoxingFrescoLoader(this);
        BoxingMediaLoader.getInstance().init(loader);
    }
}
