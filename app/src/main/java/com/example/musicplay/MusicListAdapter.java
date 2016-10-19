package com.example.musicplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 张蕾 on 2016/10/11.
 * 将数据绑定到视图
 */

public class MusicListAdapter extends BaseAdapter{
    private Context context;         //上下文对象引用
    private List<Mp3Info> mp3Infos;   //存放mp3info引用的集合
    private Mp3Info mp3Info;          //mp3info对象引用
    private int pos = -1;              //对象引用
    private ViewContainer vc;

    public MusicListAdapter(List<Mp3Info> mp3Infos,Context context){
        this.context=context;
        this.mp3Infos=mp3Infos;
    }

    public int getCount(){
        return mp3Infos.size();
    }    //指定总共包含多少项

    @Override
    public Object getItem(int position) {
        return null;
    }

    public View getView(int position, View convertView, ViewGroup parent){    //该方法返回的view将作为列表框
        vc=null;
        if(convertView==null){
            vc=new ViewContainer();
            convertView= LayoutInflater.from(context).inflate(R.layout.music_list_layout,null);
            vc.music_name=(TextView)convertView.findViewById(R.id.music_name);
            vc.musician=(TextView)convertView.findViewById(R.id.musician);
            convertView.setTag(vc);
        }else{
            vc=(ViewContainer)convertView.getTag();
        }
        mp3Info=mp3Infos.get(position);
        vc.music_name.setText(mp3Info.getTitle());    //显示标题
        vc.musician.setText(mp3Info.getArtist());  //显示歌手名
        return convertView;
    }
    public long getItemId(int position){
        return position;
    }   //返回值作为列表项的id
   /* public static String FormatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2)
            min = "0" + min;
            switch (sec.length()) {
                case 4:
                    sec = "0" + sec;
                    break;
                case 3:
                    sec = "00" + sec;
                    break;
                case 2:
                    sec = "000" + sec;
                    break;
                case 1:
                    sec = "0000" + sec;
                    break;
            }
            return min + ":" + sec.trim().substring(0, 2);
    }*/
}
class ViewContainer{

    public TextView music_name;
    public TextView musician;
    //public LinearLayout linearLayout;
}
