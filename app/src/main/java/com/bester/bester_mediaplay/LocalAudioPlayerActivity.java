package com.bester.bester_mediaplay;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bester.bean.MediaItem;
import com.bester.service.MusicPlayerService;
import com.bester.tools.LogUtil;
import com.bester.tools.LyricUtils;
import com.bester.tools.Utils;
import com.bester.view.ShowLyric;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;



/**
 * Created by Wzich on 2017/9/26.
 */
public class LocalAudioPlayerActivity extends Activity implements View.OnClickListener {
    /**
     * 通知进度更新
     */
    private static final int PROGRESS = 0;
    private static final int SHOW_LYRIC = 1;
    /**
     * 是否来自状态栏
     */
    private boolean notification;
    private LinearLayout mLlAudioTop;
    private ImageButton mBtnAudioBack;
    private LinearLayout mLlAudioInformation;
    private TextView mTvAudioName;
    private TextView mTvAudioArtist;
    private ImageButton mBtnAudioShare;
    private LinearLayout mBottomControl;
    private TextView mTvCurrentTime;
    private SeekBar mProgressControl;
    private TextView mAudioTime;
    private Button mBtnAudioMode;
    private Button mBtnPreBig;
    private Button mBtnPlayOrPause;
    private Button mBtnNextBig;
    private Button mBtnAudioList;
    private ShowLyric mTvShowLyric;

    private Utils utils;

    //得到位置
    private int position;
    //服务的代理类，通过它可以调用服务的方法
    private IMusicPlayerService service;

