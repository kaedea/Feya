/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.protobuff;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.squareup.dinosaurs.Dinosaur;
import com.squareup.geology.Period;

/**
 * @author kaede
 * @version date 16/8/24
 */
public class ProtobuffTest extends InstrumentationTestCase {
    public static final String TAG = "ProtobuffTest";

    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    public void testCreateProtobuffInstance(){
        Dinosaur stegosaurus = new Dinosaur.Builder()
                .name("Stegosaurus")
                .period(Period.JURASSIC)
                .build();
        assertNotNull(stegosaurus);
        Log.d(TAG,"My favorite dinosaur existed in the " + stegosaurus.period + " period.");
    }
}
