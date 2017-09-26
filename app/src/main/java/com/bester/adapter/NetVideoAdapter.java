package com.bester.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bester.bean.MediaItem;
import com.bester.bester_mediaplay.R;
import com.bester.tools.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;

import org.xutils.x;

import java.util.ArrayList;


/**
 * Created by Wzich on 2017/9/25.
 */

public class NetVideoAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<MediaItem> mediaItems;
    private Utils utils;

    public NetVideoAdapter(Context context, ArrayList<MediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
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
            convertView = View.inflate(context, R.layout.item_net_video,null);
            holderView = new HolderView();
            holderView.iv_net_video_image = (ImageView) convertView.findViewById(R.id.iv_net_video_image);
            holderView.tv_net_video_name = (TextView) convertView.findViewById(R.id.tv_net_video_name);
            holderView.tv_net_video_desc = (TextView) convertView.findViewById(R.id.tv_net_video_desc);
            holderView.tv_net_video_type = (TextView) convertView.findViewById(R.id.tv_net_video_type);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }
        //根据position得到列表中对应位置的数据
        MediaItem mediaItem = mediaItems.get(position);

        //使用xutils3 请求图片
//        x.image().bind(holderView.iv_net_video_image,mediaItem.getImageUrl());

        //使用Glide 请求图片
        Glide.with(context).load(mediaItem.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL) //缓存全尺寸及其他
                .placeholder(R.drawable.icon_local_video)
                .error(R.drawable.icon_local_video)
                .into(holderView.iv_net_video_image);

        //使用Picasso 请求图片
//        Picasso.with(context).load(mediaItem.getImageUrl())
//                .placeholder(R.drawable.icon_local_video)
//                .error(R.drawable.icon_local_video)
//                .into(holderView.iv_net_video_image);

        holderView.tv_net_video_name.setText(mediaItem.getName());
        holderView.tv_net_video_desc.setText(mediaItem.getDesc());
        holderView.tv_net_video_type.setText("类型：" + mediaItem.getType());
        return convertView;
    }
    static class HolderView{
        ImageView iv_net_video_image;
        TextView tv_net_video_name;
        TextView tv_net_video_desc;
        TextView tv_net_video_type;
    }
}
