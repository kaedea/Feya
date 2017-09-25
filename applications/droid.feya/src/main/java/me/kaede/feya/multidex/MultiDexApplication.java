/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.multidex;

import android.app.Application;
import android.content.Context;

/**
 * Minimal MultiDex capable application. To use the legacy multidex library there is 3 possibility:
 * <ul>
 * <li>Declare this class as the application in your AndroidManifest.xml.</li>
 * <li>Have your {@link Application} extends this class.</li>
 * <li>Have your {@link Application} override attachBaseContext starting with<br>
 * <code>
  protected void attachBaseContext(Context base) {<br>
    super.attachBaseContext(base);<br>
    MultiDex.install(this);
    </code></li>
 *   <ul>
 */
public class MultiDexApplication extends Application {
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }
}
