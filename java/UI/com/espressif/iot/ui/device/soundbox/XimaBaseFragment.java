package com.espressif.iot.ui.device.soundbox;

import java.util.List;

import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.model.track.CommonTrackList;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import android.app.Activity;
import android.app.Fragment;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public abstract class XimaBaseFragment extends Fragment {
    protected CommonRequest mXimalaya;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mXimalaya = CommonRequest.getInstanse();
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

    protected void updateTrackList(List<EspTrack> list, CommonTrackList<Track> ximaTracks) {
        for (Track newTrack : ximaTracks.getTracks()) {
            EspTrack track =
                new EspTrack.Builder(EspTrack.Platform.Ximalaya).setTitle(newTrack.getTrackTitle())
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

    protected void onTrackSelected(EspTrack track) {
        // TODO
    }
}
