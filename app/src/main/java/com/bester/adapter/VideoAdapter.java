package com.bester.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bester.base.BasePager;
import com.bester.bean.MediaItem;
import com.bester.bester_mediaplay.R;
import com.bester.tools.Utils;

import java.util.ArrayList;


/**
 * Created by Wzich on 2017/9/16.
 */

public class VideoAdapter extends BaseAdapter {
    private final boolean isVideo;
    private final Context context;
    private final ArrayList<MediaItem> mediaItems;
    private Utils utils;

    public VideoAdapter(Context context, ArrayList<MediaItem> mediaItems,boolean isVideo) {
        this.context = context;
        this.mediaItems = mediaItems;
        this.isVideo = isVideo;
        utils = new Utils();
    }

    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HolderView holderView;
        if (convertView == null){
            convertView = View.inflate(context, R.layout.item_video,null);
            holderView = new HolderView();
            holderView.iv_video_image = (ImageView) convertView.findViewById(R.id.iv_video_image);
            holderView.tv_video_name = (TextView) convertView.findViewById(R.id.tv_video_name);
            holderView.tv_video_artist = (TextView) convertView.findViewById(R.id.tv_video_artist);
            holderView.tv_video_duration = (TextView) convertView.findViewById(R.id.tv_video_duration);
            holderView.tv_video_size = (TextView) convertView.findViewById(R.id.tv_video_size);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }

        //根据position得到列表中对应位置的数据
        MediaItem mediaItem = mediaItems.get(position);
        if (!isVideo){ //音频
            holderView.iv_video_image.setImageResource(R.drawable.icon_local_audio);

        }
        holderView.tv_video_name.setText(mediaItem.getName());
        holderView.tv_video_duration.setText(utils.stringForTime((int) mediaItem.getDuration()));
        holderView.tv_video_size.setText(Formatter.formatFileSize(context,mediaItem.getSize()));
        holderView.tv_video_artist.setText(mediaItem.getArtist());


        return convertView;
    }
    static class HolderView{
        ImageView iv_video_image;
        TextView tv_video_name;
        TextView tv_video_artist;
        TextView tv_video_duration;
        TextView tv_video_size;
    }
}
