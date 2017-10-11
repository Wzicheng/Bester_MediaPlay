package com.bester.bester_mediaplay;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bester.base.BasePager;
import com.bester.pager.LocalAudioPager;
import com.bester.pager.LocalVideoPager;
import com.bester.pager.NetAudioPager;
import com.bester.pager.NetVideoPager;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Wzich on 2017/9/12.
 */
public class MainActivity extends FragmentActivity {
    private RadioGroup mRgBottomTab;
    /**
     * 页面集合
     */
    private List<BasePager> basePagers;
    /**
     * 页面位置
     */
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        basePagers = new ArrayList<>();
        //添加本地视频页面
        basePagers.add(new LocalVideoPager(this));
        //添加本地音频页面
        basePagers.add(new LocalAudioPager(this));
        //添加网络视频页面
        basePagers.add(new NetVideoPager(this));
        //添加网络音频页面
        basePagers.add(new NetAudioPager(this));

        mRgBottomTab.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
        mRgBottomTab.check(R.id.rb_local_video);

    }

   class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                default:
                    position = 0;
                    break;
                case R.id.rb_local_audio:
                    position = 1;
                    break;
                case R.id.rb_net_video:
                    position = 2;
                    break;
                case R.id.rb_net_audio:
                    position = 3;
                    break;
            }
            setFragment();
        }
    }

    /**
     * 设置Fragment 中的页面
     */
    private void setFragment() {
        //创建管理器
        FragmentManager maneger = getSupportFragmentManager();
        //开启事务
        FragmentTransaction transaction = maneger.beginTransaction();
        //替换
        transaction.replace(R.id.fl_main,new ReplaceFragment(getBasePager()));
        //提交事务
        transaction.commit();
    }

    /**
     * 根据位置绑定相应的页面
     * @return
     */
    private BasePager getBasePager() {
        BasePager basePager = basePagers.get(position);
        if (basePager != null && !basePager.isInitData){
            basePager.initData();
            basePager.isInitData = true;
        }
        return basePager;
    }


    /**
     * 初始化
     */
    private void initView() {
        mRgBottomTab = (RadioGroup) findViewById(R.id.rg_bottom_tab);
    }

    /**
     * 判断是否退出
     */
    private boolean isExit;

    /**
     * 添加双击退出功能
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (position != 0){
                mRgBottomTab.check(R.id.rb_local_video);//首页
                return true;
            } else if (!isExit){
                isExit = true;
                Toast.makeText(MainActivity.this,"2秒内再次点击退出程序",Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                },2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