    private ServiceConnection con = new ServiceConnection() {
        /**
         * 当连接成功的时候回到这个方法
         * @param name
         * @param iBinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            if (service != null) {
                try {
                    if (!notification) {
                        service.openAudio(position);
                    } else {
                        showViewData();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 当断开连接的时候回到这个方法
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (service != null) {
                    service.stop();
                    service = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS:
                    try {
                        //1.得到当前进度
                        int currentPosition = 1000;
                        currentPosition += service.getCurrentPosition();
                        //2.设置进度
                        mProgressControl.setProgress(currentPosition);
                        //3.时间进度更新
                        mTvCurrentTime.setText(utils.stringForTime(currentPosition));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    //4.每秒更新进度
                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
                case SHOW_LYRIC://显示歌词
                    try {
                        //得到当前进度
                        int currentIndex = service.getCurrentPosition();

                        //将当前进度传入ShowLyricView控件，计算高亮当前歌词
                        mTvShowLyric.setShowHighLightLyric(currentIndex);
                        //实时发送消息
                        handler.removeMessages(SHOW_LYRIC);
                        handler.sendEmptyMessage(SHOW_LYRIC);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

            }
        }
    };

    private MyReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        initData();
        initView();
        getData();
        bindAndStartService();
    }

    private void initData() {
        utils = new Utils();
        /**
         * 使用EventBus注册广播
         */
        EventBus.getDefault().register(this);//this 当前类
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showData(null);
        }
    }

        //订阅方法
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = false,priority = 0)
    public void showData(MediaItem mediaItem) {
//        LogUtil.e("showData");
        //发消息开始同步歌词
        showLyric();
        handler.sendEmptyMessage(SHOW_LYRIC);
        showViewData();
        setPlayModeStatue();
    }

    private void showLyric() {
        //解析歌词
        LyricUtils lyricUtils = new LyricUtils();
        if (lyricUtils.isExistsLyric()) {
            handler.sendEmptyMessage(SHOW_LYRIC);
        }

        try {
            String path = service.getAudioPath();//得到歌曲的绝对路径

            //传歌词文件,根据歌曲名查找对应的歌词文件
            path = path.substring(0, path.lastIndexOf("."));
            File file = new File(path + ".lrc");
            if (!file.exists()) {
                file = new File(path + ".txt");
            }
            lyricUtils.readLyricFile(file);//解析歌词

            mTvShowLyric.setLyrics(lyricUtils.getLyrics());

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void showViewData() {
        try {
            mTvAudioArtist.setText(service.getArtist());
            mTvAudioName.setText(service.getName());
            mAudioTime.setText(utils.stringForTime(service.getDuration()));
            mProgressControl.setMax(service.getDuration());
            handler.sendEmptyMessage(PROGRESS);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void bindAndStartService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction("com.bester.mobileplay_OPENAUDIO");
        bindService(intent, con, Context.BIND_AUTO_CREATE);
        startService(intent);//防止实例化多个服务
    }

    /**
     * 得到数据
     */
    private void getData() {
        notification = getIntent().getBooleanExtra("Notification", false);
        if (!notification) {
            position = getIntent().getIntExtra("position", 0);
        }
    }

    private void initView() {
        mLlAudioTop = (LinearLayout) findViewById(R.id.ll_audio_top);
        mBtnAudioBack = (ImageButton) findViewById(R.id.btn_audio_back);
        mLlAudioInformation = (LinearLayout) findViewById(R.id.ll_audio_information);
        mTvAudioName = (TextView) findViewById(R.id.tv_audio_name);
        mTvAudioArtist = (TextView) findViewById(R.id.tv_audio_artist);
        mBtnAudioShare = (ImageButton) findViewById(R.id.btn_audio_share);
        mBottomControl = (LinearLayout) findViewById(R.id.bottom_control);
        mTvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        mProgressControl = (SeekBar) findViewById(R.id.progress_control);
        mAudioTime = (TextView) findViewById(R.id.audio_time);
        mBtnAudioMode = (Button) findViewById(R.id.btn_audio_mode);
        mBtnPreBig = (Button) findViewById(R.id.btn_pre_big);
        mBtnPlayOrPause = (Button) findViewById(R.id.btn_play_or_pause);
        mBtnNextBig = (Button) findViewById(R.id.btn_next_big);
        mBtnAudioList = (Button) findViewById(R.id.btn_audio_list);
        mTvShowLyric = (ShowLyric) findViewById(R.id.tv_showLyric);

        //设置点击监听
        mBtnAudioBack.setOnClickListener(this);
        mBtnAudioShare.setOnClickListener(this);
        mBtnAudioMode.setOnClickListener(this);
        mBtnPreBig.setOnClickListener(this);
        mBtnPlayOrPause.setOnClickListener(this);
        mBtnNextBig.setOnClickListener(this);
        mBtnAudioList.setOnClickListener(this);

        //设置seekbar监听
        mProgressControl.setOnSeekBarChangeListener(new AudioOnSeekBarChangeListener());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_audio_back:
                finish();
                break;
            case R.id.btn_audio_share:
                Toast.makeText(LocalAudioPlayerActivity.this, "点击分享歌曲！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_audio_mode:
                setPlayMode();
                break;
            case R.id.btn_pre_big:
                if (service != null) {
                    try {
                        service.playPrev();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_play_or_pause:
                if (service != null) {
                    setBtnPlay_Pause();
                }
                break;
            case R.id.btn_next_big:
                if (service != null) {
                    try {
                        service.playNext();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_audio_list:

                break;
        }

    }

    private void setPlayMode() {
        try {
            int playmode = service.getPlayMode();
            if (playmode == MusicPlayerService.AUDIOMODE_CYCLELIST) {
                playmode = MusicPlayerService.AUDIOMODE_CYCLEONLY;
                Toast.makeText(LocalAudioPlayerActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
            } else if (playmode == MusicPlayerService.AUDIOMODE_CYCLEONLY) {
                playmode = MusicPlayerService.AUDIOMODE_RANDOM;
                Toast.makeText(LocalAudioPlayerActivity.this, "随机播放", Toast.LENGTH_SHORT).show();
            } else if (playmode == MusicPlayerService.AUDIOMODE_RANDOM) {
                playmode = MusicPlayerService.AUDIOMODE_CYCLELIST;
                Toast.makeText(LocalAudioPlayerActivity.this, "列表循环", Toast.LENGTH_SHORT).show();
            }
            //保存当前播放模式
            service.setPlayMode(playmode);

            //设置图片及打印Toast
            setPlayModeStatue();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置图片及打印Toast
     */
    private void setPlayModeStatue() {
        int playmode = 0;
        try {
            playmode = service.getPlayMode();
            if (playmode == MusicPlayerService.AUDIOMODE_CYCLELIST) {
                mBtnAudioMode.setBackgroundResource(R.drawable.btn_audio_cycle_list_selector);
            } else if (playmode == MusicPlayerService.AUDIOMODE_CYCLEONLY) {
                mBtnAudioMode.setBackgroundResource(R.drawable.btn_audio_cycle_only_selector);
            } else if (playmode == MusicPlayerService.AUDIOMODE_RANDOM) {
                mBtnAudioMode.setBackgroundResource(R.drawable.btn_audio_random_selector);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setBtnPlay_Pause() {
        //判断当前音频是否正在播放
        try {
            if (service.isPlaying()) {
                service.pause();
                mBtnPlayOrPause.setBackgroundResource(R.drawable.btn_play_selector);
            } else {
                service.start();
                mBtnPlayOrPause.setBackgroundResource(R.drawable.btn_pause_selector);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        EventBus.getDefault().unregister(this);
        if (con != null) {
            unbindService(con);
            con = null;
        }
        super.onDestroy();
    }

    class AudioOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        int nProgress;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                nProgress = progress;
                mTvCurrentTime.setText(utils.stringForTime(progress));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(PROGRESS);

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessage(PROGRESS);
            try {
                service.seekTo(nProgress);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
