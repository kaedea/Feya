package me.kaede.feya.service;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kaede.feya.R;

public class ServiceDemoActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_stop)
    Button btnStop;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_demo);
        ButterKnife.bind(this);

        // listener
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                intent = new Intent(this, LocalService.class);
                startService(intent);
                break;
            case R.id.btn_stop:
                intent = new Intent(this, LocalService.class);
                stopService(intent);
                break;
            default:
                break;
        }
    }
}
