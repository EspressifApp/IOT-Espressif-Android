package com.espressif.iot.ui.device.soundbox;

public class EspTrack {
    public static enum Platform {
        Ximalaya;
    }

    private Platform mPlat;

    private String mTitle;
    private String mIntro;
    private long mDownloadSize;
    private String mDownloadUrl;
    private int mDuration;
    private String mCoverUrlSmall;
    private String mCoverUrlMiddle;
    private String mCoverUrlLarge;

    public EspTrack(Platform plat) {
        mPlat = plat;
    }

    public Platform getPlatform() {
        return mPlat;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setIntro(String intro) {
        mIntro = intro;
    }

    public String getIntro() {
        return mIntro;
    }

    public void setDownloadSize(long size) {
        mDownloadSize = size;
    }

    public long getDownloadSize() {
        return mDownloadSize;
    }

    public void setDownloadUrl(String url) {
        mDownloadUrl = url;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void setCoverUrlSmall(String url) {
        mCoverUrlSmall = url;
    }

    public String getCoverUrlSmall() {
        return mCoverUrlSmall;
    }

    public void setCoverUrlMiddle(String url) {
        mCoverUrlMiddle = url;
    }

    public String getCoverUrlMiddle() {
        return mCoverUrlMiddle;
    }

    public void setCoverUrlLarge(String url) {
        mCoverUrlLarge = url;
    }

    public String getCoverUrlLarge() {
        return mCoverUrlLarge;
    }

    public int getDuration() {
        return mDuration;
    }

    public static class Builder {
        private Platform plat;
        private String title;
        private String intro;
        private long downloadSize;
        private String downloadUrl;
        private int duration;
        private String coverUrlSmall;
        private String coverUrlMiddle;
        private String coverUrlLarge;

        public Builder(Platform p) {
            plat = p;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setIntro(String intro) {
            this.intro = intro;
            return this;
        }

        public Builder setDownloadSize(long size) {
            this.downloadSize = size;
            return this;
        }

        public Builder setDownloadUrl(String url) {
            this.downloadUrl = url;
            return this;
        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder setCoverUrlSmall(String url) {
            this.coverUrlSmall = url;
            return this;
        }

        public Builder setCoverUrlMiddle(String url) {
            this.coverUrlMiddle = url;
            return this;
        }

        public Builder setCoverUrlLarge(String url) {
            this.coverUrlLarge = url;
            return this;
        }

        public EspTrack create() {
            EspTrack track = new EspTrack(plat);
            track.setTitle(title);
            track.setIntro(intro);
            track.setDownloadSize(downloadSize);
            track.setDownloadUrl(downloadUrl);
            track.setDuration(duration);
            track.setCoverUrlSmall(coverUrlSmall);
            track.setCoverUrlMiddle(coverUrlMiddle);
            track.setCoverUrlLarge(coverUrlLarge);

            return track;
        }
    }
}
