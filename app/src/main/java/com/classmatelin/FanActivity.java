package com.classmatelin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import customize.ProtractorView;
import customize.msgDeal;

public class FanActivity extends AppCompatActivity implements View.OnClickListener{

    Toolbar toolbar;
    ProtractorView fanSpeed;
    TextView fanText;
    ImageView fanSwitch;

    //当前风速 & 档位
    int currentAngle;
    int GEAR=0;

    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
            ProtractorView fanSpeed=(ProtractorView) findViewById(R.id.fanSpeed);
            TextView fanText=(TextView) findViewById(R.id.fanSpeedText);
            ImageView fanSwitch=(ImageView) findViewById(R.id.fan_switch);
            switch (msg.what){
                case 1:
                    GEAR=sharedPreferences.getInt(getResources().getString(R.string.FAN_GEAR),0);

                    if(GEAR==0){
                        fanSpeed.setEnabled(false);
                        fanSwitch.setImageResource(R.drawable.switch_off);
                        currentAngle=0;
                        setFanSpeed();
                        fanText.setText("风速："+currentAngle);
                    }else{
                        fanSpeed.setEnabled(true);
                        fanSwitch.setImageResource(R.drawable.switch_on);
                        currentAngle=210-GEAR*60;
                        setFanSpeed();
                        fanText.setText("风速："+currentAngle);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fan);

        init();
    }

    private void init(){
        //actionbar
        toolbar=(Toolbar) findViewById(R.id.fan_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("电风扇");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FanActivity.this.finish();
            }
        });


        //关于风扇速度控制以及开关的一些初始化工作
        fanSpeed=(ProtractorView) findViewById(R.id.fanSpeed);
        fanText=(TextView) findViewById(R.id.fanSpeedText);
        fanSwitch=(ImageView) findViewById(R.id.fan_switch);

        currentAngle=fanSpeed.getAngle();
        fanText.setText("风速："+currentAngle);

        /*
        这里要加上获取服务器信息
        首先判断风扇是否打开
        是则设置为开并enabled
        否则不变
        然后判断数据范围
        setCurrentAngle(int angleFromServer);
        setFanSpeed();
         */

        fanSpeed.setOnProtractorViewChangeListener(new ProtractorView.OnProtractorViewChangeListener() {
            @Override
            public void onProgressChanged(ProtractorView protractorView, int progress, boolean fromUser) {
                currentAngle=fanSpeed.getAngle();
                fanText.setText("风速："+currentAngle);
            }

            @Override
            public void onStartTrackingTouch(ProtractorView protractorView) {
                currentAngle=fanSpeed.getAngle();
                fanText.setText("风速："+currentAngle);
            }

            @Override
            public void onStopTrackingTouch(ProtractorView protractorView) {
                Log.d("FAN_Stop",String.valueOf(currentAngle));
                if(currentAngle>=120)GEAR=1;
                else if(currentAngle>=60) GEAR=2;
                else if(currentAngle>=0)GEAR=3;
                sendMsg(GEAR);
            }
        });

        fanSwitch.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                msgDeal.setOrder(getResources().getInteger(R.integer.GET_FanAngle));
                msgDeal.onReceive(FanActivity.this);
                msgDeal.onRequest(FanActivity.this);
                Message message=new Message();
                message.what=1;
                handler.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fan_switch:
                if(fanSwitch.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.switch_off).getConstantState())){
                    fanSwitch.setImageResource(R.drawable.switch_on);
                    fanSpeed.setEnabled(true);
                    currentAngle=180;
                    GEAR=1;
                    setFanSpeed();
                    fanText.setText("风速："+currentAngle);
                }else{
                    fanSwitch.setImageResource(R.drawable.switch_off);
                    fanSpeed.setEnabled(false);
                    currentAngle=0;
                    GEAR=0;
                    setFanSpeed();
                    fanText.setText("风速："+currentAngle);
                }
                sendMsg(GEAR);
                break;
            default:
                break;
        }
    }

    //调用，物理更改的时候参数通过服务器传回则调用这个函数设置当前风速
    private void setCurrentAngle(int angle){
        currentAngle=angle;
    }

    //就算enabled==false，也是可以通过主动进行调节的，物理关闭后可以直接通过服务器把指针指向0
    private void setFanSpeed(){
        fanSpeed.setAngle(currentAngle);
    }

    private int getCurrentAngle(){
        return currentAngle;
    }

    private void sendMsg(int gear){
        msgDeal.setOrder(getResources().getInteger(R.integer.ModifyFanAngle));
        msgDeal.onFanModify(gear);
    }

}