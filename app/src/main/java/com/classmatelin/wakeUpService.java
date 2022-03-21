package com.classmatelin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class wakeUpService extends Service {

    private static final String TAG=wakeUpService.class.getSimpleName();
    // 语音唤醒对象
    private VoiceWakeuper mIvw;
    private int curThresh=1500;

    //广播发送器
    private LocalBroadcastManager localBroadcastManager;

    public wakeUpService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");

        SpeechUtility.createUtility(wakeUpService.this, SpeechConstant.APPID +"=749f4d3d");
        // 初始化唤醒对象

    }

    private void startWakeUp(){
        mIvw = VoiceWakeuper.getWakeuper();
        if(mIvw!=null){
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, "0");
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH,
                    getExternalFilesDir("msc").getAbsolutePath() + "/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");

            mIvw.startListening(mWakeuperListener);
        }else{
            Toast.makeText(this,"唤醒未初始化",Toast.LENGTH_SHORT).show();
        }
    }

    //返回结果
    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            //PackageManager packageManager=wakeUpService.this.getPackageManager();

            try{
//                String pName=getPackageName();
//                Intent intent=packageManager.getLaunchIntentForPackage(pName);
//                startActivity(intent);
                  //测试：打电话给lmw
//                Intent intent=new Intent(Intent.ACTION_DIAL);
//                intent.setData(Uri.parse("tel:"+"13531532683"));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);

                //2022.03.12：能打开应用，但是只能在点开应用后一段时间内，过了则会只接受信号不进行动作，猜测是安卓系统对App生命周期管理之类照成的，待查。
                Intent intent=new Intent(wakeUpService.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
//                Intent intent=new Intent(myApplication.wakeUpName);
//                sendBroadcast(intent);

                //Toast.makeText(wakeUpService.this,"接收到唤醒命令",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(wakeUpService.this,"启动未成功，但确实是识别到了",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(SpeechError error) {
            wakeUpService.super.onCreate();
        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            switch (eventType) {
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = obj.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA);
                    Log.i(TAG, "ivw audio length: " + audio.length);
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int volume) {

        }
    };

    //资源路径
    private String getResource() {
        return "fo|/data/app/com.iflytek.mscv5plusdemo-QYT9BJQwwTuHkmc43e8k3g==/base.apk|176964|987983";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");

        NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        //构建通知渠道
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel notificationChannel=new NotificationChannel(String.valueOf(R.string.channeled_id),"躺平通知",NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.canBypassDnd();
            notificationChannel.canShowBadge();
            notificationChannel.enableVibration(true);
            notificationChannel.getGroup();

            notificationManager.createNotificationChannel(notificationChannel);
        }

        //构建Notification
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this.getApplicationContext(), String.valueOf(R.string.channeled_id));
        Intent intent1=new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent
                .getActivities(this, 0,new Intent[]{intent1},0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.tp_small))
                .setSmallIcon(R.mipmap.tp_small)
                .setContentTitle("躺屏")
                .setContentText("躺屏语音唤醒正在后台运行中")
                .setWhen(System.currentTimeMillis());

        //通知构建
        Notification notification =builder.build();
        //设置默认提示音
        notification.defaults=Notification.DEFAULT_SOUND;
        //启动图标
        startForeground(603,notification);

        mIvw = VoiceWakeuper.createWakeuper(this, null);
        startWakeUp();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        stopForeground(true);
        if(mIvw!=null){
            mIvw.stopListening();
        }else{
            Toast.makeText(wakeUpService.this,"对象为空",Toast.LENGTH_SHORT).show();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        Log.d(TAG,"onBind");

        throw new UnsupportedOperationException("Not yet implemented");
    }
}