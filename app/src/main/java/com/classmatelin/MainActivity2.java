package com.classmatelin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import customize.msgDeal;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener{

    //外面空气湿度与温度
    private WeatherNowBean.NowBaseBean now;
    private dataView outsizeTemperature,outsizeHumidity;
    private String TAG="MAINACTIVITY222";
    //获取服务器数据客户端
    private MqttClient client;
    String TOPIC = "TOPIC_DATA";
    int qos = 0;

    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
      public void handleMessage(Message msg){
          SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
          switch(msg.what){
              case 1:
                  dataView insideTemp=(dataView) findViewById(R.id.indoorTemperature);
                  dataView insideHumid=(dataView) findViewById(R.id.indoorHumidity);
                  insideTemp.setDataData(String.valueOf(sharedPreferences.getInt(getResources().getString(R.string.INSIDE_TEMP),0))+"°C");
                  insideHumid.setDataData(String.valueOf(sharedPreferences.getInt(getResources().getString(R.string.INSIDE_HUMID),0))+"%");
                  break;
              case 2:
                  ImageView light=(ImageView) findViewById(R.id.light_switch);
                  Boolean lightOn=sharedPreferences.getBoolean(getResources().getString(R.string.LIGHT),false);
                  if(lightOn){
                      light.setImageResource(R.drawable.switch_on);
                  }else{
                      light.setImageResource(R.drawable.switch_off);

                  }
              default:
                  break;
          }
      }
    };

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

        //新线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取温湿度
                msgDeal.setOrder(getResources().getInteger(R.integer.GET_TempAndHumid));
                msgDeal.onReceive(MainActivity2.this);
                msgDeal.onRequest(MainActivity2.this);
                Message message=new Message();
                message.what=1;
                handler.sendMessage(message);

                //灯状态
                msgDeal.setOrder(getResources().getInteger(R.integer.GET_LightState));
                msgDeal.onReceive(MainActivity2.this);
                msgDeal.onRequest(MainActivity2.this);
                Message m=new Message();
                m.what=2;
                handler.sendMessage(m);
            }
        }).start();
        //订阅消息
//        try {
//            client.subscribe(TOPIC, qos);
//            Log.d(TAG, "开始订阅" + TOPIC);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        //订阅消息
//        try {
//            client.unsubscribe(TOPIC);
//            Log.d(TAG, "关闭订阅" + TOPIC);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
        try {
            msgDeal.onDestroy();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void getWeather() {
        QWeather.getWeatherNow(MainActivity2.this, MainActivity2.this.getString(R.string.GuangZhou), Lang.ZH_HANS, Unit.METRIC, new QWeather.OnResultWeatherNowListener() {

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "getWeather onError: " + e);
            }

            @Override
            public void onSuccess(WeatherNowBean weatherBean) {
                //Log.i(TAG, "getWeather onSuccess: " + new Gson().toJson(weatherBean));
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

        //初始开启
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                insideData();
//            }
//        }).start();

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

    //发送请求

    //测试版本：监听服务器
//    private void insideData(){
//        String broker = "tcp://p8aa438a.cn-shenzhen.emqx.cloud:11981";
//        String clientid = "mqttx_8841f9e6";
//        String userName = "lin";
//        String passWord = "lin";
//        try {
//            // host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
//            client = new MqttClient(broker, clientid, new MemoryPersistence());
//            // MQTT的连接设置
//            MqttConnectOptions options = new MqttConnectOptions();
//            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
//            options.setCleanSession(true);
//            // 设置连接的用户名
//            options.setUserName(userName);
//            // 设置连接的密码
//            options.setPassword(passWord.toCharArray());
//            // 设置超时时间 单位为秒
//            options.setConnectionTimeout(10);
//            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
//            options.setKeepAliveInterval(20);
//            // 设置回调函数
//            client.setCallback(new MqttCallback() {
//
//                public void connectionLost(Throwable cause) {
//                    Log.d(TAG,"connectionLost");
//                }
//
//                public void messageArrived(String topic, MqttMessage message) {
//                    Log.d(TAG, "======监听到来自[" + topic + "]的消息======");
//                    Log.d(TAG, "message content:"+new String(message.getPayload()));
//                    Log.d(TAG, "============");
//                }
//
//                public void deliveryComplete(IMqttDeliveryToken token) {
//                    Log.d(TAG, "deliveryComplete---------"+ token.isComplete());
//                }
//
//            });
//
//            // 建立连接
//            Log.d(TAG, "连接到 broker: " + broker);
//            client.connect(options);
//            //开启订阅
//            client.subscribe(TOPIC, qos);
//            Log.d(TAG, "开始订阅" + TOPIC);
//            Log.d(TAG, "连接成功.");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main2_user:
                Intent main2user=new Intent(MainActivity2.this, UserActivity.class);
                startActivity(main2user);
                break;
            case R.id.light_switch:
                ImageView lightSwitch=findViewById(R.id.light_switch);
                SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
                //当前大灯状态是关，点击打开逻辑
                if(lightSwitch.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.switch_off).getConstantState())){
                    lightSwitch.setImageResource(R.drawable.switch_on);
                    msgDeal.setOrder(3);
                    msgDeal.onRequest(MainActivity2.this);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putBoolean(getResources().getString(R.string.LIGHT),true);
                    editor.apply();
                }else{
                    lightSwitch.setImageResource(R.drawable.switch_off);
                    msgDeal.setOrder(4);
                    msgDeal.onRequest(MainActivity2.this);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putBoolean(getResources().getString(R.string.LIGHT),false);
                    editor.apply();
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