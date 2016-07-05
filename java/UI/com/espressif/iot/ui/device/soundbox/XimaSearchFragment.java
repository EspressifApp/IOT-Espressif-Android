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
import com.ximalaya.ting.android.opensdk.model.track.SearchTrackList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class XimaSearchFragment extends XimaBaseFragment {
    private ListView mListView;
    private List<EspAudio> mTrackList;
    private TrackAdapter mTrackAdapter;

    private int mPageIndex = 1;
    private int mTotalPage = 0;

    private boolean mLoading = false;

    private String mQueryText = "";

    private EditText mSearchEdit;
    private Button mSearchComfirm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.xima_search_fragment, container, false);

        mSearchEdit = (EditText)view.findViewById(R.id.search_edit);
        mSearchComfirm = (Button)view.findViewById(R.id.search_comfirm);
        mSearchComfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String query = mSearchEdit.getText().toString();
                if (!mQueryText.equals(query)) {
                    mQueryText = query;
                    loadData(mQueryText, true);
                }
            }
        });

        mTrackList = new ArrayList<EspAudio>();
        mListView = (ListView)view.findViewById(R.id.list);
        mTrackAdapter = new TrackAdapter(getActivity(), mTrackList);
        mListView.setAdapter(mTrackAdapter);
        mListView.setOnScrollListener(new TrackScrollEndListener() {

            @Override
            public boolean hasMore() {
                return mPageIndex < mTotalPage;
            }

            @Override
            public void onScrollEnd() {
                loadData(mQueryText, false);
            }
        });
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EspAudio track = mTrackList.get(position);
                onAduioSelected(track);
            }
        });

        return view;
    }

    private void loadData(String query, final boolean newQuery) {
        if (mLoading) {
            return;
        }
        mLoading = true;
        mSearchComfirm.setEnabled(false);

        if (newQuery) {
            mPageIndex = 1;
            mTotalPage = 0;
        }
        Map<String, String> param = new HashMap<String, String>();
        param.put(DTransferConstants.SEARCH_KEY, query);
        param.put(DTransferConstants.PAGE, "" + mPageIndex);
        CommonRequest.getSearchedTracks(param, new IDataCallBack<SearchTrackList>() {

            @Override
            public void onSuccess(SearchTrackList list) {
                System.out.println("suc");
                if (newQuery) {
                    mTrackList.clear();
                }
                if (list != null && list.getTracks() != null && list.getTracks().size() != 0) {
                    mPageIndex++;
                    mTotalPage = list.getTotalPage();
                    updateTrackList(mTrackList, list.getTracks());
                }
                mTrackAdapter.notifyDataSetChanged();
                if (newQuery) {
                    mListView.setSelection(0);
                }
                mLoading = false;
                mSearchComfirm.setEnabled(true);
            }

            @Override
            public void onError(int code, String msg) {
                System.out.println("error " + code + " " + msg);
                mLoading = false;
                mSearchComfirm.setEnabled(true);
            }
        });
    }
}
