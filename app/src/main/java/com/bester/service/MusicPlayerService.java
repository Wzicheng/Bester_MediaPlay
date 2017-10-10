package com.bester.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.bester.bean.MediaItem;
import com.bester.bester_mediaplay.IMusicPlayerService;
import com.bester.bester_mediaplay.LocalAudioPlayerActivity;
import com.bester.bester_mediaplay.R;
import com.bester.tools.CacheUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Wzich on 2017/9/27.
 */

public class MusicPlayerService extends Service {
    public static final String OPENAUDIO = "com.bester.mobileplay_OPENAUDIO";
    private ArrayList<MediaItem> mediaItems;
    private int position;

    /**
     * 当前播放的音频文件对象
     */
    private MediaItem mediaItem;

    /**
     * 用于播放音乐
     */
    private MediaPlayer mediaPlayer;

    /**
     * Audio 播放模式
     */
    public static final int AUDIOMODE_CYCLELIST = 0; //列表循环模式（默认）
    public static final int AUDIOMODE_CYCLEONLY = 1; //单曲循环模式
    public static final int AUDIOMODE_RANDOM = 2; //随机播放模式
    private int playmode = AUDIOMODE_CYCLELIST;



    @Override
    public void onCreate() {
        super.onCreate();
        playmode = CacheUtils.getPlayMode(this,"playmode");
        //服务被创建的时候加载音乐列表
        getLocalAudio();
    }

    private void getLocalAudio() {
        mediaItems = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;//外部uri
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//视频名称
                        MediaStore.Audio.Media.DURATION,//视频时长
                        MediaStore.Audio.Media.SIZE,//视频文件大小
                        MediaStore.Audio.Media.DATA,//视频的绝对地址
                        MediaStore.Audio.Media.ARTIST//歌曲的演唱者
                };
                Cursor cursor = resolver.query(uri,objs,null,null,null);
                if (cursor != null){
                    while (cursor.moveToNext()){
                        MediaItem medio = new MediaItem();
                        mediaItems.add(medio);

                        String name = cursor.getString(0);
                        medio.setName(name);

                        long duration = cursor.getLong(1);
                        medio.setDuration(duration);

                        long size = cursor.getLong(2);
                        medio.setSize(size);

                        String data = cursor.getString(3);
                        medio.setData(data);

                        String artist = cursor.getString(4);
                        medio.setArtist(artist);
                    }
                    cursor.close();
                }
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {
        //获取服务的实例
        MusicPlayerService service = MusicPlayerService.this;
        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }

        @Override
        public void playNext() throws RemoteException {
            service.playNext();
        }

        @Override
        public void playPrev() throws RemoteException {
            service.playPrev();
        }

        @Override
        public void setPlayMode(int playmode) throws RemoteException {
            service.setPlayMode(playmode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mediaPlayer.isPlaying();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            mediaPlayer.seekTo(position);
        }
    };

    /**
     * 根据位置打开对应的音频文件，并播放
     * @param position
     */
    private void openAudio(int position){
        this.position = position;
        if (mediaItems != null && mediaItems.size() > 0){
            mediaItem = mediaItems.get(position);
            if (mediaPlayer != null){
                mediaPlayer.reset();
            }
            try {
                mediaPlayer = new MediaPlayer();
                //设置监听(准备完成，播放出错，播放完成)
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setDataSource(mediaItem.getData());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(MusicPlayerService.this,"还没加载到数据！",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 播放音乐
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void start(){
        mediaPlayer.start();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, LocalAudioPlayerActivity.class);
        intent.putExtra("Notification",true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.bester_icon)
                .setContentTitle(mediaItem.getName())
                .setOngoing(true)
                .setContentText(mediaItem.getArtist())
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(1,notification);
    }

    /**
     * 播暂停音乐
     */
    private void pause(){
        mediaPlayer.pause();
        manager.cancel(1);
    }

    /**
     * 停止
     */
    private void stop(){
        mediaPlayer.stop();
    }

    /**
     * 得到当前的播放进度
     * @return
     */
    private int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 得到当前音频的总时长
     * @return
     */
    private int getDuration(){
        return mediaPlayer.getDuration();
    }

    /**
     * 得到艺术家
     * @return
     */
    private String getArtist(){
        return mediaItem.getArtist();
    }

    /**
     * 得到歌曲名字
     * @return
     */
    private String getName(){
        return mediaItem.getName();
    }


    /**
     * 得到歌曲播放的路径
     * @return
     */
    private String getAudioPath(){
        return mediaItem.getData();
    }

    /**
     * 播放下一个视频
     */
    private void playNext(){
        getNextAudio();
    }

    private void getNextAudio() {
        playmode = getPlayMode();
        if (playmode == MusicPlayerService.AUDIOMODE_CYCLELIST
                || playmode == MusicPlayerService.AUDIOMODE_CYCLEONLY){
            if (position + 1 <= mediaItems.size()-1){
                position += 1;
            } else {
                position = 0;
            }
            openAudio(position);
        } else if(playmode == MusicPlayerService.AUDIOMODE_RANDOM){
            position += (int) ((Math.random()*10) *  (Math.random()*10));
            position = position % mediaItems.size();
            openAudio(position);
        }
    }


    /**
     * 播放上一个视频
     */
    private void playPrev(){
        getPreAudio();
    }

    private void getPreAudio() {
        playmode = getPlayMode();
        if (playmode == MusicPlayerService.AUDIOMODE_CYCLELIST
                || playmode == MusicPlayerService.AUDIOMODE_CYCLEONLY){
            if (position - 1 >= 0){
                position -= 1;
            } else {
                position = mediaItems.size()-1;
            }
            openAudio(position);
        } else if(playmode == MusicPlayerService.AUDIOMODE_RANDOM){
            position += (int) ((Math.random()*10) *  (Math.random()*10));
            position = position % mediaItems.size();
            openAudio(position);
        }
    }

    /**
     * 设置播放模式
     * @param playmode
     */
    private void setPlayMode(int playmode){
        this.playmode = playmode;
        CacheUtils.putPlayMode(this,"playmode",playmode);
    }

    /**
     * 得到播放模式
     * @return
     */
    private int getPlayMode(){
        return playmode;
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onPrepared(MediaPlayer mp) {
            /**
             * 准备完成以后通过广播通知Activity 来获取数据信息
             */
            EventBus.getDefault().post(mediaItem);
            start();
        }
    }

    private NotificationManager manager;

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            playNext();
            return true;
        }

    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            judgePlayMode();
        }
    }

    private void judgePlayMode() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.AUDIOMODE_CYCLELIST){
            if (position + 1 <= mediaItems.size()-1){
                position += 1;
            } else {
                position = 0;
            }
            openAudio(position);
        } else if(playmode == MusicPlayerService.AUDIOMODE_CYCLEONLY){
            openAudio(position);
        } else if(playmode == MusicPlayerService.AUDIOMODE_RANDOM){
            position += (int) ((Math.random()*10) *  (Math.random()*10));
            position = position % mediaItems.size();
            openAudio(position);
        }
    }
}
