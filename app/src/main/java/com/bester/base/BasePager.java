package com.bester.base;

import android.content.Context;
import android.view.View;

/**
 * Created by Wzich on 2017/9/12.
 */

public abstract class BasePager {

    public final Context context;
    public View rootView;
    public boolean isInitData;

    public BasePager(Context context) {
        this.context = context;
        this.rootView = initView();
    }

    public abstract View initView();

    /**
     * 初始化数据
     */
    public void initData(){

    }
}
