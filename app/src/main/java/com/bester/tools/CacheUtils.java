package com.bester.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.bester.service.MusicPlayerService;

import static android.R.attr.key;

/**
 * 缓存工具类
 * Created by Wzich on 2017/9/26.
 */

public class CacheUtils {
    /**
     * 保存数据
     * @param context
     * @param key
     * @param values
     */
    public static void putString(Context context,String key,String values){
        SharedPreferences sharedPreferences = context.getSharedPreferences("Bester_player",Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key,values).commit();
    }

    /**
     * 得到缓存数据
     * @param context
     * @param key
     * @return
     */
    public static String getString(Context context,String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("Bester_player",Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }

    /**
     * 将播放模式保存
     * @param context
     * @param key
     * @param values
     */
    public static void putPlayMode(Context context,String key,int values){
        SharedPreferences sharedPreferences = context.getSharedPreferences("Bester_player",Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(key,values).commit();
    }

    /**
     * 得到播放模式
     * @param context
     * @param key
     * @return
     */
    public static int getPlayMode(Context context, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("Bester_player",Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, MusicPlayerService.AUDIOMODE_CYCLELIST);
    }
}
