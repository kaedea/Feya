/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.protobuff;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.squareup.dinosaurs.Dinosaur;
import com.squareup.geology.Period;

import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

                byte[] stegosaurusBytes = Dinosaur.ADAPTER.encode(stegosaurus);
                String byteContent = new String(stegosaurusBytes);
                Log.d(TAG, "encoded byte content = " + byteContent);
                toast("encoded byte content = " + byteContent);

                Log.d(TAG, "write encoded bytes to file");
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(
                            Environment.getExternalStorageDirectory() + File.separator + "dinosaur_byte"));
                    bos.write(stegosaurusBytes);
                    bos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (IOException ignore) {
                        }
                    }
                }

                break;

            case R.id.btn2:
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "dinosaur_byte");
                if (!file.exists()) {
                    Log.d(TAG, "pls create byte content first.");
                    toast("pls create byte content first.");
                    return;
                }
                try {
                    Log.d(TAG, "read encoded bytes from file.");
                    byte[] bytes = FileUtils.readFileToByteArray(file);
                    Dinosaur tyrannosaurus = Dinosaur.ADAPTER.decode(bytes);
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
