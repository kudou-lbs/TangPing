package customize;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.classmatelin.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class msgDeal {
    public msgDeal(){};

    private static int order=0;
    private static String TAG="MSGDEAL_LOG";
    private static String broker="tcp://p8aa438a.cn-shenzhen.emqx.cloud:11981";
    private static String topic_order="TOPIC_ORDER";
    private static String topic_data="TOPIC_DATA";
    private static String mqtt_username = "lin";
    private static String mqtt_password = "lin";
    private static String client_id = "mqttx_f291a866";
    private static String publish_id="mqttx_8841f9e5";
    private static int mqtt_port = 11981;
    private static int qos=0;
    private static String content="wtf";
    private static MqttClient client;

    public static void setOrder(int o){
        order=o;
    }
    public static int getOrder() {
        return order;
    }

    //查询请求order
    public static void onRequest(Context context){
        if(order== context.getResources().getInteger(R.integer.GET_TempAndHumid)
                ||order==context.getResources().getInteger(R.integer.GET_LightState)
                ||order==context.getResources().getInteger(R.integer.LightOn)
                ||order==context.getResources().getInteger(R.integer.LightOff)
                ||order==context.getResources().getInteger(R.integer.GET_FanAngle)){
            send();
        }
    }
    //接收数据order
    public static void onReceive(Context context){
        try {
            // host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(broker, client_id, new MemoryPersistence());
            // MQTT的连接设置
            MqttConnectOptions options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName(mqtt_username);
            // 设置连接的密码
            options.setPassword(mqtt_password.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            // 设置回调函数
            client.setCallback(new MqttCallback() {

                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "connectionLost");
                }

                public void messageArrived(String topic, MqttMessage message) {
                    String responseData=new String(message.getPayload());
                    Log.d(TAG, "======监听到来自[" + topic + "]的消息======");
                    Log.d(TAG, "message content:"+new String(message.getPayload()));
                    Log.d(TAG, "============");

                    try{
                        JSONObject jsonObject=new JSONObject(responseData);
                        SharedPreferences sharedPreferences=context.getSharedPreferences("data",MODE_PRIVATE);
                        if(jsonObject.has("tempe")){
                            Log.d("LBSSS","温湿度已获取");
                            String temps=jsonObject.getString("tempe").substring(0,2);
                            String humids=jsonObject.getString("moder").substring(0,2);
                            int temp=Integer.valueOf(temps);
                            int humid=Integer.valueOf(humids);
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            editor.putInt(context.getResources().getString(R.string.INSIDE_TEMP),temp);
                            editor.putInt(context.getResources().getString(R.string.INSIDE_HUMID),humid);
                            editor.apply();
                        }else if(jsonObject.has("light_state")){
                            Log.d("LBSSS","灯状态已获取");
                            Boolean lightOn=jsonObject.getString("light_state").equals("1")?true:false;
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            editor.putBoolean(context.getResources().getString(R.string.LIGHT),lightOn);
                            editor.apply();
                        }else if(jsonObject.has("fan_state")){
                            Log.d("LBSSS","风扇状态已获取");
                            int gear=jsonObject.getInt("fan_state");
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            editor.putInt(context.getResources().getString(R.string.FAN_GEAR),gear);
                            editor.apply();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "deliveryComplete---------"+ token.isComplete());
                }

            });

            // 建立连接
            Log.d(TAG, "连接到 broker: " + broker);
            client.connect(options);

            Log.d(TAG, "连接成功.");
            //订阅消息
            client.subscribe(topic_data, qos);
            Log.d(TAG, "开始监听" + topic_data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //修改请求
    public static void onFanModify(int gear){

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            // 创建客户端
            MqttClient sampleClient = new MqttClient(broker, publish_id, persistence);
            // 创建链接参数
            MqttConnectOptions connOpts = new MqttConnectOptions();
            // 在重新启动和重新连接时记住状态
            connOpts.setCleanSession(false);
            // 设置连接的用户名
            connOpts.setUserName(mqtt_username);
            connOpts.setPassword(mqtt_password.toCharArray());
            // 建立连接
            Log.d(TAG, "连接到 broker: " + broker);
            sampleClient.connect(connOpts);
            Log.d(TAG, "连接成功.");
            // 创建消息
            content="{\"order\" : \""+order+"\",\"fan_gear\" : \""+gear+"\"}";
            MqttMessage message = new MqttMessage(content.getBytes());
            // 设置消息的服务质量
            message.setQos(qos);
            // 发布消息
            Log.d(TAG, "向" + topic_order + "发送消息:" + message);
            sampleClient.publish(topic_order, message);
            // 断开连接
            sampleClient.disconnect();
            // 关闭客户端
            sampleClient.close();
        } catch (MqttException me) {
            Log.d(TAG, "reason " + me.getReasonCode());
            Log.d(TAG, "msg " + me.getMessage());
            Log.d(TAG, "loc " + me.getLocalizedMessage());
            Log.d(TAG, "cause " + me.getCause());
            Log.d(TAG, "excep " + me);
            me.printStackTrace();
        }
    }
    //发送请求
    private static void send(){
        //内存存储
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            // 创建客户端
            MqttClient sampleClient = new MqttClient(broker, publish_id, persistence);
            // 创建链接参数
            MqttConnectOptions connOpts = new MqttConnectOptions();
            // 在重新启动和重新连接时记住状态
            connOpts.setCleanSession(false);
            // 设置连接的用户名
            connOpts.setUserName(mqtt_username);
            connOpts.setPassword(mqtt_password.toCharArray());
            // 建立连接
            Log.d(TAG, "连接到 broker: " + broker);
            sampleClient.connect(connOpts);
            Log.d(TAG, "连接成功.");
            // 创建消息
            content="{\"order\" : \""+order+"\"}";
            MqttMessage message = new MqttMessage(content.getBytes());
            // 设置消息的服务质量
            message.setQos(qos);
            // 发布消息
            Log.d(TAG, "向" + topic_order + "发送消息:" + message);
            sampleClient.publish(topic_order, message);
            // 断开连接
            sampleClient.disconnect();
            // 关闭客户端
            sampleClient.close();
        } catch (MqttException me) {
            Log.d(TAG, "reason " + me.getReasonCode());
            Log.d(TAG, "msg " + me.getMessage());
            Log.d(TAG, "loc " + me.getLocalizedMessage());
            Log.d(TAG, "cause " + me.getCause());
            Log.d(TAG, "excep " + me);
            me.printStackTrace();
        }

    }
    //关闭连接
    public static void onDestroy() throws MqttException {
        if(client!=null){
            client.unsubscribe(topic_data);
            Log.d(TAG,"断开subscribe");
        }
    }
}
