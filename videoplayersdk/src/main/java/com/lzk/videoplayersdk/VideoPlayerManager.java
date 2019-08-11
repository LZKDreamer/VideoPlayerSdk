package com.lzk.videoplayersdk;

import android.content.Context;
import android.media.MediaRecorder;
import android.print.PrinterId;
import android.view.ViewGroup;

import com.lzk.videoplayersdk.Bean.VideoBean;

public class VideoPlayerManager implements VideoPlayerSlot.VideoPlayerSlotListener {

    private ViewGroup mParentContainer;
    private VideoBean mVideoBean;
    private VideoPlayerSlot mVideoPlayerSlot;
    private VideoPlayerManagerListener mManagerListener;

    public VideoPlayerManager(ViewGroup mParentContainer,VideoBean videoBean,VideoPlayerManagerListener managerListener){
        this.mParentContainer = mParentContainer;
        this.mVideoBean = videoBean;
        this.mManagerListener = managerListener;
        load();
    }

    private void load(){
        if (mVideoBean != null&& mVideoBean.getVideoUrl() != null){
            mVideoPlayerSlot = new VideoPlayerSlot(mParentContainer.getContext(),mParentContainer,mVideoBean,this);
        }else {
            mVideoPlayerSlot = new VideoPlayerSlot(mParentContainer.getContext(),mParentContainer,null,this);
            if (mManagerListener != null){
                mManagerListener.onLoadFailed();
            }
        }
    }

    @Override
    public void onLoadSuccess() {
        if (mManagerListener != null){
            mManagerListener.onLoadSuccess();
        }
    }

    @Override
    public void onLoadFailed() {
        if (mManagerListener != null){
            mManagerListener.onLoadFailed();
        }
    }

    @Override
    public void onPlayComplete() {

    }

    public void destroy(){
        if (mVideoPlayerSlot != null){
            mVideoPlayerSlot.destroy();
        }
    }

    public interface VideoPlayerManagerListener{
        void onLoadSuccess();
        void onLoadFailed();
        void onClickVideo(String url);

    }
}
