package com.classmatelin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;

public class HelpActivity extends AppCompatActivity {

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        init();
    }
    private void init(){
        toolbar=(Toolbar) findViewById(R.id.help_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("使用帮助");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpActivity.this.finish();
            }
        });
    }
}