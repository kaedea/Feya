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

import java.io.IOException;

/**
 * @author Kaede
 * @since date 16/8/24
 */
public class ProtobuffTest extends InstrumentationTestCase {
    public static final String TAG = "ProtobuffTest";

    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    public void testProtobuffWithWire() {
        Dinosaur stegosaurus = new Dinosaur.Builder()
                .name("Stegosaurus")
                .period(Period.JURASSIC)
                .build();
        assertNotNull(stegosaurus);
        Log.d(TAG, "My favorite dinosaur existed in the " + stegosaurus.period + " period.");

        byte[] bytes = Dinosaur.ADAPTER.encode(stegosaurus);

        try {
            Dinosaur stegosaurusBytes = Dinosaur.ADAPTER.decode(bytes);
            assertNotNull(stegosaurusBytes);
            stegosaurus = stegosaurusBytes.newBuilder().period(Period.TRIASSIC).build();
            Log.d(TAG, "My favorite dinosaur existed in the " + stegosaurus.period + " period.");
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
