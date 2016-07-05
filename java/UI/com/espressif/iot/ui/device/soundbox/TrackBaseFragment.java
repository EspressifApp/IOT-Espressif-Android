package com.espressif.iot.ui.device.soundbox;

import java.util.List;

import com.espressif.iot.type.device.other.EspAudio;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import android.app.Activity;
import android.app.Fragment;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public abstract class TrackBaseFragment extends Fragment {
    protected CommonRequest mXimalaya;
    private DeviceSoundboxActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mXimalaya = CommonRequest.getInstanse();
        mActivity = (DeviceSoundboxActivity)activity;
    }

    private CharSequence mTitle;

    public void setTitle(CharSequence title) {
        mTitle = title;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public abstract class TrackScrollEndListener implements OnScrollListener {

        public abstract boolean hasMore();

        public abstract void onScrollEnd();

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_IDLE) {
                int count = view.getCount();
                count = count - 5 > 0 ? count - 5 : count - 1;
                if (view.getLastVisiblePosition() > count && hasMore()) {
                    onScrollEnd();
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    }

    protected void updateTrackList(List<EspAudio> list, List<Track> ximaTracks) {
        for (Track newTrack : ximaTracks) {
            EspAudio track =
                new EspAudio.Builder(EspAudio.Platform.Ximalaya, EspAudio.Type.Track).setId(newTrack.getDataId())
                    .setTitle(newTrack.getTrackTitle())
                    .setIntro(newTrack.getAnnouncer() == null ? newTrack.getTrackTags()
                        : newTrack.getAnnouncer().getNickname())
                    .setDuration(newTrack.getDuration())
                    .setDownloadSize(newTrack.getDownloadSize())
                    .setDownloadUrl(newTrack.getDownloadUrl())
                    .setCoverUrlLarge(newTrack.getCoverUrlLarge())
                    .setCoverUrlMiddle(newTrack.getCoverUrlMiddle())
                    .setCoverUrlSmall(newTrack.getCoverUrlSmall())
                    .create();
            list.add(track);
        }
    }

    protected void updateRadioList(List<EspAudio> list, List<Radio> ximaRadios) {
        for (Radio newRadio : ximaRadios) {
            EspAudio radio = new EspAudio.Builder(EspAudio.Platform.Ximalaya, EspAudio.Type.Radio)
                .setId(newRadio.getDataId())
                .setTitle(newRadio.getRadioName())
                .setIntro(newRadio.getProgramName())
                .setCoverUrlLarge(newRadio.getCoverUrlLarge())
                .setCoverUrlMiddle(newRadio.getCoverUrlLarge())
                .setCoverUrlSmall(newRadio.getCoverUrlSmall())
                .setDownloadUrl(newRadio.getRate24AacUrl())
                .create();
            list.add(radio);
        }
    }

    protected void onAduioSelected(EspAudio audio) {
        mActivity.postAudio(audio);
    }

    public void refresh(){
    }
}