package com.bester.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bester.bean.Lyric;
import com.bester.tools.DensityUtil;
import com.bester.tools.LogUtil;

import java.util.ArrayList;

/**
 * Created by Wzich on 2017/9/29.
 */

public class ShowLyric extends TextView {
    /**
     * 歌词列表
     */
    private ArrayList<Lyric> lyrics ;
    private Paint paint;
    private Paint whitepaint;

    private int width;
    private int height;
    /**
     * 歌词列表索引
     */
    private int index;

    /**
     * 每行的高
     */
    private float textHeight = 40;

    /**
     * 当前播放进度索引
     */
    private float currentIndex;

    /**
     * 高亮显示时间
     */
    private float sleepTime;

    /**
     * 当前时间戳
     */
    private float timePoint;

    private DensityUtil densityUtil;

    public void setLyrics(ArrayList<Lyric> lyrics) {
        this.lyrics = lyrics;
    }

    /**
     * 设置歌词列表
     * @param context
     */

    public ShowLyric(Context context) {
        this(context,null);
    }

    public ShowLyric(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ShowLyric(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    private void initView(Context context) {
        lyrics = new ArrayList<>();
        //创建画笔
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setTextSize(densityUtil.dip2px(context,20));
        paint.setAntiAlias(true);
        //设置居中对齐
        paint.setTextAlign(Paint.Align.CENTER);

        whitepaint = new Paint();
        whitepaint.setColor(Color.WHITE);
        whitepaint.setTextSize(densityUtil.dip2px(context,16));
        whitepaint.setAntiAlias(true);
        //设置居中对齐
        whitepaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lyrics != null && lyrics.size() > 0){
            //缓缓向上移动
            float plush = 0;
            if (sleepTime == 0){
                plush = 0;
            } else { //缓缓上移
                //当前句所化的时间：休眠时间 = 移动距离：总距离（行高）
                //移动距离 = (当前句所化的时间：休眠时间)*总距离（行高）
                //屏幕坐标 = 行高 + 移动距离
                plush = textHeight + ((currentIndex - timePoint)/sleepTime)*textHeight;
            }
            canvas.translate(0,-plush);
            //绘制当前歌词
            String currentText = lyrics.get(index).getContent();
            canvas.drawText(currentText,width/2,height/2,paint);

            //绘制前面部分
            float tempY = height/2;//当前句的Y坐标
            for (int i = index - 1; i >= 0; i--) {
                String preContent = lyrics.get(i).getContent();
                tempY = tempY - textHeight;
                if (tempY < 0){
                    break;
                }
                canvas.drawText(preContent,width/2,tempY,whitepaint);
            }
            //绘制后面部分
            tempY = height/2;
            for (int i = index + 1; i < lyrics.size(); i++) {
                String nextContent = lyrics.get(i).getContent();
                tempY = tempY + textHeight;
                if (tempY > height){
                    break;
                }
                canvas.drawText(nextContent,width/2,tempY,whitepaint);
            }
        } else {
            //暂无歌词
            canvas.drawText("暂无歌词...",width/2,height/2,paint);
        }
    }

    /**
     * 根据当前播放位置，计算需要高亮显示的歌词
     * @param currentIndex
     */
    public void setShowHighLightLyric(int currentIndex) {
        this.currentIndex = currentIndex;
        if (lyrics == null || lyrics.size() == 0){
//            LogUtil.e("lyric is null");
            return;
        }

        for (int i = 1; i < lyrics.size(); i++) {
            if (currentIndex < lyrics.get(i).getTimePoint()){
                int tempIndex = i - 1;
                if (currentIndex >= lyrics.get(tempIndex).getTimePoint()){
                    //当前正在播放的歌词
                    index = tempIndex;
                    sleepTime = lyrics.get(index).getSleepTime();
                    timePoint = lyrics.get(index).getTimePoint();
                }
            }
        }
        //重新绘制
        invalidate();//在主线程中执行
//        postInvalidate(); //在子线程中执行
    }
}
