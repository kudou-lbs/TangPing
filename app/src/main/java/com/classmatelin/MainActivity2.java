package com.classmatelin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
        ImageView user_image=(ImageView) findViewById(R.id.main2_user);
        user_image.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main2_user:
                Intent main2user=new Intent(MainActivity2.this, UserActivity.class);
                startActivity(main2user);
                break;
            default:
                break;
        }
    }
}