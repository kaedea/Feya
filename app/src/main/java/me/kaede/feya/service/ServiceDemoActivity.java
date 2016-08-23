/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kaede.feya.BaseActivity;
import me.kaede.feya.R;

public class ServiceDemoActivity extends BaseActivity implements View.OnClickListener {
    public static final String TAG = "Service";

    // local service
    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.btn_bind)
    Button btnBind;
    @BindView(R.id.btn_unbind)
    Button btnUnbind;
    @BindView(R.id.btn_stopSelf1)
    Button btnStopSelf1;
    @BindView(R.id.btn_stopSelf2)
    Button btnStopSelf2;

    // remote service
    @BindView(R.id.btn_start_remote)
    Button btnStartRemote;
    @BindView(R.id.btn_stop_remote)
    Button btnStopRemote;
    @BindView(R.id.btn_bind_remote)
    Button btnBindRemote;
    @BindView(R.id.btn_unbind_remote)
    Button btnUnbindRemote;

    private Intent intent;
    private LocalService.LocalBinder localBinder;
    private IRemoteService remoteBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_demo);
        ButterKnife.bind(this);

        // listener
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnBind.setOnClickListener(this);
        btnUnbind.setOnClickListener(this);
        btnStopSelf1.setOnClickListener(this);
        btnStopSelf2.setOnClickListener(this);
        btnStartRemote.setOnClickListener(this);
        btnStopRemote.setOnClickListener(this);
        btnBindRemote.setOnClickListener(this);
        btnUnbindRemote.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localBinder != null) {
            unbindService(localConnection);
        }
        if (remoteBinder != null) {
            unbindService(remoteConnection);
        }
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
            case R.id.btn_bind:
                intent = new Intent(this, LocalService.class);
                bindService(intent, localConnection, BIND_AUTO_CREATE);
                break;
            case R.id.btn_unbind:
                if (localBinder != null) {
                    unbindService(localConnection);
                }
                break;
            case R.id.btn_stopSelf1:
                intent = new Intent(this, LocalService.class);
                intent.putExtra(LocalService.EXTRA_STOP_SELF, 1);
                startService(intent);
                break;
            case R.id.btn_stopSelf2:
                intent = new Intent(this, LocalService.class);
                intent.putExtra(LocalService.EXTRA_STOP_SELF, 2);
                startService(intent);
                break;

            case R.id.btn_start_remote:
                intent = new Intent(this, RemoteService.class);
                startService(intent);
                break;
            case R.id.btn_stop_remote:
                intent = new Intent(this, RemoteService.class);
                stopService(intent);
                break;
            case R.id.btn_bind_remote:
                intent = new Intent(this, RemoteService.class);
                bindService(intent, remoteConnection, BIND_AUTO_CREATE);
                break;
            case R.id.btn_unbind_remote:
                if (remoteBinder != null) {
                    unbindService(remoteConnection);
                }
                break;
            default:
                break;
        }
    }

    private ServiceConnection localConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            localBinder = (LocalService.LocalBinder) service;

            // Tell the user about this for our demo.
            localBinder.getService().toast("call api from local service");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            localBinder = null;
            toast("unbind service");
        }
    };

    private ServiceConnection remoteConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            remoteBinder = IRemoteService.Stub.asInterface(service);

            // Tell the user about this for our demo.
            try {
                // remoteBinder.toast("call api from remote service");
                String msg = remoteBinder.talk("echo hello");
                toast(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            remoteBinder = null;
            toast("unbind remote service");
        }
    };
}
