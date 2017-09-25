package com.bester.tools;

import android.content.Context;
import android.net.TrafficStats;

import java.util.Formatter;
import java.util.Locale;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class Utils {

	private StringBuilder mFormatBuilder;
	private Formatter mFormatter;

	private long lastTotalRxBytes = 0;
	private long lastTimeStamp = 0;

	public Utils() {
		// 转换成字符串的时间
		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

	}

	/**
	 * 把毫秒转换成：1:20:30这里形式
	 * @param timeMs
	 * @return
	 */
	public String stringForTime(int timeMs) {
		int totalSeconds = timeMs / 1000;
		int seconds = totalSeconds % 60;

		int minutes = (totalSeconds / 60) % 60;

		int hours = totalSeconds / 3600;

		mFormatBuilder.setLength(0);
		if (hours > 0) {
			return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
					.toString();
		} else {
			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}
	/**
	 * 判断是否为网络资源
	 */
	public boolean isNetUri(String uri){
		boolean reault = false;
		if (uri != null){
			//rtsp 直播，mms流媒体协议
			if (uri.toLowerCase().startsWith("http")
					||uri.toLowerCase().startsWith("rtsp")
					||uri.toLowerCase().startsWith("mms")){
				reault = true;
			}
		}
		return reault;
	}

	public String getNetSpeed(Context context) {
		String netSpeed = "0 kb/s";
		long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid)
				== TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
		long nowTimeStamp = System.currentTimeMillis();
		//毫秒转换
		long speed = (nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp);
		lastTimeStamp = nowTimeStamp;
		lastTotalRxBytes = nowTotalRxBytes;
		if (speed > 1024){
			speed = speed /1024;
			netSpeed = String.valueOf(speed) + "MB/s";
		} else {
			netSpeed = String.valueOf(speed) + "kb/s";
		}

		return netSpeed;
	}
}
