package com.classmatelin;

import android.Manifest;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import customize.msgDeal;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //联系人信息
    public class ContactInfo {
        private String name;
        private String number;
        public  ContactInfo(String name,String number){
            this.name=name;
            this.number=number;
        }

        public String getName(){return name;}
        public String getNumber(){return number;}
    }

    public static final String TAG = "MAINACTIVITY1";

    protected TextView txtResult;//识别结果
    protected FloatingActionButton microphone;  //语音按钮
    protected FloatingActionButton toMain;  //主界面按钮

    private String res;//语音识别结果
    private String msg_number;//短信联系人电话
    private String msg_name=null;//联系人姓名
    private boolean isMessage=false;//是否是即将发送的消息内容

    private long eventtime = 0;
    private AudioManager vAudioManager = null;

    private EventManager asr;//语音识别核心库
    private EventManager wp;//语音唤醒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //将窗口设置在底部
        getWindow().getAttributes().width = ActionBar.LayoutParams.MATCH_PARENT;
        getWindow().getAttributes().gravity = Gravity.BOTTOM;

        //初始化控件
        initView();

        //获取权限
        initPermission();

        //初始化EventManager对象
        asr = EventManagerFactory.create(this, "asr");
        //注册自己的输出事件类
        asr.registerListener(MyListener); //EventListener 中 onEvent方法

        //初始化EventManager对象
        wp = EventManagerFactory.create(this, "wp");
        //注册监听事件
        wp.registerListener(WakeUpListener);//EventListener 中 onEvent方法
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.microphone:
                txtResult = (TextView) findViewById(R.id.tv_txt);
                asr.send(SpeechConstant.ASR_START, "{}", null, 0, 0);
                break;
            case R.id.Main:
                Intent intent=new Intent(MainActivity.this,MainActivity2.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    //判断服务是否运行（确保每次只调用一次startCommand()）
    private boolean isServiceRunning(final String className){
        ActivityManager activityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info=activityManager.getRunningServices(Integer.MAX_VALUE);
        if(info==null||info.size()==0) return false;
        for(ActivityManager.RunningServiceInfo serviceInfo:info){
            if(className.equals(serviceInfo.service.getClassName())) return true;
        }
        return false;
    }


    //下面是classmateLin的旧代码，躺屏的新代码放在这里上面onCreate()下面

    /**
     * 初始化控件
     */
    private void initView() {
        //判断是否进行了语音唤醒的设置
        SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);  //获取/创建共享数据
        //这里的问题是String.valueOf(rid)，返回的id转成的字符串而不是key对应的value本身，但因为都在文件中担任key，所以又能完美的跑起来，缘分，妙不可言，不改了。
        Boolean wakeUpSet=sharedPreferences.getBoolean(String.valueOf(R.string.WAKEUPSET),false);
        //首次安装应用时启动，未设置语音唤醒数据存储器，开始设置
        if(!wakeUpSet){
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putBoolean(String.valueOf(R.string.WAKEUPSET),true);
            editor.putBoolean(String.valueOf(R.string.ISWAKINGUP),true);
            editor.putInt(getResources().getString(R.string.INSIDE_TEMP),0);
            editor.putInt(getResources().getString(R.string.INSIDE_HUMID),0);
            editor.putInt(getResources().getString(R.string.FAN_GEAR),0);
            //还要加上灯的
            editor.apply();

            //启动服务
            startWakeUp();

        }
        //每次启动时候判断是否设置启动服务，是则启动
        if(sharedPreferences.getBoolean(String.valueOf(R.string.ISWAKINGUP),false)){
            startWakeUp();
            Log.d(TAG,"打开服务");
        }

        //判断是否进行了音乐播放器设置
        Boolean MPSelected=sharedPreferences.getBoolean(MainActivity.this.getString(R.string.MPSet),false);
        if(!MPSelected){
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putBoolean(MainActivity.this.getString(R.string.MPSet),true);
            editor.putString(MainActivity.this.getString(R.string.MP),"音乐");
            editor.apply();
            //写一下关于默认播放器选择的逻辑，
        }

        //获取按钮
        toMain=(FloatingActionButton)findViewById(R.id.Main);
        microphone = (FloatingActionButton) findViewById(R.id.microphone);  //中间麦克风

        //设置点击事件
        toMain.setOnClickListener(this);
        microphone.setOnClickListener(this);
    }

    //调用以启动服务
    void startWakeUp(){
        if(isServiceRunning("com.classmatelin.wakeUpService")){
            Log.d(TAG,"服务早已存在，不作启动");
        }else{
            Intent startWakeUpService = new Intent(MainActivity.this,wakeUpService.class);
            startService(startWakeUpService);
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.SEND_SMS,//
                Manifest.permission.READ_SMS,//
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MEDIA_CONTENT_CONTROL,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.RECORD_AUDIO
        };

        try {
            ActivityCompat.requestPermissions(this, permissions,0x0010);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 自定义输出事件类 EventListener 回调方法
     */

    //唤醒事件
    EventListener WakeUpListener = new EventListener() {
        @Override
        public void onEvent(String name, String params, byte[] bytes, int offset, int length) {
            try {
                //解析json文件
                JSONObject json = new JSONObject(params);
                if ("wp.data".equals(name)) { // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                    String word = json.getString("word"); // 唤醒词
                    //这里我们简单打印出唤醒词
                    Log.d(TAG, word);
                } else if ("wp.exit".equals(name)) {
                    // 唤醒已经停止
                }
            } catch (JSONException e) {
                throw new AndroidRuntimeException(e);
            }
        }
    };

    //识别事件
    EventListener MyListener = new EventListener() {
        @Override
        public void onEvent(String name, String params, byte[] data, int offset, int length) {

            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                // 识别相关的结果都在这里
                if (params == null || params.isEmpty()) {
                    return;
                }
                if (params.contains("\"final_result\"")) {
                    // 一句话的最终识别结果
                    Log.i("ars.event",params);

                    Gson gson = new Gson();
                    ASRresponse asRresponse = gson.fromJson(params, ASRresponse.class);//数据解析转实体bean

                    if(asRresponse == null) return;
                    //从日志中，得出Best_result的值才是需要的，但是后面跟了一个中文输入法下的逗号，
                    if(asRresponse.getBest_result().contains("，")){//包含逗号  则将逗号替换为空格
                        txtResult.setText(asRresponse.getBest_result().replace('，',' ').trim());//替换为空格之后，通过trim去掉字符串的首尾空格
                        res=txtResult.getText().toString();
                        onActivityResult(res);
                    }else {//不包含
                        txtResult.setText(asRresponse.getBest_result().trim());
                        res=txtResult.getText().toString();
                        onActivityResult(res);
                    }
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取温湿度
                msgDeal.setOrder(getResources().getInteger(R.integer.GET_TempAndHumid));
                msgDeal.onReceive(MainActivity.this);
                msgDeal.onRequest(MainActivity.this);
                //灯状态
                msgDeal.setOrder(getResources().getInteger(R.integer.GET_LightState));
                msgDeal.onReceive(MainActivity.this);
                msgDeal.onRequest(MainActivity.this);
                //风扇状态
                msgDeal.setOrder(getResources().getInteger(R.integer.GET_FanAngle));
                msgDeal.onReceive(MainActivity.this);
                msgDeal.onRequest(MainActivity.this);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //发送取消事件
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        //退出事件管理器
        // 必须与registerListener成对出现，否则可能造成内存泄露
        asr.unregisterListener(MyListener);
        wp.unregisterListener(WakeUpListener);
    }


    //根据麦克风输入结果执行动作
    protected void onActivityResult(String result){
        if(isMessage){
            sendMessage(result);
            isMessage=false;
            msg_number=null;
            msg_name=null;
        }
        if(result.contains("灯")){
            SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
            if(result.contains("开")){
                msgDeal.setOrder(3);
                msgDeal.onRequest(MainActivity.this);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean(getResources().getString(R.string.LIGHT),true);
                editor.apply();
            }else if(result.contains("关")){
                msgDeal.setOrder(4);
                msgDeal.onRequest(MainActivity.this);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean(getResources().getString(R.string.LIGHT),false);
                editor.apply();
            }
            return;
        }
        if(result.contains("打开")){
            String appName=result.substring(result.indexOf("开")+1,result.indexOf("。"));
            Log.d("打开：",appName);
            //test
            openApp(appName);
            return;
        }
        if(result.contains("打电话")){
            call();
            return;
        }
        if(result.contains("发短信")){
            getSendMsgContactInfo();
            //发送信息对象有误
            if(msg_number==null)return;

            txtResult.setText("请输入要发送消息的内容");
            return;
        }
        if(res.contains("搜索")){
            String searchContent=res.substring(res.indexOf("索")+1);
            surfTheInternet(searchContent);
            return;
        }
        eventtime = SystemClock.uptimeMillis();
        vAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if(res.contains("播放")){
            //openApp("酷狗音乐");
            if(vAudioManager.isMusicActive()) return;
            else {
                SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
                String s=sharedPreferences.getString(MainActivity.this.getString(R.string.MP),"音乐");
                //Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show();
                if(s.contains("酷狗音乐")){openApp("酷狗音乐");}
                else openApp(s);
                playMusic();
            }
        }
        if(res.contains("暂停")){
            pauseMusic();
        }
        if(res.contains("下一首")) {
            nextMusic();
        }
        if(res.contains("上一首")){
            lastMusic();
        }
        if(res.contains("音量")) {
            if (res.contains("降低") || res.contains("调低")) {
                vAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            }
            if (res.contains("提高")||res.contains("调高")) {
                vAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            }
        }
    }

    //发送短信的内容
    private void sendMessage(String result) {
        if(msg_number==null) return;

        SmsManager manager=SmsManager.getDefault();
        manager.sendTextMessage(msg_number,null,result,null,null);
    }

    //根据语音输入的电话而非姓名进行操作
    private String getNumber(String result){
        if(result.contains("给")){
            String number=res.substring(res.indexOf("给")+1,res.indexOf("。"));
            for(int i=0;i<number.length();++i){
                if(!Character.isDigit(number.charAt(i))){
                    return null;
                }
            }
            return number;
        }
        return null;
    }

    //打开应用
    private void openApp(String appName){
        PackageManager packageManager=MainActivity.this.getPackageManager();
        //获取应用列表
        List<PackageInfo> packageInfos=packageManager.getInstalledPackages(0);
        //在应用列表中查询应用
        for(int i=0;i<packageInfos.size();++i){
            PackageInfo p=packageInfos.get(i);
            String label=packageManager.getApplicationLabel(p.applicationInfo).toString();
            Log.d("tag",label);
            //查询成功启动应用
            if(label.equals(appName)){
                String pName=p.packageName;
                Intent intent=packageManager.getLaunchIntentForPackage(pName);
                startActivity(intent);
                return;
            }
        }
    }

    //获取联系人信息
    private List<ContactInfo> getContactList(Context context){
        List<ContactInfo> lists=new ArrayList();
        Cursor cursor=context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,null,null,null);
        while (cursor.moveToNext()){
            //读取通讯录名字
            String name =cursor.getString(
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            //读取通讯录号码
            String number =cursor.getString(
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            ContactInfo contactInfo=new ContactInfo(name,number);
            lists.add(contactInfo);
        }
        return lists;
    }

    //打电话
    private void call(){
        //获取联系人信息
        List<ContactInfo> contactInfos=getContactList(this);
        //信息为空时返回
        if(contactInfos.isEmpty()){
            return;
        }
        for(ContactInfo contactInfo:contactInfos){
            if(res.contains(contactInfo.getName())||res.contains(contactInfo.getNumber())){
                String number=contactInfo.getNumber();
                Intent intent=new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+number));
                startActivity(intent);
                return;
            }
        }
        if(res.contains("打电话给")){
            String number=getNumber(res);
            if(number==null) return;
            Intent intent=new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:"+number));
            startActivity(intent);
            return;
        }
    }

    //发短信给xxx
    private void getSendMsgContactInfo(){
        List<ContactInfo> contactInfos=getContactList(this);
        if(contactInfos.isEmpty()){
            return;
        }
        for(ContactInfo contactInfoL:contactInfos){
            if(res.contains((contactInfoL.getName()))){
                msg_name=contactInfoL.getName();
                msg_number=contactInfoL.getNumber();
                isMessage=true;
                return;
            }
        }
        if(res.contains("发短信给")){
            msg_number=getNumber(res);
            if(msg_number==null) return;
            isMessage=true;
        }
    }

    //查找
    private void surfTheInternet(String searchContent) {
        Intent intent=new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, searchContent);
        startActivity(intent);
    }

    //播放音乐
    private void playMusic() {
        if (eventtime<=0)return;
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        sendOrderedBroadcast(upIntent, null);
    }

    //暂停音乐
    private void pauseMusic() {
        if (eventtime<=0)return;
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        sendOrderedBroadcast(upIntent, null);
    }

    //上一首
    private void lastMusic() {
        if (eventtime<=0)return;
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        sendOrderedBroadcast(upIntent, null);
    }

    //下一首
    private void nextMusic() {
        if (eventtime<=0)return;
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        sendOrderedBroadcast(upIntent, null);
    }
}