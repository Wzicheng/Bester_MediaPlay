package com.wzc.startallplayer;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void startAllPlayer(View view){
        Intent intent = new Intent();
//        intent.setDataAndType(Uri.parse("http://192.168.56.1:8080/002.mp4"),"video/*");
//        intent.setDataAndType(Uri.parse("http://192.168.56.1:8080/005.mkv"),"video/*");
//        intent.setDataAndType(Uri.parse("http://192.168.56.1:8080/006.avi"),"video/*");
        intent.setDataAndType(Uri.parse("http://192.168.56.1:8080/007.rmvb"),"video/*");
        startActivity(intent);

    }
}
