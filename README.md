# tangping
This is not a really readme but just a instructio of the tangping app.

1. 本软件只在红米note7pro上进行了充分测试，不排除会出现其他机型不匹配的问题；
2. 使用前需要打开手机的“后台弹出界面”权限，小米手机的做法是：长按软件图标——应用信息——权限管理——后台弹出界面，修改为打开即可；
3. 启动软件默认启动语音唤醒功能，下拉通知栏看到躺屏的“语音唤醒正在后台运行中“即可进行语音唤醒；
4. 打开软件下方会出现三个按钮，点击中间按钮并开始说话即可进行语音识别，如说出“打开酷狗”便会打开酷狗音乐；

视频演示链接：

温湿度获取：https://www.bilibili.com/video/BV1Y94y1d7o3?spm_id_from=333.999.0.0

灯和风扇调节：https://www.bilibili.com/video/BV12r4y1n7ce?spm_id_from=333.999.0.0

语音控制：https://www.bilibili.com/video/BV1g44y157P7?spm_id_from=333.999.0.0

① MainActivity
点击左边按钮即可进入主操作界面，点击中间按钮便会进行语音识别，说完后自动停止并将识别结果返回，同时响应命令，如说出"打开酷狗音乐"即可打开酷狗app；下拉通知栏可看到躺屏语音唤醒正在后台运行中

<img src="https://user-images.githubusercontent.com/66213161/159296362-cdc5a867-8f8f-4175-b0f3-8cc925ee2f07.jpg" width=250px><img src="https://user-images.githubusercontent.com/66213161/159298945-9f918048-4077-43f5-88ac-5e074fcc0967.jpg" width=250px>


② 主操作界面（MainActivity2）

<img src="https://user-images.githubusercontent.com/66213161/159297248-85b4fbe5-f933-4e5e-be3d-6feee9dceb20.jpg" width=250px>

③ 风扇调节（FanActivity）

<img src="https://user-images.githubusercontent.com/66213161/159297765-430fa73f-4c9e-442c-bdbe-3011e9ca5f36.jpg" width=250px><img src="https://user-images.githubusercontent.com/66213161/159297790-39b9ac73-694d-4550-b3b0-54bd6a3033fd.jpg" width=250px>

④ 个人中心（UserActivity）

<img src="https://user-images.githubusercontent.com/66213161/159297553-70413ea7-f600-4082-bcaf-d6063ea5f5e4.jpg" width=250px>

⑤ 音乐播放器选择，用于在识别到命令“播放音乐”时系统自动选择的播放器，默认为系统自带播放器。

<img src="https://user-images.githubusercontent.com/66213161/159298310-3ead6e06-609b-4070-b525-d3ee0144b097.jpg" width=250px>
