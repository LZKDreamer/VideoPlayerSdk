package com.lzk.videoplayersdk;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.lzk.videoplayersdk.Bean.VideoBean;

public class VideoPlayerSlot implements VideoPlayer.VideoPlayerListener {

    private ViewGroup mParentContainer;
    private VideoPlayer mVideoPlayer;
    private Context mContext;
    private VideoBean mVideoBean;
    private VideoPlayerSlotListener mSlotListener;

    public VideoPlayerSlot(Context context, ViewGroup mParentContainer, VideoBean videoBean,VideoPlayerSlotListener listener){
        mContext = context;
        this.mParentContainer = mParentContainer;
        this.mVideoBean = videoBean;
        this.mSlotListener = listener;
        initVideoView();
    }

    private void initVideoView(){
        if (mVideoBean != null){
            mVideoPlayer = new VideoPlayer(mContext, mParentContainer,mVideoBean.getVideoUrl(),
                    mVideoBean.getFrameUrl(),this);
        }
        RelativeLayout paddingView = new RelativeLayout(mContext);
        paddingView.setBackgroundColor(mContext.getResources().getColor(android.R.color.black));
        paddingView.setLayoutParams(mVideoPlayer.getLayoutParams());
        mParentContainer.addView(paddingView);
        mParentContainer.addView(mVideoPlayer);
    }

    public void destroy() {
        mVideoPlayer.destroy();
        mVideoPlayer = null;
        mContext = null;
        mVideoBean = null;
    }

    @Override
    public void onLoadingSuccess() {
        if (mSlotListener != null){
            mSlotListener.onLoadSuccess();
        }
    }

    @Override
    public void onLoadingFailed() {
        if (mSlotListener != null){
            mSlotListener.onLoadFailed();
        }
    }

    @Override
    public void onPlayComplete() {
        if (mSlotListener != null){
            mSlotListener.onPlayComplete();
        }
    }

    @Override
    public void onBufferUpdate(int time) {

    }

    public interface VideoPlayerSlotListener{
        void onLoadSuccess();
        void onLoadFailed();
        void onPlayComplete();
    }
}
