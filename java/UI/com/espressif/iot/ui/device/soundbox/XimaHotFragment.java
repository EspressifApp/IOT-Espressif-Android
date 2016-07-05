package com.espressif.iot.ui.device.soundbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.espressif.iot.R;
import com.espressif.iot.type.device.other.EspAudio;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.track.TrackHotList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class XimaHotFragment extends XimaBaseFragment {
    private ListView mListView;
    private TrackAdapter mTrackAdapter;
    private List<EspAudio> mTrackList;
    private int mTotalPage;
    private int mPageIndex = 1;
    private boolean mLoading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mListView = (ListView)inflater.inflate(R.layout.xima_hot_fragment, container, false);
        mTrackList = new ArrayList<EspAudio>();
        mTrackAdapter = new TrackAdapter(getActivity(), mTrackList);
        mListView.setAdapter(mTrackAdapter);
        mListView.setOnScrollListener(new TrackScrollEndListener() {

            @Override
            public boolean hasMore() {
                return mPageIndex < mTotalPage;
            }

            @Override
            public void onScrollEnd() {
                loadData();
            }
        });
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EspAudio track = mTrackList.get(position);
                onAduioSelected(track);
            }
        });

        return mListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadData();
    }

    public void refresh() {
        if (mTrackList.isEmpty()) {
            loadData();
        }
    }

    private void loadData() {
        if (mLoading) {
            return;
        }

        mLoading = true;
        Map<String, String> param = new HashMap<String, String>();
        param.put(DTransferConstants.CATEGORY_ID, "" + 0);
        param.put(DTransferConstants.PAGE, "" + mPageIndex);
        param.put(DTransferConstants.PAGE_SIZE, "" + mXimalaya.getDefaultPagesize());
        CommonRequest.getHotTracks(param, new IDataCallBack<TrackHotList>() {

            @Override
            public void onSuccess(TrackHotList object) {
                if (object != null && object.getTracks() != null && object.getTracks().size() != 0) {
                    mPageIndex++;
                    mTotalPage = object.getTotalPage();
                    updateTrackList(mTrackList, object.getTracks());
                    mTrackAdapter.notifyDataSetChanged();
                }
                mLoading = false;
            }

            @Override
            public void onError(int code, String message) {
                mLoading = false;
            }
        });
    }
}

