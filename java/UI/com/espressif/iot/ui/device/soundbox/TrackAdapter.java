package com.espressif.iot.ui.device.soundbox;

import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.type.device.other.EspAudio;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import net.tsz.afinal.FinalBitmap;

public class TrackAdapter extends BaseAdapter {

    private Context mContext;
    private List<EspAudio> mTrackList;
    private FinalBitmap mFinalBitmap;

    private class ViewHolder {
        public ImageView cover;
        public TextView title;
        public TextView intro;
    }

    public TrackAdapter(Context context, List<EspAudio> list) {
        mContext = context;
        mTrackList = list;

        mFinalBitmap = FinalBitmap.create(context);
        mFinalBitmap.configLoadfailImage(R.drawable.unknown_cover);
        mFinalBitmap.configLoadingImage(R.drawable.unknown_cover);
    }

    @Override
    public int getCount() {
        return mTrackList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTrackList.get(position);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.xima_track_content, null);
            holder = new ViewHolder();
            holder.cover = (ImageView)convertView.findViewById(R.id.imageview);
            holder.title = (TextView)convertView.findViewById(R.id.trackname);
            holder.intro = (TextView)convertView.findViewById(R.id.intro);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        EspAudio sound = mTrackList.get(position);
        holder.title.setText(sound.getTitle());
        holder.intro.setText(sound.getIntro());
        mFinalBitmap.display(holder.cover, sound.getCoverUrlLarge());

        return convertView;
    }

}
