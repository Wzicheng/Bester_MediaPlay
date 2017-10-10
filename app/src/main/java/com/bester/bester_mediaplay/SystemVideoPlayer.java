package com.bester.bester_mediaplay;

import com.bester.bean.MediaItem;
import com.bester.tools.Utils;
import com.bester.view.VideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.bester.bester_mediaplay.R.id.system_video_name;


/**
 * Created by Wzich on 2017/9/16.
 */
public class SystemVideoPlayer extends Activity implements View.OnClickListener{
    /**
     * 通知视频更新进度
     */
    private static final int PROGRESS = 1;

    /**
     * 通知隐藏控制面板
     */

    private static final int HIDE_VIDEOCONTROL = 2;
    /**
     * 通知隐藏亮度控制面板
     */
    private static final int HIDE_LIGHTCONTROL = 3;

    /**
     * 通知隐藏音量控制面板
     */
    private static final int HIDE_SOUNDCONTROL = 4;

    /**
     * 通知全屏播放
     */
    private static final int FULLSCREEN = 1;

    /**
     * 通知默认屏幕大小播放
     */
    private static final int DEFAULTSCREEN = 2;

    /**
     * 手势识别标记
     */
    private int GESTURE_FLAG;
    /**
     * 滑动调节进度
     * 滑动调节音量
     * 滑动调节亮度
     */
    private static final int GESTURE_MODIFY_PROGRESS = 1;
    private static final int GESTURE_MODIFY_SOUND = 2;
    private static final int GESTURE_MODIFY_BRIGHT = 3;

    private VideoView mVvVideoPlayer;
    private TextView mSystemVideoName;
    private ImageView mIvBattery;
    private TextView mTvTime;
    private LinearLayout mLlSound;
    private SeekBar mSoundSeekbar;
    private ImageButton mBtnSound;
    private LinearLayout mLlLight;
    private SeekBar mLightSeekbar;
    private TextView mTvInformation;
    private TextView mTvCurrentTime;
    private SeekBar mProgressControl;
    private TextView mVideoTime;
    private Button mBtnBack;
    private Button mBtnPreBig;
    private Button mBtnPlayOrPause;
    private Button mBtnNextBig;
    private Button mBtnFullscreen;

    private LinearLayout mSystemState;
    private LinearLayout mBottomControl;
    private RelativeLayout mRlBuffer;
    private TextView mTvCurspeed;
    private LinearLayout mLlLoading;

    private Utils utils;
    /**
     * 调用声音
     */
    private AudioManager mAudioManager;

    /**
     * 当前系统音量
     */
    private int systemSound;

    /**
     * 系统最大音量
     */
    private int maxSound;

    /**
     * 当前系统屏幕亮度
     */
    private int systemBrightness;


    /**
     * 是否显示video控制面板
     * 默认为不显示
     */
    private boolean isShowControl = false;

    /**
     * 是否全屏显示
     */
    private boolean isFullScreen = true;

    /**
     * 是否静音
     */
    private boolean isMute = false;

    /**
     * 是否为网络资源
     */
    private boolean isNetUri = false;
    /**
     * 手机屏幕的宽高
     */
    private int screen_width;
    private int screen_height;

    /**
     * 视频的真实宽高
     */
    private int video_width;
    private int video_height;

    /**
     * 触摸屏幕后第一次Scroll标志
     */
    private boolean firstScroll;
    /**
     * 触摸按下以后获得的X、Y坐标
     */
    private float oldX;
    private float oldY;

    /**
     * 是否使用系统的监听卡顿
     */
    private boolean isUseSystem = false;

