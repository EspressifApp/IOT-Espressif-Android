package com.espressif.iot.type.device.status;

import com.espressif.iot.type.device.other.EspAudio;

public class EspStatusSoundbox implements IEspStatusSoundbox {
    private EspAudio mAudio;

    private int mAction = ACTION_NULL;
    private int mPlayStatus = -1;
    private int mVolume = -1;

    @Override
    public void setAudio(EspAudio audio) {
        mAudio = audio;
    }

    @Override
    public EspAudio getAudio() {
        return mAudio;
    }

    @Override
    public void setPlayStatus(int playStatus) {
        mPlayStatus = playStatus;
    }

    @Override
    public int getPlayStatus() {
        return mPlayStatus;
    }

    @Override
    public void setVolume(int percent) {
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }
        mVolume = percent;
    }

    @Override
    public int getVolume() {
        return mVolume;
    }

    @Override
    public void setAction(int action) {
        mAction = action;
    }

    @Override
    public int getAction() {
        return mAction;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("action = ");
        switch(mAction) {
            case ACTION_AUDIO:
                String audioStr = mAudio == null ? "NULL" : (mAudio.getId() + " - " + mAudio.getTitle() + " /// " + mAudio.getDownloadUrl());
                sb.append("ACTION_AUDIO, audio = " + audioStr);
                break;
            case ACTION_PLAY:
                sb.append("ACTION_PLAY, play status = ").append(mPlayStatus);
                break;
            case ACTION_VOLUME:
                sb.append("ACTION_VOLUME, volume = ").append(mVolume);
                break;
            case ACTION_NULL:
                sb.append("ACTION_NULL");
                break;
             default:
                 sb.append("UNKNOW");
                 break;
        }
        
        return sb.toString();
    }
}
