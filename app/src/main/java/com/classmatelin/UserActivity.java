package com.classmatelin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class UserActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView wakeUpSwitch;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    LinearLayout user2music;
    LinearLayout useHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        init();
    }

    private void init(){
        toolbar=(Toolbar) findViewById(R.id.user_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("个人中心");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserActivity.this.finish();
            }
        });

        //根据当前记录数据设置语音唤醒按钮状态
        wakeUpSwitch=(ImageView) findViewById(R.id.wakeup_switch);
        sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
        if(sharedPreferences.getBoolean(String.valueOf(R.string.ISWAKINGUP),false)){
            wakeUpSwitch.setImageResource(R.drawable.switch_on);
            //editor.putBoolean(String.valueOf(R.string.ISWAKINGUP),true).apply();
        }else{
            wakeUpSwitch.setImageResource(R.drawable.switch_off);
            //以防万一没有设置
            editor=sharedPreferences.edit();
            editor.putBoolean(String.valueOf(R.string.ISWAKINGUP),false).apply();
        }
        wakeUpSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wakeUpSwitch.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.switch_off).getConstantState())){
                    wakeUpSwitch.setImageResource(R.drawable.switch_on);
                    startWakeUp();

                }else{
                    wakeUpSwitch.setImageResource(R.drawable.switch_off);
                    stopWakeUp();
                }
            }
        });

        //设置音乐播放源项点击时间
        user2music=(LinearLayout) findViewById(R.id.user2music);
        user2music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(UserActivity.this,MusicPlayerSelectActivity.class);
                startActivity(intent);
            }
        });

        //使用帮助
        useHelp=(LinearLayout) findViewById(R.id.user2help);
        useHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(UserActivity.this,HelpActivity.class);
                startActivity(intent);
            }
        });
    }

    //判断服务是否在运行
    private boolean isServiceRunning(final String className){
        ActivityManager activityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info=activityManager.getRunningServices(Integer.MAX_VALUE);
        if(info==null||info.size()==0) return false;
        for(ActivityManager.RunningServiceInfo serviceInfo:info){
            if(className.equals(serviceInfo.service.getClassName())) return true;
        }
        return false;
    }

    //开启语音唤醒
    void startWakeUp(){
        if(isServiceRunning("com.classmatelin.wakeUpService")){
        }else{
            Intent startWakeUpService = new Intent(UserActivity.this,wakeUpService.class);
            startService(startWakeUpService);
        }
        editor=sharedPreferences.edit();
        editor.putBoolean(String.valueOf(R.string.ISWAKINGUP),true).apply();
    }
    void stopWakeUp(){
        Intent intent=new Intent(UserActivity.this,wakeUpService.class);
        stopService(intent);
        editor=sharedPreferences.edit();
        editor.putBoolean(String.valueOf(R.string.ISWAKINGUP),false).apply();
    }

}