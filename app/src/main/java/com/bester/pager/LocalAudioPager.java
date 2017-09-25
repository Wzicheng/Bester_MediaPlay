package com.bester.pager;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.bester.base.BasePager;
import com.bester.tools.LogUtil;

/**
 * Created by Wzich on 2017/9/12. */

public class LocalAudioPager extends BasePager {
    private TextView textView;

    public LocalAudioPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        LogUtil.e("初始化本地音频页面");
        textView = new TextView(context);
        textView.setTextSize(25);
        textView.setTextColor(Color.RED);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.e("初始化本地音频页面数据");
        textView.setText("本地音频页面");
    }
}
