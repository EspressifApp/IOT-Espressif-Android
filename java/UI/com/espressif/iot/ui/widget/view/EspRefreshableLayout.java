package com.espressif.iot.ui.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

public class EspRefreshableLayout extends FrameLayout {
    private View mHeaderView;
    private View mRefreshableView;

    private float mTouchDownY;
    private float mTouchMoveY;

    private boolean mRefreshable;

    private static enum State {
        Standby, Pulling, Refreshing
    }

    private State mState = State.Standby;

    public interface OnRefreshListener {
        void onRefresh();
    }

    private OnRefreshListener mRefreshListener;

    public EspRefreshableLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public EspRefreshableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EspRefreshableLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mRefreshable = true;
    }

    public void setRefreshable(boolean refreshable) {
        mRefreshable = refreshable;
    }

    public boolean isRefreshable() {
        return mRefreshable;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (getChildCount() != 2) {
            throw new IllegalArgumentException("Must contain a HeaderView and a ContentView");
        }
        mHeaderView = getChildAt(0);
        mRefreshableView = getChildAt(1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mRefreshable) {
            return false;
        }

        if (mState == State.Refreshing) {
            return true;
        }
        int headerHeight = mHeaderView.getHeight();
        float y = event.getY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mState = State.Pulling;
                mTouchDownY = y;
                return true;

            case MotionEvent.ACTION_MOVE:
                mTouchMoveY = y;
                updateRefreshableViewPosition();
                return true;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchMoveY = y;
                updateRefreshableViewPosition();
                if (mRefreshableView.getY() >= headerHeight) {
                    mState = State.Refreshing;
                    notifyRefreshing();
                    if (mRefreshListener != null) {
                        mRefreshListener.onRefresh();
                    }
                } else if (mRefreshableView.getY() == 0) {
                    mState = State.Standby;
                } else {
                    mState = State.Refreshing;
                    notifyStandby();
                }
                return true;
        }

        return true;
    }

    private void notifyStandby() {
        mRefreshableView.clearAnimation();
        float y = mRefreshableView.getY();
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -y);
        anim.setDuration(300);
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRefreshableView.setY(0);
                mRefreshableView.clearAnimation();
                mState = State.Standby;
            }
        });
        mRefreshableView.startAnimation(anim);
    }

    private void notifyRefreshing() {
        int headerHeight = mHeaderView.getHeight();
        if (mRefreshableView.getY() > headerHeight) {
            float translateY = mRefreshableView.getY() - headerHeight;
            TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -translateY);
            anim.setDuration(100);
            anim.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mRefreshableView.setY(mHeaderView.getHeight());
                    mRefreshableView.clearAnimation();
                }
            });
            mRefreshableView.startAnimation(anim);
        }
    }

    public void notifyRefreshComplete() {
        notifyStandby();
    }

    private void updateRefreshableViewPosition() {
        int headerHeight = mHeaderView.getHeight();
        float moveY = mTouchMoveY - mTouchDownY;
        float offsetY = Math.max(0f, moveY);
        if (moveY > headerHeight) {
            offsetY = headerHeight + (moveY - headerHeight) * (headerHeight / moveY);
        }

        mRefreshableView.setY(offsetY);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mRefreshListener = listener;
    }
}
