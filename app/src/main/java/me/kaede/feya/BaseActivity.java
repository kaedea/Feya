package me.kaede.feya;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Kaede on 16/8/11.
 */
public class BaseActivity extends AppCompatActivity {

    protected void toast(String msg) {
        InternalUtils.toast(this, msg);
    }
}
