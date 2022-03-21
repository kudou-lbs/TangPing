package com.classmatelin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import customize.musicPlayerInfos;
import customize.musicPlayerSelectionAdpter;

public class MusicPlayerSelectActivity extends AppCompatActivity {

    String TAG="MUSIC_SELECTION";
    Toolbar toolbar;
    private List<musicPlayerInfos> playerList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player_select);

        init();
    }

    private void init(){
        //设置标题
        toolbar=(Toolbar) findViewById(R.id.MPS_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("音乐播放源");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayerSelectActivity.this.finish();
            }
        });

        getMusicList();
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.music_player_list);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        musicPlayerSelectionAdpter adapter=new musicPlayerSelectionAdpter(playerList);
        recyclerView.setAdapter(adapter);
    }

    private void getMusicList(){
        PackageManager pm=MusicPlayerSelectActivity.this.getPackageManager();
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        //这个似乎是寻找手机里能处理音频的软件
        intent.setDataAndType(Uri.fromFile(new File("")),"audio");

        SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
        String selected=sharedPreferences.getString(MusicPlayerSelectActivity.this.getString(R.string.MP),"音乐");
        if(selected.equals("音乐")){
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putInt(MusicPlayerSelectActivity.this.getString(R.string.MPS),0);
            editor.putString(MusicPlayerSelectActivity.this.getString(R.string.MP),"音乐");
            editor.apply();
        }
        selected=sharedPreferences.getString(MusicPlayerSelectActivity.this.getString(R.string.MP),"音乐");
        List<ResolveInfo> resolveInfoList=pm.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        for(ResolveInfo ri:resolveInfoList){
            if(ri.loadLabel(pm).toString().contains("音乐")){
                Log.d(TAG,"ICON: "+ri.loadIcon(pm));
                Log.d(TAG,"name: "+ri.loadLabel(pm));
                Log.d(TAG,"package name: "+ri.activityInfo.packageName);

                int selectionImageId;
                //Toast.makeText(MusicPlayerSelectActivity.this,selected,Toast.LENGTH_SHORT).show();
                if(ri.loadLabel(pm).toString().equals(selected)){
                    selectionImageId=R.drawable.selected;
                }else{
                    selectionImageId=R.drawable.unselected;
                }

                musicPlayerInfos player=new musicPlayerInfos(ri.loadIcon(pm),
                        ri.loadLabel(pm).toString(),selectionImageId);
                playerList.add(player);
            }
        }
        //这里修改后在MainActivity里也要加上对应选择播放器逻辑
        //接下来该设计点击事件了，内selection不好用

    }
}