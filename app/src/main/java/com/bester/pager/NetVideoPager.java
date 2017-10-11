package com.bester.pager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bester.adapter.NetVideoAdapter;
import com.bester.base.BasePager;
import com.bester.bean.MediaItem;
import com.bester.bester_mediaplay.IMusicPlayerService;
import com.bester.bester_mediaplay.R;
import com.bester.bester_mediaplay.SystemVideoPlayer;
import com.bester.tools.CacheUtils;
import com.bester.tools.Constants;
import com.bester.tools.LogUtil;
import com.bester.view.XListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Wzich on 2017/9/12.
 */

public class NetVideoPager extends BasePager {

    @ViewInject(R.id.net_list_video)
    private XListView mNetListVideo;

    @ViewInject(R.id.tv_nonet)
    private TextView mTvNonet;

    @ViewInject(R.id.pb_loading)
    private ProgressBar mPbLoading;

    @ViewInject(R.id.tv_loading)
    private TextView mTvLoading;

    /**
     * 是否加载更多网络数据
     */
    private boolean isLoadMoreNetData = false;

    /**
     * 装载数据集合
     */
    private ArrayList<MediaItem> mediaItems;

    /**
     * ListView 适配器
     */
    private NetVideoAdapter adapter;


    public NetVideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.netvideo_pager,null);
        x.view().inject(NetVideoPager.this,view);

        //设置点击监听
        mNetListVideo.setOnItemClickListener(new MyOnItemClickListener());

        mNetListVideo.setPullLoadEnable(true);
        mNetListVideo.setXListViewListener(new MyXListViewListener());
        return view;
    }



    class MyXListViewListener implements XListView.IXListViewListener{

        @Override
        public void onRefresh() {
            getNetData();
        }

        @Override
        public void onLoadMore() {
            getMoreNetData();
        }
    }

    private void getMoreNetData() {
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("联网成功==" + result);
                isLoadMoreNetData = true;
                //主线程
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("联网失败==" + ex );
                isLoadMoreNetData = false;
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex);
                isLoadMoreNetData = false;
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
                isLoadMoreNetData = false;
            }
        });
    }

    private void onLoad() {
        mNetListVideo.stopRefresh();
        mNetListVideo.stopLoadMore();
        mNetListVideo.setRefreshTime(getSystemtTime());
    }



    @Override
    public void initData() {
        super.initData();
        LogUtil.e("初始化网络视频页面数据");
        String saveJson = CacheUtils.getString(context,Constants.NET_URL);
        if (!TextUtils.isEmpty(saveJson)){
            processData(saveJson);
        }
        getNetData();
    }

    private void getNetData() {
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("联网成功==" + result);
                //主线程
                CacheUtils.putString(context,Constants.NET_URL,result);
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("联网失败==" + ex );
               showData();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex);

            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");

            }
        });
    }

    /**
     * 接受json数据
     * @param json
     */
    private void processData(String json) {
        if (!isLoadMoreNetData){
            mediaItems = parseJson(json);
            showData();

        } else { //加载更多
            isLoadMoreNetData = false;
            mediaItems.addAll(parseJson(json));
            //刷新适配器
            adapter.notifyDataSetChanged();
            //重新载入
            onLoad();
        }
    }

    private void showData() {
        //设置适配器
        if (mediaItems != null && mediaItems.size() > 0){
            //获取到数据，隐藏文本，设置适配器
            mTvNonet.setVisibility(View.GONE);
            adapter = new NetVideoAdapter(context,mediaItems);
            mNetListVideo.setAdapter(adapter);
            onLoad();
        } else {
            //未获取到数据，显示文本
            mTvNonet.setVisibility(View.VISIBLE);
        }
        //隐藏pregressBar和加载文本
        mPbLoading.setVisibility(View.GONE);
        mTvLoading.setVisibility(View.GONE);
    }

    /**
     * 解析json数据
     * 1.使用系统接口解析数据
     * 2.使用第三方接口解析数据(GSON,fastjson)
     * @param json
     * @return
     */
    private ArrayList<MediaItem> parseJson(String json) {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");
            if (jsonArray != null && jsonArray.length() > 0){
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);
                    if (jsonObjectItem != null){
                        MediaItem mediaItem = new MediaItem();

                        String movieName = jsonObjectItem.optString("movieName");
                        mediaItem.setName(movieName);

                        String videoTitle = jsonObjectItem.optString("videoTitle");
                        mediaItem.setDesc(videoTitle);

                        String imageUrl = jsonObjectItem.optString("coverImg");
                        mediaItem.setImageUrl(imageUrl);

                        String hightUrl = jsonObjectItem.optString("hightUrl");
                        mediaItem.setData(hightUrl);

                        String type = jsonObjectItem.optString("type");
                        mediaItem.setType(type);

                        mediaItems.add(mediaItem);
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mediaItems;
    }


    private class MyOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(context,SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("mediaItems",mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position - 1);
            context.startActivity(intent);
        }
    }

    /**
     * 得到系统时间
     * @return
     */
    private String getSystemtTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date()) ;
    }
}