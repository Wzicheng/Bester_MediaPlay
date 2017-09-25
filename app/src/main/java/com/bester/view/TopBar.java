package com.bester.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bester.bester_mediaplay.R;

/**
 * Created by Wzich on 2017/9/15.
 */

public class TopBar extends LinearLayout implements View.OnClickListener {
    private Context context;

    private View mTvTextSearch;
    private View mRlGame;
    private View mIvHistory;

    /**
     * 在代码中实例化该类的时候调用此方法
     * @param context
     */
    public TopBar(Context context) {
        this(context,null);
    }

    /**
     * 当在布局文件中使用该类的时候，android 系统通过这个构造方法实例化该类
     * @param context
     * @param attrs
     */
    public TopBar(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    /**
     * 当需要设置样式的时候调用此方法
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public TopBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    /**
     * 当布局文件加载完成的时候回调此方法
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //得到孩子的实例
        mTvTextSearch = getChildAt(1);
        mRlGame = getChildAt(2);
        mIvHistory = getChildAt(3);
        //绑定监听事件
        mTvTextSearch.setOnClickListener(this);
        mRlGame.setOnClickListener(this);
        mIvHistory.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_text_search:
                Toast.makeText(context,"search",Toast.LENGTH_SHORT).show();
                break;
            case R.id.rl_game:
                Toast.makeText(context,"game",Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_history:
                Toast.makeText(context,"history",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
