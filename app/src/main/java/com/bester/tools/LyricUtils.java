package com.bester.tools;

import com.bester.bean.Lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Wzich on 2017/9/29.
 * 解析歌词的工具类
 */

public class LyricUtils {
    private ArrayList<Lyric> lyrics;
    /**
     * 得到解析好的歌词列表
     * @return
     */
    public ArrayList<Lyric> getLyrics() {
        return lyrics;
    }
    /**
     * 是否存在歌词
     */
    private boolean isExistsLyric  = false;

    public boolean isExistsLyric() {
        return isExistsLyric;
    }

    /**
     * 读取歌词文件
     * file /mnt/scard/
     * @param file
     */
    public void readLyricFile(File file){
        if (file == null || !file.exists()){//歌词文件不存在
            lyrics = null;
            isExistsLyric = false;
        } else {//歌词文件存在，解析
            /**
             * 按行读取解析
             * 对歌词时间排序
             * 计算每句高亮显示时间
             */
            lyrics = new ArrayList<>();
            isExistsLyric = true;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
                String line = "";
                while ((line = reader.readLine()) != null){
                    line = parseLyric(line);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //2.对歌词时间排序
            Collections.sort(lyrics, new Comparator<Lyric>() {
                @Override
                public int compare(Lyric lhs, Lyric rhs) {
                    if(lhs.getTimePoint() < rhs.getTimePoint()){
                        return  -1;
                    }else if(lhs.getTimePoint() > rhs.getTimePoint()){
                        return  1;
                    }else{
                        return 0;
                    }

                }
            });

            //3.计算每句高亮显示的时间
            for(int i=0;i<lyrics.size();i++){
                Lyric oneLyric = lyrics.get(i);
                if(i+1 < lyrics.size()){
                    Lyric twoLyric = lyrics.get(i+1);
                    oneLyric.setSleepTime(twoLyric.getTimePoint()-oneLyric.getTimePoint());
                }
            }
        }
    }

    /**
     * 解析一句歌词
     * @param line
     * @return
     */
    private String  parseLyric(String line) {
        int post1 = line.indexOf("[");//0,第一次出现 "[" 的位置，没有返回-1
        int post2 = line.indexOf("]");//9,第一次出现 "]" 的位置，没有返回-1
        if (post1 == 0 && post2 != -1) {
            long[] times = new long[getCountLine(line)];
            String strTime = line.substring(post1 + 1, post2);
            times[0] = strTime2LongTime(strTime);

            String content = line;
            int i = 1;
            while (post1 == 0 && post2 != -1) {
                content = content.substring(post2 + 1);
                post1 = content.indexOf("[");
                post2 = content.indexOf("]");
                if (post1 != -1 && post2 != -1) {
                    strTime = content.substring(post1 + 1, post2);
                    times[i] = strTime2LongTime(strTime);
                    if (times[i] == -1) {
                        return "";
                    }
                    i++;
                }

            }
            Lyric lyric = new Lyric();
            //把时间数组和文本关联起来，并且加入到集合中
            for (int j = 0; j < times.length; j++) {

                if (times[j] != 0) {//有时间戳
                    lyric.setContent(content);
                    lyric.setTimePoint(times[j]);
                    //添加到集合中
                    lyrics.add(lyric);
                    lyric = new Lyric();
                }
            }
            return content;
        }
        return "";
    }

    /**
     * 把str类型的时间转换成long类型
     * @param strTime
     * @return
     */
    private long strTime2LongTime(String strTime) {
        long result = -1;
        try{
            String[] s1 = strTime.split(":");
            if(s1.length > 1){
                String[] s2 = s1[1].split("\\.");
                long min = Long.parseLong(s1[0]);
                long second = Long.parseLong(s2[0]);
                long mil = Long.parseLong(s2[1]);
                result = (min * 60 * 1000 + second * 1000 + mil * 10);
            }
        } catch (Exception e){
            result = -1;
        }
        return result;
    }

    /**
     * 判断歌词有多少句
     * @param line
     * @return
     */
    private int getCountLine(String line) {
        int result = -1;
        String[] left = line.split("\\[");
        String[] right = line.split("\\]");

        if (left.length == 0 && right.length == 0){
            result = 1;
        } else if(left.length > right.length){
            result = left.length;
        } else {
            result = right.length;
        }

        return result;
    }
}
