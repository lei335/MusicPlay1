package com.example.musicplay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.media.MediaBrowserCompatUtils;
import android.widget.Toast;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


/**
 * Created by 张蕾 on 2016/10/14.
 */

public class PlayerService extends Service {
    private FindLocalSongs find;
    private MediaPlayer mediaplayer = new MediaPlayer(); //播放器对象
    private String path;   //音乐文件路径
    private String msg;
    private boolean isPause;  //暂停
    private int current = 0;  //记录当前正在播放的音乐
    List<Mp3Info> mp3Infos;   //存放Mp3Info对象的集合
    private int status = 4;    //开始播放时默认为顺序播放,1为单曲循环，2为全部循环，3为随机播放

    private MyReceiver myReceiver;   //自定义广播接收器
    private int currentTime;    //当前播放进度
    private int duration;       //播放长度

    public String PAUSE_MSG;
    public String CONTINUE_MSG;
    public String PLAY_MSG;
    public String PROGRESS_CHANGE;
    public String PREVIOUS_MSG;
    public String NEXT_MSG;

    /*//服务要发送的Action
    public static final String UPDATE_ACTION = "action.UPDATE_ACTION";   //更新动作
    public static final String CTL_ACTION = "action.CTL_ACTION";    //控制动作
    public static final String MUSIC_CURRENT = "action.MUSIC_CURRENT"; //当前音乐播放时间更新动作
    public static final String MUSIC_DURATION = "action.MUSIC_DURATION";  //新音乐长度更新动作
*/
   /* *
     * handler用来接收消息
    *//*
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (mediaplayer != null) {
                    currentTime = mediaplayer.getCurrentPosition();  //获取当前音乐播放的位置
                    Intent intent = new Intent();
                    intent.setAction(MUSIC_CURRENT);
                    intent.putExtra("currentTime", currentTime);
                    sendBroadcast(intent);     //给MainActivity发送广播
                    handler.sendEmptyMessageDelayed(1, 1000); //指定一秒之后发送消息
                }
            }
        };
    };*/

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.CTL_ACTION);
        registerReceiver(myReceiver, filter);
        mediaplayer = new MediaPlayer();
        find = new FindLocalSongs();
        mp3Infos = find.getMp3Infos(getContentResolver());
        /**
         * 为MediaPlayer设置音乐播放完成时的监听器
         */
        mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (status == 1) {          //单曲循环
                    mediaPlayer.start();
                } else if (status == 2) {     //全部循环
                    current++;
                    if (current > mp3Infos.size() - 1) {   //变为第一首的位置继续播放
                        current = 0;
                    }
                    Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);   //发送广播通知Activity
                    sendIntent.putExtra("current", current);
                    //发送广播，将被activity中的broadcastReceiver接收到
                    sendBroadcast(sendIntent);
                    path = mp3Infos.get(current).getUrl();
                    play(0);
                } else if (status == 3) {          //随机播放
                    current = getRandomIndex(mp3Infos.size() - 1);
                    Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
                    sendIntent.putExtra("current", current);
                    //发送广播，将被activity中的broadcastReceiver接收到
                    sendBroadcast(sendIntent);
                    path = mp3Infos.get(current).getUrl();
                    play(0);
                } else if (status == 4) {            //顺序播放
                    current++;                    //下一首位置
                    if (current <= mp3Infos.size() - 1) {
                        Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
                        sendIntent.putExtra("current", current);
                        sendBroadcast(sendIntent);
                        path = mp3Infos.get(current).getUrl();
                        play(0);
                    } else {
                        mediaPlayer.seekTo(0);
                        current = 0;
                        Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
                        sendIntent.putExtra("current", current);
                        sendBroadcast(sendIntent);
                    }
                }
            }
        });
    }

    public class MyReceiver extends BroadcastReceiver{
        public void onReceive(Context context,Intent intent){

            path = intent.getStringExtra("url");  //歌曲路径
            current = intent.getIntExtra("listPosition", -1);   //当前播放的歌曲在mp3infos中的位置
            msg = intent.getStringExtra("MSG");    //播放信息
            //currentTime = mediaplayer.getCurrentPosition();   //音乐当前播放位置
            int contral=intent.getIntExtra("contral",-1);
            if (msg==PAUSE_MSG){
                pause();
            }else if (msg==PLAY_MSG){
                play(0);
            }else if (msg == CONTINUE_MSG) {    //继续播放
                resume();
            }else if (msg == PREVIOUS_MSG) {    //上一首
                previous();
            }else if (msg == NEXT_MSG) {            //下一首
                next();
            }else if (msg == PROGRESS_CHANGE) {     //进度更新
                currentTime = intent.getIntExtra("progress", -1);
                play(currentTime);
            }
            switch (contral){
                case 1:
                    status=1;       //将播放状态设置为1表示单曲循环
                    break;
                case 2:
                    status=2;       //将播放状态设置为2表示全部循环
                    break;
                case 3:
                    status=3;       //将播放状态设置为3表示随机播放
                    break;
                case 4:
                    status=4;       //将播放状态设置为4表示顺序播放
                    break;
            }
            Intent sendIntent=new Intent(MainActivity.UPDATE_ACTION);
            sendIntent.putExtra("update",status);
            sendIntent.putExtra("current",current);
            sendBroadcast(sendIntent);
        }
    }

    /**
     * 随机播放时，获取随机位置
     */
    protected int getRandomIndex(int end) {
        int index = (int) (Math.random() * end);
        return index;
    }

    public int onStartCommand(Intent intent,int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        path = intent.getStringExtra("url");  //歌曲路径
        current = intent.getIntExtra("listPosition", -1);   //当前播放的歌曲在mp3infos中的位置
        msg = intent.getStringExtra("MSG");    //播放信息
        if (msg == PLAY_MSG) {     //直接播放音乐
            play(0);
            //mediaplayer.seekTo();
        } else if (msg == PAUSE_MSG) {  //暂停
            pause();
        } else if (msg == CONTINUE_MSG) {    //继续播放
            resume();
        } else if (msg == PREVIOUS_MSG) {    //上一首
            previous();
        } else if (msg == NEXT_MSG) {            //下一首
            next();
        } else if (msg == PROGRESS_CHANGE) {     //进度更新
            currentTime = intent.getIntExtra("progress", -1);
            play(currentTime);
        }
        return START_REDELIVER_INTENT;       //重传intent
    }

    private void play(int currentTime) {    //播放音乐
        try {
            mediaplayer.reset();     //把各项参数回复到初始状态
            mediaplayer.setDataSource(path);
            mediaplayer.prepare();   //进行缓冲
            mediaplayer.setOnPreparedListener(new PreparedListener(currentTime));  //注册一个监听器
            //handler.sendEmptyMessage(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 实现一个OnPrepareListener接口，当音乐准备好的时候开始播放
     */
    private final class PreparedListener implements MediaPlayer.OnPreparedListener {
        private int currentTime;
        public PreparedListener(int currentTime) {
            this.currentTime = currentTime;
        }
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();    //开始播放
            currentTime=mediaPlayer.getCurrentPosition();  //音乐当前播放时间
            if (currentTime> 0) {       //如果音乐不是从头播放
                mediaPlayer.seekTo(currentTime);
            }
            Intent intent=new Intent();
            intent.setAction(MainActivity.MUSIC_DURATION);
            duration=mediaPlayer.getDuration();
            intent.putExtra("duration",duration);    //通过intent传递歌曲的总长度
            intent.putExtra("progress",currentTime);
            sendBroadcast(intent);
        }
    }

    private void pause() {        //暂停音乐
        if (mediaplayer != null && mediaplayer.isPlaying()) {
            mediaplayer.pause();
            isPause = true;
        }
    }

    private void resume() {       //继续播放音乐
        if (isPause) {
            mediaplayer.start();
            isPause = false;
        }
    }

    private void previous() {     //上一首
        Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
        sendIntent.putExtra("current", current);
        sendBroadcast(sendIntent);
        play(0);
    }

    private void next() {         //下一首
        Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
        sendIntent.putExtra("current", current);
        sendBroadcast(sendIntent);
        play(0);
    }

   /* private void stop() {         //停止音乐
        if (mediaplayer != null) {
            mediaplayer.stop();
            try {
                mediaplayer.prepare();  //在调用stop后如果需要再次通过start进行播放，需要之前调用prepare
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    public void onDestroy() {
        if (mediaplayer != null) {
            mediaplayer.stop();
            mediaplayer.release();
            mediaplayer = null;
        }
    }
}