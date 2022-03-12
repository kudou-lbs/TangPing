package com.classmatelin;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.widget.Toast;

public class myApplication extends Application {

    private IntentFilter intentFilter;
    private wakeUpReceiver receiver;
    public final static String wakeUpName="com.tangping.classmateLin.wakeUp";

    @Override
    public void onCreate() {
        super.onCreate();

        intentFilter=new IntentFilter();
        intentFilter.addAction(wakeUpName);
        receiver=new wakeUpReceiver();
        registerReceiver(receiver,intentFilter);
    }

    class wakeUpReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("wtf","收到了广播");
            Intent intent1=new Intent(myApplication.this,MainActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity(intent1);
        }
    }

}
