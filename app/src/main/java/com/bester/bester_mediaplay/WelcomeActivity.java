package com.bester.bester_mediaplay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * 欢迎页面，延迟2秒跳转到主页面
 */
public class WelcomeActivity extends Activity {
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    private Handler handle = new Handler();
    private Button mBtnWelcomeSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainActivity();
                Log.e(TAG, "当前线程名称==" + Thread.currentThread().getName());
            }
        },3000);
        initView();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //关闭当前activity
        finish();
    }

    @Override
    protected void onDestroy() {
        //移除所有的消息和回调
        handle.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void initView() {
        mBtnWelcomeSkip = (Button) findViewById(R.id.btn_welcome_skip);

        //绑定点击监听
        mBtnWelcomeSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });
    }

}