    /**
     * 上一秒播放的进度
     */
    private int pre_time;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){

                case PROGRESS:
                    //得到当前播放进度，并设置到SeekBar
                    int current_time = 1000;
                    current_time += mVvVideoPlayer.getCurrentPosition();
                    mProgressControl.setProgress(current_time);

                    //更新文本播放进度
                    mTvCurrentTime.setText(utils.stringForTime(current_time));

                    //得到系统时间
                    mTvTime.setText(getSystemtTime());

                    //缓存进度的更新
                    if (isNetUri){
                        int buffer = mVvVideoPlayer.getBufferPercentage(); //0~100
                        int totalBuffer = buffer * mProgressControl.getMax();
                        int secondProgress = totalBuffer / 100;
                        mProgressControl.setSecondaryProgress(secondProgress);
                    } else {
                        mProgressControl.setSecondaryProgress(0);
                    }

                    //监听卡
                    if (!isUseSystem){
                        if (mVvVideoPlayer.isPlaying()){
                            int buffer = current_time - pre_time;
                            if (buffer < 500){ //卡顿
                                if (isNetUri){
                                    mRlBuffer.setVisibility(View.VISIBLE);
                                }

                            } else {//不卡顿
                                mRlBuffer.setVisibility(View.GONE);
                            }
                        } else {
                            mRlBuffer.setVisibility(View.GONE);
                        }
                    } else {
                        if (!mVvVideoPlayer.isPlaying()){
                            mRlBuffer.setVisibility(View.GONE);
                        }
                    }
                    pre_time = current_time;
                    if (isNetUri){
                        //1.监听网速，得到网络速度
                        String netSpeed = utils.getNetSpeed(SystemVideoPlayer.this);
                        //2.显示网速
                        mTvCurspeed.setText(netSpeed);
                    }

                    //每秒更新
                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS,1000);
                    break;

                case HIDE_VIDEOCONTROL:
                    hideVideoControl();
                    break;

                case HIDE_LIGHTCONTROL:
                    mLlLight.setVisibility(View.GONE);
                    mTvInformation.setVisibility(View.GONE);
                    break;

                case HIDE_SOUNDCONTROL:
                    mLlSound.setVisibility(View.GONE);
                    break;

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activty_system_video_player);
        initView();
        initData();
        setListener();
        getData();
        setData();
    }



    /**
     * 视频列表
     */
    private ArrayList<MediaItem> mediaItems;
    private int position;
    private Uri uri;
    private void getData() {
        //得到播放地址
        uri = getIntent().getData();
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("mediaItems");
        position = getIntent().getIntExtra("position",0);

        //得到手机屏幕的宽高
        getPhoneScreen();

        //得到系统当前亮度
        try {
            getSystemBrightness();
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        //得到当前系统音量以及最大音量
        systemSound = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxSound = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private void setButtonState() {
        if (mediaItems != null && mediaItems.size() > 0) {
            if (mediaItems.size() == 1) {
                setEnable(false);
            }
            else {
                if (position == 0) {
                    mBtnPreBig.setBackgroundResource(R.drawable.btn_pre_big_unable);
                    mBtnPreBig.setEnabled(false);
                    mBtnNextBig.setBackgroundResource(R.drawable.btn_next_big_selector);
                    mBtnNextBig.setEnabled(true);
                } else if (position == mediaItems.size() - 1) {
                    mBtnPreBig.setBackgroundResource(R.drawable.btn_pre_big_selector);
                    mBtnPreBig.setEnabled(true);
                    mBtnNextBig.setBackgroundResource(R.drawable.btn_next_big_unable);
                    mBtnNextBig.setEnabled(false);
                }
                else {
                    setEnable(true);
                }
            }
        } else if (uri != null){
            setEnable(false);
        }
    }

    private void setEnable(boolean isEnable) {
        if (isEnable){
            mBtnPreBig.setBackgroundResource(R.drawable.btn_pre_big_selector);
            mBtnPreBig.setEnabled(true);
            mBtnNextBig.setBackgroundResource(R.drawable.btn_next_big_selector);
            mBtnNextBig.setEnabled(true);
        } else {
            mBtnPreBig.setBackgroundResource(R.drawable.btn_pre_big_unable);
            mBtnPreBig.setEnabled(false);
            mBtnNextBig.setBackgroundResource(R.drawable.btn_next_big_unable);
            mBtnNextBig.setEnabled(false);
        }
    }


    private void setData() {

        //将系统当前亮度设置到mLightSeekBar
        mLightSeekbar.setProgress(systemBrightness);
        //将当前系统亮度设置为App亮度
        changeAppBrightness(systemBrightness);

        //将当前系统音量、最大音量设置到mSoundSeekbar
        mSoundSeekbar.setMax(maxSound);
        mSoundSeekbar.setProgress(systemSound);

        if (mediaItems != null && mediaItems.size() > 0){
            MediaItem video = mediaItems.get(position);//获得点击的列表中的某一视频
            mSystemVideoName.setText(video.getName());//设置视频名称
            isNetUri = utils.isNetUri(video.getData());
            if (isNetUri){
                mLlLoading.setVisibility(View.VISIBLE);
            }
            mVvVideoPlayer.setVideoPath(video.getData());//设置视频播放位置
        } else if (uri != null) {

            mSystemVideoName.setText(uri.toString());
            isNetUri = utils.isNetUri(uri.toString());
            mVvVideoPlayer.setVideoURI(uri);    //设置播放地址
        } else {
            Toast.makeText(this,"未传递数据！",Toast.LENGTH_SHORT).show();
        }

        //根据得到的列表数据，设置按钮的显示状态
        setButtonState();
    }

    /**
     * 监听电量变化的广播
     */
    private MyReceiver myReceiver;
    private GestureDetector gestureDetector;
    private void initData() {
        utils = new Utils();
        myReceiver = new MyReceiver();
        IntentFilter intentFiler = new IntentFilter();

        //当电量变化的时候发送这个广播
        intentFiler.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(myReceiver,intentFiler);

        //音量控制初始化
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //实例化手势识别器，并重写双击，单击，拖拽方法
        gestureDetector = new GestureDetector(this,new MySimpleOnGestureListener());
    }

    /**
     * 将事件传递给手势识别器
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (GESTURE_FLAG == GESTURE_MODIFY_PROGRESS){
                mVvVideoPlayer.start();
                mBtnPlayOrPause.setBackgroundResource(R.drawable.btn_pause_selector);
                handler.sendEmptyMessageDelayed(HIDE_VIDEOCONTROL,3000);
            } else if(GESTURE_FLAG == GESTURE_MODIFY_BRIGHT) {
               handler.sendEmptyMessage(HIDE_LIGHTCONTROL);
            } else if (GESTURE_FLAG == GESTURE_MODIFY_SOUND){
                handler.sendEmptyMessageDelayed(HIDE_SOUNDCONTROL,3000);
                mTvInformation.setVisibility(View.GONE);
            }
            oldX = 0;
            firstScroll = false;
            GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量、亮度或进度的标志
        }
        return super.onTouchEvent(event);
    }

    /**
     * 设置监听
     */
    private void setListener() {
        //设置点击监听
        mBtnBack.setOnClickListener(this);
        mBtnPreBig.setOnClickListener(this);
        mBtnPlayOrPause.setOnClickListener(this);
        mBtnNextBig.setOnClickListener(this);
        mBtnFullscreen.setOnClickListener(this);
        mBtnSound.setOnClickListener(this);

        //设置ProgressControl监听
        mProgressControl.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());
        mLightSeekbar.setOnSeekBarChangeListener(new LightOnSeekBarChangeListener());
        mSoundSeekbar.setOnSeekBarChangeListener(new SoundOnSeekBarChangeListener());

        //设置准备完成监听
        mVvVideoPlayer.setOnPreparedListener(new MyOnPreparedListener());
        //设置播放错误监听
        mVvVideoPlayer.setOnErrorListener(new MyOnErrorListener());
        //设置播放完成监听
        mVvVideoPlayer.setOnCompletionListener(new MyOnCompletionListener());

        //监听视频播放卡顿,4.2版本以上才能直接使用此监听
        if (isUseSystem){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mVvVideoPlayer.setOnInfoListener(new MyOnInfoListener());
            }
        }


    }

    /**
     * 点击监听
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_pre_big:
                getPreVideo();
                break;
            case R.id.btn_play_or_pause:
                setBtnPlay_Pause();
                break;
            case R.id.btn_next_big:
                getNextVideo();
                break;
            case R.id.btn_fullscreen:
                judgeScreen();
                break;
            case R.id.btn_sound:
                handler.removeMessages(HIDE_SOUNDCONTROL);
                isMute = !isMute;
                changeAppSound(systemSound,isMute);
                break;
        }
    }


    /**
     * 设置是全屏播放
     */
    private void setScreenType(int screen) {
        switch (screen) {
            case 1://设置全屏播放
                mVvVideoPlayer.setVideoSize(screen_width,screen_height);
                mBtnFullscreen.setBackgroundResource(R.drawable.btn_defaultscreen_selector);
                isFullScreen = true;
                break;
            case 2://设置默认屏幕大小播放
                int mVideoWidth = video_width;
                int mVideoHeight = video_height;
                int width = screen_width;
                int height = screen_height;

                if ( mVideoWidth * height  < width * mVideoHeight ) {
                    width = height * mVideoWidth / mVideoHeight;
                } else if ( mVideoWidth * height  > width * mVideoHeight ) {
                    height = width * mVideoHeight / mVideoWidth;
                }
                mVvVideoPlayer.setVideoSize(width,height);
                mBtnFullscreen.setBackgroundResource(R.drawable.btn_fullscreen_selector);
                isFullScreen = false;

                break;
        }
        showVideoControl();
    }

    /**
     * 下一个视频
     */
    private void getNextVideo() {
        if (mediaItems != null && mediaItems.size() > 0){
            position ++;
            if (position < mediaItems.size()){
                MediaItem video = mediaItems.get(position);
                mSystemVideoName.setText(video.getName());
                isNetUri = utils.isNetUri(video.getData());
                if (isNetUri){
                    mLlLoading.setVisibility(View.VISIBLE);
                }
                mVvVideoPlayer.setVideoPath(video.getData());
                setButtonState();
            } else {
                mBtnNextBig.setBackgroundResource(R.drawable.btn_next_big_unable);
                mBtnNextBig.setEnabled(false);
            }
        } else if (uri != null){
            isNetUri = utils.isNetUri(uri.toString());
            if (isNetUri){
                mLlLoading.setVisibility(View.VISIBLE);
            }
            setButtonState();
        }
        handler.removeMessages(HIDE_VIDEOCONTROL);
        handler.sendEmptyMessageDelayed(HIDE_VIDEOCONTROL,4000);
    }

    /**
     * 上一个视频
     */
    private void getPreVideo() {
        if (mediaItems != null && mediaItems.size() > 0){
            position --;
            if (position >=0 ){
                MediaItem video = mediaItems.get(position);
                mSystemVideoName.setText(video.getName());
                isNetUri = utils.isNetUri(video.getData());
                if (isNetUri){
                    mLlLoading.setVisibility(View.VISIBLE);
                }
                mVvVideoPlayer.setVideoPath(video.getData());
                setButtonState();
            } else {
                mBtnPreBig.setBackgroundResource(R.drawable.btn_pre_big_unable);
                mBtnPreBig.setEnabled(false);
            }
        } else if (uri != null){
            isNetUri = utils.isNetUri(uri.toString());
            if (isNetUri){
                mLlLoading.setVisibility(View.VISIBLE);
            }
            setButtonState();
        }
        handler.removeMessages(HIDE_VIDEOCONTROL);
        handler.sendEmptyMessageDelayed(HIDE_VIDEOCONTROL,4000);
    }

    /**
     * 设置播放、暂停
     */
    private void setBtnPlay_Pause() {
        //判断当前视频是否正在播放
        if (mVvVideoPlayer.isPlaying()){
            //设置暂停，并将按钮状态设置为播放
            mVvVideoPlayer.pause();
            mBtnPlayOrPause.setBackgroundResource(R.drawable.btn_play_selector);
        } else {
            //设置播放，并将按钮状态设置为暂停
            mVvVideoPlayer.start();
            mBtnPlayOrPause.setBackgroundResource(R.drawable.btn_pause_selector);
        }
        handler.removeMessages(HIDE_VIDEOCONTROL);
        handler.sendEmptyMessageDelayed(HIDE_VIDEOCONTROL,4000);
    }

    /**
     * 得到系统当前亮度
     * @return
     * @throws Settings.SettingNotFoundException
     */
    public int getSystemBrightness() throws Settings.SettingNotFoundException {
        systemBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        return systemBrightness;
    }

    /**
     * 广播监听电池电量变化
     */
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取当前的电量
            int level = intent.getIntExtra("level",0);//0 ~ 100
            setBattery(level);
        }
    }

    /**
     * 根据系统电量显示
     * @param level
     */
    private void setBattery(int level) {
        if (level <= 0){
            mIvBattery.setImageResource(R.drawable.ic_battery_0);
        } else if(level <= 10){
            mIvBattery.setImageResource(R.drawable.ic_battery_10);
        } else if(level <= 20){
            mIvBattery.setImageResource(R.drawable.ic_battery_20);
        } else if(level <= 40){
            mIvBattery.setImageResource(R.drawable.ic_battery_40);
        } else if(level <= 60){
            mIvBattery.setImageResource(R.drawable.ic_battery_60);
        } else if(level <= 80){
            mIvBattery.setImageResource(R.drawable.ic_battery_80);
        } else if(level <= 100){
            mIvBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            mIvBattery.setImageResource(R.drawable.ic_battery_100);
        }

    }

    /**
     * 视频解码准备完成
     */
    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        //当底层解码准备好的时候
        @Override
        public void onPrepared(MediaPlayer mp) {
            /**
             * 得到视频的宽高
             */
            video_width = mp.getVideoWidth();
            video_height = mp.getVideoHeight();

            /**
             * 设置默认播放
             */
            setScreenType(DEFAULTSCREEN);
            mVvVideoPlayer.start();//开始播放
            mLlLoading.setVisibility(View.GONE);
            //获取视频总时长，并设置SeeKBar的最大值
            int video_time = mVvVideoPlayer.getDuration();
            mProgressControl.setMax(video_time);
            mVideoTime.setText(utils.stringForTime(video_time));

            handler.sendEmptyMessage(PROGRESS);
            handler.sendEmptyMessageDelayed(HIDE_VIDEOCONTROL,4000);
        }
    }

    /**
     * 视频发生错误
     */
    class MyOnErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            startVitamioVideoPlayer();

            return true;
        }
    }

    /**
     * 无法解析视频格式时，跳转到万能播放器播放
     */
    private void startVitamioVideoPlayer() {
        Toast.makeText(SystemVideoPlayer.this,"视频解析失败,已为您跳转到万能播放器！",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,VitamioVideoPlayer.class);
        if (mVvVideoPlayer != null){
            mVvVideoPlayer.stopPlayback();
        }
        if (mediaItems != null && mediaItems.size() > 0 ){
            Bundle bundle = new Bundle();
            bundle.putSerializable("mediaItems",mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position);
        } else if (uri != null){
            intent.setData(uri);
        }
        startActivity(intent);
        finish();
    }

    /**
     * 视频播放完成
     */
    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (position + 1 <= mediaItems.size()-1){
                position += 1;
            } else {
                position = 0;
                mBtnPreBig.setBackgroundResource(R.drawable.btn_pre_big_unable);
                mBtnPreBig.setEnabled(false);
                mBtnNextBig.setBackgroundResource(R.drawable.btn_next_big_selector);
                mBtnNextBig.setEnabled(true);
            }

            MediaItem video = mediaItems.get(position);
            mVvVideoPlayer.setVideoPath(video.getData());
            mSystemVideoName.setText(video.getName());
        }
    }

    /**
     * 隐藏控制面板
     */
    private void hideVideoControl() {
        mSystemState.setVisibility(View.GONE);
        mBottomControl.setVisibility(View.GONE);

        isShowControl = false;
        handler.removeMessages(HIDE_VIDEOCONTROL);
    }

    /**
     * 显示控制面板
     */
    private void showVideoControl() {
        mSystemState.setVisibility(View.VISIBLE);
        mBottomControl.setVisibility(View.VISIBLE);
        isShowControl = true;
        handler.sendEmptyMessageDelayed(HIDE_VIDEOCONTROL,4000);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mVvVideoPlayer = (VideoView) findViewById(R.id.vv_video_player);
        mSystemVideoName = (TextView) findViewById(system_video_name);
        mIvBattery = (ImageView) findViewById(R.id.iv_battery);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mLlSound = (LinearLayout) findViewById(R.id.ll_sound);
        mSoundSeekbar = (SeekBar) findViewById(R.id.sound_seekbar);
        mBtnSound = (ImageButton) findViewById(R.id.btn_sound);
        mLlLight = (LinearLayout) findViewById(R.id.ll_light);
        mLightSeekbar = (SeekBar) findViewById(R.id.light_seekbar);
        mTvInformation = (TextView) findViewById(R.id.tv_information);
        mTvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        mProgressControl = (SeekBar) findViewById(R.id.progress_control);
        mVideoTime = (TextView) findViewById(R.id.video_time);
        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnPreBig = (Button) findViewById(R.id.btn_pre_big);
        mBtnPlayOrPause = (Button) findViewById(R.id.btn_play_or_pause);
        mBtnNextBig = (Button) findViewById(R.id.btn_next_big);
        mBtnFullscreen = (Button) findViewById(R.id.btn_fullscreen);

        mSystemState = (LinearLayout) findViewById(R.id.system_state);
        mBottomControl = (LinearLayout) findViewById(R.id.bottom_control);
        mRlBuffer = (RelativeLayout) findViewById(R.id.rl_buffer);
        mTvCurspeed = (TextView) findViewById(R.id.tv_curspeed);
        mLlLoading = (LinearLayout) findViewById(R.id.ll_loading);
    }

    /**
     * VideoSeekBar 监听
     */
    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        /**
         * 当SeekBar 的 Progress发生变化的时候回调
         * @param seekBar
         * @param progress
         * @param fromUser  如果使用用户引起的Progress 变化为true，否则为false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser){
                mVvVideoPlayer.seekTo(progress);
                mTvCurrentTime.setText(utils.stringForTime(progress));
            }
        }

        /**
         * 手指触碰的时候回调
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (mVvVideoPlayer.isPlaying()){
                mVvVideoPlayer.pause();
                mBtnPlayOrPause.setBackgroundResource(R.drawable.btn_play_selector);
            }
            handler.removeCallbacksAndMessages(null);
        }

        /**
         * 手指离开的时候回调
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!mVvVideoPlayer.isPlaying()){
                mVvVideoPlayer.start();
                mBtnPlayOrPause.setBackgroundResource(R.drawable.btn_pause_selector);
            }

            handler.sendEmptyMessage(PROGRESS);
        }
    }

    /**
     * LightSeekBar 监听
     */
    class LightOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                changeAppBrightness(progress);
                mTvInformation.setText("亮度：" + progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mTvInformation.setText("亮度：" + mLightSeekbar.getProgress());
            mTvInformation.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mTvInformation.setVisibility(View.GONE);
        }
    }

    /**
     * SoundSeekBar 监听
     */
    private class SoundOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        int soundValue;
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                changeAppSound(progress, isMute);
                mSoundSeekbar.setProgress(progress);
                soundValue = (int) ((double)mSoundSeekbar.getProgress()/(double)mSoundSeekbar.getMax() * 100);
                mTvInformation.setText("音量：" + soundValue +"%");
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            soundValue = (int) ((double)mSoundSeekbar.getProgress()/(double)mSoundSeekbar.getMax() * 100);
            mTvInformation.setText("音量：" + soundValue +"%");
            mTvInformation.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mTvInformation.setVisibility(View.GONE);
        }
    }

    /**
     * 视频卡顿监听
     */
    private class MyOnInfoListener implements MediaPlayer.OnInfoListener {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what){
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://开始卡顿
                    mRlBuffer.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END://卡顿结束
                    mRlBuffer.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    }

    /**
     * 判断当前屏幕播放模式（全屏/默认）
     */
    private void judgeScreen() {
        if (isFullScreen){
            //设置默认屏幕大小播放
            setScreenType(DEFAULTSCREEN);
        } else {
            //设置全屏播放
            setScreenType(FULLSCREEN);
        }
    }

    /**
     * 得到系统时间
     * @return
     */
    private String  getSystemtTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date()) ;
    }

    /**
     * 得到手机屏幕的宽高
     */
    private void getPhoneScreen() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_width = displayMetrics.widthPixels;
        screen_height = displayMetrics.heightPixels;
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        //释放资源的时候先释放子类的资源再释放父类的资源，否则可能出现空指针异常
        if (myReceiver != null){
            unregisterReceiver(myReceiver);
        }
        super.onDestroy();
    }

    /**
     * 改变当前屏幕亮度
     * @param brightness
     */
    public void changeAppBrightness(int brightness) {
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
            lp.screenBrightness = brightness / 255f;
        window.setAttributes(lp);
    }

    /**
     * 改变当前系统音量
     * @param progress
     * @param isMute
     */
    private void changeAppSound(int progress, boolean isMute) {
        if (isMute){
            mBtnSound.setBackgroundResource(R.drawable.btn_no_sound_selector);
            systemSound = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
            mSoundSeekbar.setProgress(0);
            isMute = !isMute;
        } else {
            mBtnSound.setBackgroundResource(R.drawable.btn_sound_selector);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            mSoundSeekbar.setProgress(progress);
        }
    }

    /**
     * 使用手机音量键调节音量
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int value = mSoundSeekbar.getProgress();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            mLlSound.setVisibility(View.VISIBLE);
            mTvInformation.setVisibility(View.VISIBLE);
            value += 1;
            isMute = false;
            if (value >= mSoundSeekbar.getMax()){
                value = mSoundSeekbar.getMax();
            }
            changeAppSound(value,isMute);
            value = (int) ((double)value/(double)mSoundSeekbar.getMax() * 100);
            mTvInformation.setText("音量：" + value + "%");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            mLlSound.setVisibility(View.VISIBLE);
            mTvInformation.setVisibility(View.VISIBLE);
            value -= 1;
            if (value <= 0){
                value = 0;
                isMute = true;
            }
            changeAppSound(value,isMute);
            value = (int) ((double)value/(double)mSoundSeekbar.getMax() * 100);
            mTvInformation.setText("音量：" + value + "%");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            judgeScreen();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isShowControl){
                setBtnPlay_Pause();
            } else {
                showVideoControl();
            }

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (firstScroll) {// 判断是否是第一次触摸屏幕，避免在屏幕上操作切换混乱
                //横向的距离变化大则调整进度，纵向的变化大则调整音量
                if (Math.abs(distanceX) >= Math.abs(distanceY)) {
                    GESTURE_FLAG = GESTURE_MODIFY_PROGRESS;
                    setBtnPlay_Pause();
                    showVideoControl();
                    handler.removeMessages(HIDE_VIDEOCONTROL);
                } else if (Math.abs(distanceX) < Math.abs(distanceY)) {
                    if (oldY > 20){//防止拖出系统自身状态栏时，误操作亮度音量控制
                        if (oldX > 0 && oldX < 0.33 * screen_width ) {
                            handler.removeMessages(HIDE_SOUNDCONTROL);
                            mLlSound.setVisibility(View.GONE);
                            GESTURE_FLAG = GESTURE_MODIFY_BRIGHT;
                            mTvInformation.setVisibility(View.VISIBLE);
                            mLlLight.setVisibility(View.VISIBLE);
                        } else if (oldX > 0.66 * screen_width && oldX < screen_width) {
                            handler.removeMessages(HIDE_SOUNDCONTROL);
                            GESTURE_FLAG = GESTURE_MODIFY_SOUND;
                            mTvInformation.setVisibility(View.VISIBLE);
                            mLlSound.setVisibility(View.VISIBLE);
                        }
                    }
                }
                firstScroll = false;
            }

            //调节进度
            int curProgress;
            int curBright;
            int curSound;
            if (GESTURE_FLAG == GESTURE_MODIFY_PROGRESS) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    if (distanceX < 0) {
                        curProgress = mProgressControl.getProgress() + 1000;
                        if (curProgress >= mProgressControl.getMax()) {
                            curProgress = mProgressControl.getMax();
                            mProgressControl.setProgress(curProgress);
                        } else {
                            mProgressControl.setProgress(curProgress);
                        }

                    } else {
                        curProgress = mProgressControl.getProgress() - 1000;
                        if (curProgress <= 0) {
                            curProgress = 0;
                            mProgressControl.setProgress(curProgress);

                        } else {
                            mProgressControl.setProgress(curProgress);
                        }
                    }
                    mTvCurrentTime.setText(utils.stringForTime(mProgressControl.getProgress()));
                    mVvVideoPlayer.seekTo(curProgress);
                    if (mVvVideoPlayer.isPlaying()) {
                        mBtnPlayOrPause.setBackgroundResource(R.drawable.btn_pause_selector);
                    } else {
                        mBtnPlayOrPause.setBackgroundResource(R.drawable.btn_play_selector);
                    }
                }
            }
            //调节亮度
            else if (GESTURE_FLAG == GESTURE_MODIFY_BRIGHT) {
                if (Math.abs(distanceX) < Math.abs(distanceY)) {
                    if (distanceY > 0) {
                        curBright = mLightSeekbar.getProgress() + 5;
                        if (curBright >= mLightSeekbar.getMax()) {
                            curBright = mLightSeekbar.getMax();
                        }
                    } else {
                        curBright = mLightSeekbar.getProgress() - 5;
                        if (curBright <= 0) {
                            curBright = 0;
                        }
                    }
                    changeAppBrightness(curBright);
                    int i = (int) (((double)curBright/255)*100);
                    mTvInformation.setText("亮度：" + i + "%");
                    mLightSeekbar.setProgress(curBright);
                }
            }
            //调节音量
            else if (GESTURE_FLAG == GESTURE_MODIFY_SOUND) {
                if (Math.abs(distanceX) < Math.abs(distanceY)) {
                    if (distanceY > 0 ) {
                        isMute = false;
                        curSound = mSoundSeekbar.getProgress() + 1;
                        if (curSound >= mSoundSeekbar.getMax()){
                            curSound = mSoundSeekbar.getMax();
                        }

                    } else {
                        curSound = mSoundSeekbar.getProgress() - 1;
                        if (curSound <= 0){
                            curSound = 0;
                            isMute = true;
                        }
                    }
                    mSoundSeekbar.setProgress(curSound);
                    changeAppSound(curSound,isMute);
                    int i = (int) ((double)mSoundSeekbar.getProgress()/(double)mSoundSeekbar.getMax() * 100);
                    mTvInformation.setText("音量：" + i + "%");
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            oldX = e.getX();
            oldY = e.getY();
            firstScroll = true;
            return super.onDown(e);
        }
    }
}
