package com.bester.pager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bester.adapter.VideoAdapter;
import com.bester.base.BasePager;
import com.bester.bean.MediaItem;
import com.bester.bester_mediaplay.IMusicPlayerService;
import com.bester.bester_mediaplay.R;
import com.bester.bester_mediaplay.SystemVideoPlayer;
import com.bester.tools.LogUtil;
import com.bester.tools.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import static com.bester.bester_mediaplay.R.id.list_video;

/**
 * Created by Wzich on 2017/9/12.
 *
 */

public class LocalVideoPager extends BasePager {
    private TextView mTvNovideo;
    private ListView mListVideo;
    private ProgressBar mPbLoading;
    private TextView mTvLoading;
    private VideoAdapter adapter;

    /**
     * 装载数据集合
     */
    private ArrayList<MediaItem> mediaItems;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mediaItems != null && mediaItems.size() > 0){
                //获取到数据，隐藏文本，设置适配器
                mTvNovideo.setVisibility(View.GONE);
                adapter = new VideoAdapter(context,mediaItems,true);
                mListVideo.setAdapter(adapter);
            } else {
                //未获取到数据，显示文本
                mTvNovideo.setVisibility(View.VISIBLE);

            }
            //隐藏pregressBar和加载文本
            mPbLoading.setVisibility(View.GONE);
            mTvLoading.setVisibility(View.GONE);
        }
    };

    public LocalVideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        LogUtil.e("初始化本地视频页面");
        View view = View.inflate(context, R.layout.video_pager,null);
        mTvNovideo = (TextView) view.findViewById(R.id.tv_novideo);
        mListVideo = (ListView) view.findViewById(list_video);
        mPbLoading = (ProgressBar) view.findViewById(R.id.pb_loading);
        mTvLoading = (TextView) view.findViewById(R.id.tv_loading);

        //设置listView点击监听
        mListVideo.setOnItemClickListener(new MyOnItemClickListener());
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.e("初始化本地视频页面数据");
        //加载本地视频数据
        getLocalVideo();
    }

    /**
     * 获取本地Video
     * 1.遍历sdcard ，利用后缀名搜索
     * 2.从内容提供者中获取视频
     */
    private void getLocalVideo() {
        mediaItems = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;//外部uri
                String[] objs = {
                        MediaStore.Video.Media.DISPLAY_NAME,//视频名称
                        MediaStore.Video.Media.DURATION,//视频时长
                        MediaStore.Video.Media.SIZE,//视频文件大小
                        MediaStore.Video.Media.DATA,//视频的绝对地址
                        MediaStore.Video.Media.ARTIST//歌曲的演唱者
                };
                Cursor cursor = resolver.query(uri,objs,null,null,null);
                if (cursor != null){
                    while (cursor.moveToNext()){
                        MediaItem medio = new MediaItem();
                        mediaItems.add(medio);

                        String name = cursor.getString(0);//视频名称
                        medio.setName(name);

                        long duration = cursor.getLong(1);//视频时长
                        medio.setDuration(duration);

                        long size = cursor.getLong(2);//视频文件大小
                        medio.setSize(size);

                        String data = cursor.getString(3);//视频的绝对地址
                        medio.setData(data);

                        String artist = cursor.getString(4);//歌曲的演唱者
                        medio.setArtist(artist);
                    }
                    cursor.close();
                }
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    private class MyOnItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(context,SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("mediaItems",mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position);
            context.startActivity(intent);
        }
    }
}
