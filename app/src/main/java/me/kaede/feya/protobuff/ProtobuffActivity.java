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

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kaede.feya.BaseActivity;
import me.kaede.feya.R;

public class ProtobuffActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "Protobuff";

    @BindView(R.id.btn1)
    Button btn1;
    @BindView(R.id.btn2)
    Button btn2;

    private byte[] stegosaurusBytes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protobuff);
        ButterKnife.bind(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
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
                toast("dinosaur name = " + stegosaurus.name + ", existed in the "
                        + stegosaurus.period + " period.");

                stegosaurusBytes = Dinosaur.ADAPTER.encode(stegosaurus);
                String byteContent =  new String(stegosaurusBytes);
                Log.d(TAG, "encoded byte content = " + byteContent);
                toast("encoded byte content = " + byteContent);
                break;

            case R.id.btn2:
                if (stegosaurusBytes == null) {
                    Log.d(TAG, "pls create byte content first.");
                    toast("pls create byte content first.");
                    return;
                }
                try {
                    Dinosaur tyrannosaurus = Dinosaur.ADAPTER.decode(stegosaurusBytes);
                    Log.d(TAG, "dinosaur name = " + tyrannosaurus.name + ", existed in the "
                            + tyrannosaurus.period + " period.");
                    toast("dinosaur name = " + tyrannosaurus.name + ", existed in the "
                            + tyrannosaurus.period + " period.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
