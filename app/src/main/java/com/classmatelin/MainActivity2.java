package com.classmatelin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initView();

        //隐藏上方标题栏
        ActionBar actionbar=getSupportActionBar();
        if(actionbar!=null){actionbar.hide();}

    }

    private void initView(){
        //点击用户头像图标跳转到用户详情页
        ImageView user_image=(ImageView) findViewById(R.id.main2_user);
        ImageView lightSwitch=(ImageView) findViewById(R.id.light_switch);
        ImageView toFan=(ImageView) findViewById(R.id.toFan);

        user_image.setOnClickListener(this);
        lightSwitch.setOnClickListener(this);
        toFan.setOnClickListener(this);

        TextView brightness=(TextView)findViewById(R.id.txt_thresh);
        SeekBar seekBar=(SeekBar) findViewById(R.id.seekBar_thresh);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brightness.setText("亮度："+(seekBar.getProgress()==0?0:seekBar.getProgress()/30+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                brightness.setText("亮度："+(seekBar.getProgress()==0?0:seekBar.getProgress()/30+1));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main2_user:
                Intent main2user=new Intent(MainActivity2.this, UserActivity.class);
                startActivity(main2user);
                break;
            case R.id.light_switch:
                ImageView lightSwitch=findViewById(R.id.light_switch);
                //当前大灯状态是关，点击打开逻辑
                if(lightSwitch.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.switch_off).getConstantState())){
                    lightSwitch.setImageResource(R.drawable.switch_on);
                }else{
                    lightSwitch.setImageResource(R.drawable.switch_off);
                }
                break;
            case R.id.toFan:
                Intent intentToFan=new Intent(MainActivity2.this, FanActivity.class);
                startActivity(intentToFan);
                break;
            default:

                break;
        }
    }
}