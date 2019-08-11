package com.lzk.videoplayersdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.lzk.videoplayersdk.Bean.VideoBean;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout mContainerRl;
    private VideoPlayerManager mVideoPlayerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainerRl = findViewById(R.id.main_video_container_rl);
        String url="http://gslb.miaopai.com/stream/4EuUxZejQqFolbiuc7H3~TzaC-T4HcB7jcbR~w__.mp4?vend=miaopai&ssig=6542c8c6007af8698f56557ecb953f0f&time_stamp=1565512363887&mpflag=32";
        VideoBean videoBean =new VideoBean();
        videoBean.setVideoUrl(url);
        mVideoPlayerManager = new VideoPlayerManager(mContainerRl, videoBean, new VideoPlayerManager.VideoPlayerManagerListener() {
            @Override
            public void onLoadSuccess() {

            }

            @Override
            public void onLoadFailed() {

            }

            @Override
            public void onClickVideo(String url) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoPlayerManager.destroy();
    }
}
