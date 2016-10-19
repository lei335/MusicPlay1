package com.example.musicplay;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v4.media.MediaBrowserCompatUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;

/**
 * 给listView设置adapter
 */

public class MainActivity extends AppCompatActivity {
    private FindLocalSongs find;   //查找歌曲的类的实例
    private List<Mp3Info> mp3Infos;  //存放mp3Info引用的集合
    private Mp3Info mp3Info;       //Mp3Info对象引用
    private MusicListAdapter musicListAdapter;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    /**
     * 定义一系列的intent携带的信息
     */
    public String PAUSE_MSG;
    public String CONTINUE_MSG;
    public String PLAY_MSG;
    public String PROGRESS_CHANGE;
    public String PREVIOUS_MSG;
    public String NEXT_MSG;
    private PlayerReceiver playerReceiver;     //自定义的广播接收器
    //一系列的动作
    public static final String UPDATE_ACTION="action.UPDATE_ACTION";  //更新动作
    public static final String CTL_ACTION="action.CTL_ACTION";     //控制动作
    public static final String MUSIC_CURRENT="action.MUSIC_CURRENT";  //音乐当前时间改变动作
    public static final String MUSIC_DURATION="action.MUSIC_DURATION";  //音乐播放长度改变动作
    //public static final String MUSIC_PLAYING="action.MUSIC_PLAYING";     //音乐正在播放动作
    public static final String REPEAT_ACTION="action.REPEAT_ACTION";    //音乐重复播放动作
    private int repeatState;  //循环模式
    private final int isCurrentRepeat = 1;  //单曲循环
    private final int isAllRepeat = 2;   //全部循环
    private final int isShuffle = 3;  //随机播放
    private final int isNoneShuffle = 4; //顺序播放
    private boolean isPlaying;      //正在播放
    private boolean isPause;      //暂停

