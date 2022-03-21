package com.classmatelin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.base.Unit;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.HeConfig;
import com.qweather.sdk.view.QWeather;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

import customize.dataView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener{

    //外面空气湿度与温度
    WeatherNowBean.NowBaseBean now;
    private dataView outsizeTemperature,outsizeHumidity;
    private String TAG="MAINACTIVITY2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        intWhether();
        //getWeather();
        initView();

        //隐藏上方标题栏
        ActionBar actionbar=getSupportActionBar();
        if(actionbar!=null){actionbar.hide();}
    }

    //初始化账号信息
    private void intWhether() {
        HeConfig.init("HE2203162056371446","e38aba925bf04739859d4a9f37e04292");
        HeConfig.switchToDevService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWeather();
    }

    private void getWeather() {
        QWeather.getWeatherNow(MainActivity2.this, MainActivity2.this.getString(R.string.GuangZhou), Lang.ZH_HANS, Unit.METRIC, new QWeather.OnResultWeatherNowListener() {

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "getWeather onError: " + e);
            }

            @Override
            public void onSuccess(WeatherNowBean weatherBean) {
                Log.i(TAG, "getWeather onSuccess: " + new Gson().toJson(weatherBean));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK == weatherBean.getCode()) {
                    WeatherNowBean.NowBaseBean now = weatherBean.getNow();
                    outsizeTemperature=(dataView) findViewById(R.id.outsizeTemperature);
                    outsizeHumidity=(dataView) findViewById(R.id.outsizeHumidity);
                    outsizeTemperature.setDataData(now.getTemp()+"°C");
                    outsizeHumidity.setDataData(now.getHumidity()+"%");
                } else {
                    //在此查看返回数据失败的原因
                    Code code = weatherBean.getCode();
                    Log.i(TAG, "failed code: " + code);
                }
            }
        });

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