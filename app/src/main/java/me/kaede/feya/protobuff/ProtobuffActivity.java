/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.protobuff;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.squareup.dinosaurs.Dinosaur;
import com.squareup.geology.Period;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kaede.feya.R;

public class ProtobuffActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "Protobuff";

    @BindView(R.id.btn1)
    Button btn1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protobuff);
        ButterKnife.bind(this);
        btn1.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                Dinosaur stegosaurus = new Dinosaur.Builder()
                        .name("Stegosaurus")
                        .period(Period.JURASSIC)
                        .build();
                Log.d(TAG, "dinosaur name = " + stegosaurus.name + ", existed in the "
                        + stegosaurus.period + " period.");
                break;
            default:
                break;
        }
    }
}
