package com.classmatelin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import customize.ProtractorView;

public class FanActivity extends AppCompatActivity implements View.OnClickListener{

    Toolbar toolbar;
    ProtractorView fanSpeed;
    TextView fanText;
    ImageView fanSwitch;

    //当前风速
    int currentAngle;

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
            }
        });

        fanSwitch.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fan_switch:
                if(fanSwitch.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.switch_off).getConstantState())){
                    fanSwitch.setImageResource(R.drawable.switch_on);
                    fanSpeed.setEnabled(true);
                    currentAngle=180;
                    setFanSpeed();
                    fanText.setText("风速："+currentAngle);
                }else{
                    fanSwitch.setImageResource(R.drawable.switch_off);
                    fanSpeed.setEnabled(false);
                    currentAngle=0;
                    setFanSpeed();
                    fanText.setText("风速："+currentAngle);
                }
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

}