    private String url;        //歌曲路径
    private ListView listView;  //歌曲列表
    //private String title;      //歌曲标题
    //private String artist;     //歌曲家
    private SeekBar seekBar;   //显示歌曲进度
    private Timer timer;     //计时器
    private ImageView musicImage;  //歌曲图片
    private int listposition;        //播放歌曲在mp3Infos中的位置
    private ImageButton pre_song;  //上一首
    private ImageButton play_song;  //播放
    private ImageButton next_song;   //下一首
    private ImageButton play_style;   //播放方式
    private TextView tx;    //用来显示正在播放歌曲的信息
    private int currentTime;  //音乐当前播放时间
    private boolean isSeekBarChanging;  //防止计时器与seekBar拖动时冲突
    private int duration;    //总时长
    private int flag;      //播放标识
    private boolean isFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);   //隐藏标题栏
        setContentView(R.layout.activity_main);
        /**
         * 获取id
         */
        listView = (ListView) findViewById(R.id.list_music);
        seekBar = (SeekBar) findViewById(R.id.process_bar);
        musicImage = (ImageView) findViewById(R.id.music_image);
        pre_song = (ImageButton) findViewById(R.id.pre_song);
        play_song = (ImageButton) findViewById(R.id.play_song);
        next_song = (ImageButton) findViewById(R.id.next_song);
        play_style = (ImageButton) findViewById(R.id.play_style);
        tx = (TextView) findViewById(R.id.music_info);
        find = new FindLocalSongs();
        mp3Infos = find.getMp3Infos(getContentResolver());     //获取歌曲对象集合
        musicListAdapter = new MusicListAdapter(mp3Infos, this);
        listView.setAdapter(musicListAdapter);

        setViewOnclickListener();
        listView.setOnItemClickListener(new MusicListItemClickListener());
        playerReceiver=new PlayerReceiver();
        IntentFilter filter= new IntentFilter();   //创建IntentFilter
        //指定BroadcastReceiver监听的Action
        filter.addAction(UPDATE_ACTION);
        filter.addAction(MUSIC_CURRENT);
        filter.addAction(MUSIC_DURATION);
        filter.addAction(REPEAT_ACTION);
        filter.addAction(CTL_ACTION);
        //注册BroadcastReceiver
        registerReceiver(playerReceiver,filter);
        Intent intent = new Intent(this,PlayerService.class);
        startService(intent);
        repeatState = isNoneShuffle;   //初始状态为顺序播放
    }
    /**
     * 给每一个按钮设置监听器
     */
    private void setViewOnclickListener() {
        ViewOnclickListener viewOnClickListener = new ViewOnclickListener();
        seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener());
        pre_song.setOnClickListener(viewOnClickListener);
        play_song.setOnClickListener(viewOnClickListener);
        next_song.setOnClickListener(viewOnClickListener);
        play_style.setOnClickListener(viewOnClickListener);
    }
    /**
     * 定义一个PlayerReceiver,用来接收从PlayerService传回来的广播的内部类
     */
    public class PlayerReceiver extends BroadcastReceiver{
        public void onReceive(Context context,Intent intent){
            String action=intent.getAction();
            if(action.equals(MUSIC_CURRENT)){
                currentTime=intent.getIntExtra("progress",-1);
                seekBar.setProgress(currentTime);        //设置进度完成百分比
            }else if(action.equals(MUSIC_DURATION)){
                int duration=intent.getIntExtra("duration",-1);
                seekBar.setMax(duration);                 //设置进度条的最大值
            }else if(action.equals(UPDATE_ACTION)){
                //获取intent中的current消息，current代表当前正在播放的歌曲
                listposition=intent.getIntExtra("current",-1);
                //url=mp3Infos.get(listposition).getUrl();
                if (listposition>=0){
                    tx.setText(mp3Infos.get(listposition).getTitle()+"/"+mp3Infos.get(listposition).getArtist());
                }/*else if(action.equals(REPEAT_ACTION)){
                    repeatState=intent.getIntExtra("repeatState",-1);
                    switch(repeatState){
                        case isCurrentRepeat:      //单曲循环
                            play_style.setBackgroundResource(R.drawable.one);
                            break;
                        case isAllRepeat:              //全部循环
                            play_style.setBackgroundResource(R.drawable.cycle);
                            break;
                        case isShuffle:              //随机播放
                            play_style.setBackgroundResource(R.drawable.free);
                            break;
                        case isNoneShuffle:           //顺序播放
                            play_style.setBackgroundResource(R.drawable.order);
                            break;
                    }
                }*/
                else if (listposition==0){
                    play_song.setBackgroundResource(R.drawable.stop);
                    isPause=true;
                    isPlaying=false;
                }
            }
        }
    }

    /**
     * 点击列表播放音乐
     */
    private class MusicListItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
            listposition=position;  //列表点击位置
            playMusic(listposition);
        }
    }
    /**
     * 播放音乐
     */
    public void playMusic(int listposition) {
        if (mp3Infos != null) {
            play_song.setBackgroundResource(R.drawable.play);
            isPlaying=true;
            Mp3Info mp3Info = mp3Infos.get(listposition);
            tx.setText(mp3Info.getTitle() + "/" + mp3Info.getArtist());   //在这里显示歌曲标题和艺术家
            // Bitmap bitmap= MediaBrowserCompatUtils   打算获取专辑图片
            noneShuffle();   //开始播放时为顺序播放
            Intent intent = new Intent(MainActivity.this,PlayerService.class);
            intent.setAction("media.MUSIC_SERVICE");
            intent.putExtra("url", mp3Info.getUrl());
            intent.putExtra("listPosition",listposition);
            intent.putExtra("MSG", PLAY_MSG);
            //intent.setClass(MainActivity.this, PlayerService.class);
            startService(intent);  //启动服务
        }else{
            Toast.makeText(MainActivity.this,"没有歌曲可以播放",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 控件点击事件
     * 定义一个内部类
     */
    private class ViewOnclickListener implements View.OnClickListener {
        //Intent intent = new Intent(CTL_ACTION);
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play_song:  //点击播放音乐按钮
                        if (isPlaying) {
                            play_song.setBackgroundResource(R.drawable.stop);

                            Intent intent=new Intent(MainActivity.this,PlayerService.class);
                            intent.setAction("media.MUSIC_SERVICE");
                            intent.putExtra("MSG", PAUSE_MSG);
                            startService(intent);
                            isPlaying = false;
                            isPause = true;
                        } else if (isPause) {
                            play_song.setBackgroundResource(R.drawable.play);
                            Intent intent=new Intent(MainActivity.this,PlayerService.class);
                            intent.setAction("media.MUSIC_SERVICRE");
                            intent.putExtra("MSG",CONTINUE_MSG);
                            startService(intent);
                            isPause = false;
                            isPlaying = true;
                        }
                    break;
                case R.id.pre_song:     //上一首歌曲
                    pre_song();
                    pre_song.setBackgroundResource(R.drawable.pre);
                    //isFirstTime=false;
                    isPlaying=true;
                    isPause=false;
                    break;
                case R.id.next_song:   //下一首歌曲
                    next_song();
                    next_song.setBackgroundResource(R.drawable.next_music);
                    //isFirstTime=false;
                    isPlaying=true;
                    isPause=false;
                    break;
                case R.id.play_style:    //播放格式
                    if (repeatState == isCurrentRepeat) {
                        repeat_all();
                        repeatState = isAllRepeat;
                    } else if (repeatState == isAllRepeat) {
                        shuffle();
                        repeatState = isShuffle;
                    } else if (repeatState == isShuffle) {
                        noneShuffle();
                        repeatState = isNoneShuffle;
                    } else if (repeatState == isNoneShuffle) {
                        repeat_one();
                        repeatState = isCurrentRepeat;
                    }
                    Intent intent = new Intent(REPEAT_ACTION);
                    switch (repeatState) {
                        case isCurrentRepeat:
                            play_style.setBackgroundResource(R.drawable.one);
                            intent.putExtra("repeatState", isCurrentRepeat);
                            sendBroadcast(intent);
                            break;
                        case isAllRepeat:
                            play_style.setBackgroundResource(R.drawable.cycle);
                            intent.putExtra("repeatState", isAllRepeat);
                            sendBroadcast(intent);
                            break;
                        case isShuffle:
                            play_style.setBackgroundResource(R.drawable.free);
                            intent.putExtra("repeatState", isShuffle);
                            sendBroadcast(intent);
                            break;
                        case isNoneShuffle:
                            play_style.setBackgroundResource(R.drawable.order);
                            intent.putExtra("repeatState", isNoneShuffle);
                            sendBroadcast(intent);
                            break;
                    }
                    break;
            }
        }
    }
    /**
     * 随机播放
     */
    public void shuffle(){
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("contral",3);
        sendBroadcast(intent);
    }
    /**
     * 单曲循环
     */
    public void repeat_one(){
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("contral",1);
        sendBroadcast(intent);
    }
    /**
     * 全部循环
     */
    public void repeat_all(){
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("contral",2);
        sendBroadcast(intent);
    }
    /**
     * 顺序播放
     */
    public void noneShuffle(){
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("contral",4);
        sendBroadcast(intent);
    }
    /**
     * 上一首
     */
    public void pre_song(){
        listposition=listposition-1;
        if (listposition>=0){
            Mp3Info mp3Info=mp3Infos.get(listposition);  //上一首歌曲
            tx.setText(mp3Info.getTitle()+"/"+mp3Info.getArtist());

            url=mp3Info.getUrl();
            Intent intent = new Intent(MainActivity.this,PlayerService.class);
           //Intent intent = new Intent(CTL_ACTION);
            //intent.setAction("media.MUSIC_SERVICE");
            intent.setAction("media.MUSIC_SERVICE");
            intent.putExtra("url",mp3Info.getUrl());
            intent.putExtra("listPosition",listposition);
            intent.putExtra("MSG",PREVIOUS_MSG);
            startService(intent);
        }else{
            Toast.makeText(MainActivity.this,"没有上一首了",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 下一首
     */
    public void next_song(){
        //  next_song.setBackgroundResource(R.drawable.next_music);
        listposition=listposition+1;
        if(listposition<=mp3Infos.size()-1){
            Mp3Info mp3Info=mp3Infos.get(listposition);
            url=mp3Info.getUrl();
            tx.setText(mp3Info.getTitle()+"/"+mp3Info.getArtist());
            Intent intent = new Intent(MainActivity.this,PlayerService.class);
            //Intent intent = new Intent(CTL_ACTION);
            //intent.setAction("media.MUSIC_SERVICE");
            intent.setAction("media.MUSIC_SERVICE");
            intent.putExtra("url",mp3Info.getUrl());
            intent.putExtra("listPosition",listposition);
            intent.putExtra("MSG",NEXT_MSG);
            startService(intent);
        }else{
            Toast.makeText(MainActivity.this,"没有下一首",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 实现监听seekBar的类
     */
    public class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser){     //seekBar进度改变的时候调用
            if (fromUser){
                currentTime=progress;
                audioTrackChange(currentTime);            //用户控制进度的改变
            }
        }
        public void onStartTrackingTouch(SeekBar seekBar){    //seekBar开始拖动的时候调用
        }
        public void onStopTrackingTouch(SeekBar seekBar){     //seekBar停止拖动的时候调用
        }
    }
    public void audioTrackChange(int currentTime){
        //Mp3Info mp3Info=mp3Infos.get(listposition);
        //url=mp3Info.getUrl();
        Toast.makeText(MainActivity.this,"音乐暂停中"+currentTime,Toast.LENGTH_SHORT);
        Intent intent=new Intent(MainActivity.this,PlayerService.class);
        //intent.getIntExtra("progress")
        intent.setAction("media.MUSIC_SERVICE");
        intent.putExtra("url",url);
        intent.putExtra("listPosition",listposition);
        if (isPause){
            intent.putExtra("MSG",PAUSE_MSG);

        }else{
            intent.putExtra("MSG",PROGRESS_CHANGE);
        }
        intent.putExtra("progress",currentTime);
        startService(intent);
    }

    /**
     * 按返回键弹出对话框确定退出
     */
    public boolean onKeyDown(int keyCode,KeyEvent event){
         if(keyCode==KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_DOWN){
             new AlertDialog.Builder(this).setTitle("退出").setMessage("您确定要退出吗?").setNegativeButton("取消",null)
                     .setPositiveButton("确定",
                             new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialogInterface, int i) {
                                     finish();
                                     Intent intent=new Intent(MainActivity.this,PlayerService.class);
                                     unregisterReceiver(playerReceiver);    //释放服务
                                     stopService(intent);     //停止后台服务
                                 }
                             }).show();
         }
        return super.onKeyDown(keyCode, event);
    }
}



