package com.tct.weather.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tct.weather.bean.Hour;
import com.tct.weather.util.CommonUtils;
import com.tct.weather.util.IconBackgroundUtil;
import com.tct.weather.R;

import java.util.List;

/**
 * Created by jiajun.shen on 9/6/15.
 */
public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.HourlyHolder> {

    private String TAG = "weather HourlyAdapter";
    Context mContext;
    String[] times;
    String[] temps;
    int[] icons;
    int mTextColor = 0;

    public HourlyAdapter(Context mContext, List<Hour> hourList, boolean isUnitC, int color) {
        this.mContext = mContext;
        int size = hourList.size();
        temps = new String[size];
        times = new String[size];
        icons = new int[size];
        for (int i = 0; i < hourList.size(); i++) {
            times[i] = hourList.get(i).getTime();
            temps[i] = hourList.get(i).getTemperature();
//            icons[i] = IconBackgroundUtil.getHourlyIcon(hourList.get(i).getIcon()); //MODIFIED by qian-li, 2016-04-13,BUG-1940875
            //Log.i(TAG, "icon=" + hourList.get(i).getIcon());
            if (!isUnitC) {
                temps[i] = CommonUtils.c2f(temps[i]);
            }
        }
        mTextColor = color;
    }

    public void setTextColor(int color) {
        mTextColor = color;
    }

    @Override
    public HourlyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) ((Activity) mContext).getLayoutInflater().inflate(R.layout.recycle_hourly_item, parent, false);
        HourlyHolder hourlyHolder = new HourlyHolder(linearLayout);
        return hourlyHolder;
    }

    @Override
    public void onBindViewHolder(HourlyHolder holder, int position) {
        //Bitmap bitmap= BitmapFactory.decodeResource(mContext.getResources(),R.drawable.mini_icons_01);
        //holder.iconIv.setBackgroundResource(R.drawable.mini_icons_01);
        if (mTextColor != 0) {
            holder.timeTv.setTextColor(mTextColor);
            holder.tempTv.setTextColor(mTextColor);
            holder.timeTailTv.setTextColor(mTextColor);
        }
        holder.iconIv.setBackgroundResource(icons[position]);
        int time = Integer.parseInt(times[position].substring(0, 2));
        if (is24Format()) {
            holder.timeTv.setText(time + "");
            holder.timeTailTv.setText("00");
        } else {
            if (time <= 12) {
                holder.timeTv.setText(time + "");
                holder.timeTailTv.setText(R.string.date_am);
            } else {
                holder.timeTv.setText((time - 12) + "");
                holder.timeTailTv.setText(R.string.date_pm);
            }
        }
        holder.tempTv.setText(temps[position] + "°");
    }

    @Override
    public int getItemCount() {
        return times.length;
    }

    class HourlyHolder extends RecyclerView.ViewHolder {
        TextView tempTv, timeTv, timeTailTv;
        ImageView iconIv;

        public HourlyHolder(View itemView) {
            super(itemView);
            tempTv = (TextView) itemView.findViewById(R.id.hourly_tv_temp);
            timeTv = (TextView) itemView.findViewById(R.id.hourly_tv_time);
            timeTailTv = (TextView) itemView.findViewById(R.id.hourly_tv_time_tail);
            iconIv = (ImageView) itemView.findViewById(R.id.hourly_iv_icon);
        }
    }

    private boolean is24Format() {
        if (DateFormat.is24HourFormat(mContext)) {
            return true;
        } else {
            return false;
        }
    }
}
