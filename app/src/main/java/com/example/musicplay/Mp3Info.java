package com.example.musicplay;

/**
 * Created by 张蕾 on 2016/10/11.
 * 存放本地歌曲信息
 */

public class Mp3Info {
    private long id;
    private String title;  //歌曲名字
    private String artist;  //歌手
    private long duration;   //持续时间
    private long size;
    private String url;
    private int isMusic;

    public void setId(long id){
        this.id=id;
    }
    public long getId(){
        return this.id;
    }

    public void setTitle(String title){
        this.title=title;
    }
    public String getTitle(){
        return this.title;
    }

    public void setArtist(String artist){
        this.artist=artist;
    }
    public String getArtist(){
        return this.artist;
    }

    public void setDuration(long duration){
        this.duration=duration;
    }
    public long getDuration(){
        return this.duration;
    }

    public void setSize(long size){
        this.size=size;
    }
    public long getSize(){
        return this.size;
    }

    public void setUrl(String url){
        this.url=url;
    }
    public String getUrl(){
        return this.url;
    }

}
