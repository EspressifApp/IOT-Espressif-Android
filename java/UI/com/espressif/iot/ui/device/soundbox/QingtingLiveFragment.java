package com.espressif.iot.ui.device.soundbox;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.type.device.other.EspAudio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class QingtingLiveFragment extends QingtingBaseFragment {

    private ListView mListView;
    private TrackAdapter mTrackAdapter;
    private List<EspAudio> mTrackList;

    private boolean mLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.qingting_live_fragment, container, false);

        mListView = (ListView)view.findViewById(R.id.list);
        mTrackList = new ArrayList<EspAudio>();
        mTrackAdapter = new TrackAdapter(getActivity(), mTrackList);
        mListView.setAdapter(mTrackAdapter);
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

        refresh();
    }

    @Override
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
        new AsyncTask<Void, Void, List<EspAudio>>() {
            protected void onPreExecute() {
            }

            @Override
            protected List<EspAudio> doInBackground(Void... params) {
                return mQingtingFM.getLiveChannels();
            }

            protected void onPostExecute(List<EspAudio> result) {
                if (result != null) {
                    mTrackList.addAll(result);
                    mTrackAdapter.notifyDataSetChanged();
                }

                mLoading = false;
            }
        }.execute();
    }
}
