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
import com.ximalaya.ting.android.opensdk.model.live.radio.RadioList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class XimaLiveFragment extends XimaBaseFragment {
    private ListView mListView;
    private TrackAdapter mTrackAdapter;
    private List<EspAudio> mTrackList;
    private int mTotalPage;
    private int mPageIndex = 1;
    private boolean mLoading = false;

    private int mRadioType = 1;
    private int mProvinceCode = 360000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.xima_live_fragment, container, false);
        mListView = (ListView)view.findViewById(R.id.list);
        mTrackList = new ArrayList<EspAudio>();
        mTrackAdapter = new TrackAdapter(getActivity(), mTrackList);
        mListView.setAdapter(mTrackAdapter);
        mListView.setOnScrollListener(new TrackScrollEndListener() {

            @Override
            public void onScrollEnd() {
                loadData();
            }

            @Override
            public boolean hasMore() {
                return mPageIndex < mTotalPage;
            }
        });
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EspAudio radio = mTrackList.get(position);
                onAduioSelected(radio);
            }
        });

        return view;
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

    protected void loadData() {
        if (mLoading) {
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.RADIOTYPE, "" + mRadioType);
        map.put(DTransferConstants.PROVINCECODE, "" + mProvinceCode);
        CommonRequest.getRadios(map, new IDataCallBack<RadioList>() {

            @Override
            public void onSuccess(RadioList object) {
                if (object != null && object.getRadios() != null && object.getRadios().size() != 0) {
                    mPageIndex++;
                    mTotalPage = object.getTotalPage();
                    updateRadioList(mTrackList, object.getRadios());
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
