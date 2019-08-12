package com.lzk.videoplayersdk.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.lzk.videoplayersdk.Bean.AdParameters;
import com.lzk.videoplayersdk.R;
import com.lzk.videoplayersdk.Util.Utils;
import com.lzk.videoplayersdk.constants.SDKConstant;

public class VideoPlayer extends RelativeLayout implements View.OnClickListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, TextureView.SurfaceTextureListener{

    private static final String TAG = VideoPlayer.class.getSimpleName();

    private ViewGroup mParentContainer;
    private TextureView mTextureView;
    private ImageView mFrameIv;
    private ImageView mPlayIv;
    private RelativeLayout mBottomRl;
    private ImageView mFullScreenIv;
    private ProgressBar mProgressBar;
    private Surface mSurface;
    private int mCurrentCount;

    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PLAYING = 1;
    private static final int STATE_PAUSING = 2;
    private static final int LOAD_TOTAL_COUNT = 3;

    private int playerState = STATE_IDLE;

    private int mScreenWidth, mDestationHeight;

    private MediaPlayer mediaPlayer;
    private String mUrl;
    private String mFrameUrl;
    private VideoPlayerListener mPlayerListener;
    private Context mContext;

    private boolean mIsComplete,mIsRealPause;
    private ScreenEventReceiver mScreenReceiver;

    public VideoPlayer(Context context,ViewGroup mParentContainer,String videoUrl,String frameUrl,VideoPlayerListener listener) {
        super(context);
        this.mContext = context;
        this.mParentContainer = mParentContainer;
        this.mUrl = videoUrl;
        this.mFrameUrl = frameUrl;
        this.mPlayerListener = listener;
        initData();
        initView();
        initEvent();
    }

    private void initData(){
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mDestationHeight = (int) (mScreenWidth * SDKConstant.VIDEO_HEIGHT_PERCENT);
    }

    private void initView(){
        RelativeLayout mLayout = (RelativeLayout) LayoutInflater.from(this.getContext()).inflate(R.layout.video_player_layout,this);
        mTextureView = mLayout.findViewById(R.id.video_player_texture_view);
        mFrameIv = mLayout.findViewById(R.id.video_player_frame_iv);
        mPlayIv = mLayout.findViewById(R.id.video_player_play_iv);
        mBottomRl = mLayout.findViewById(R.id.video_player_bottom_rl);
        mFullScreenIv = mLayout.findViewById(R.id.video_player_full_screen_iv);
        mProgressBar = mLayout.findViewById(R.id.video_player_pb);

        LayoutParams params = new LayoutParams(mScreenWidth,mDestationHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLayout.setLayoutParams(params);
    }

    private void initEvent(){
        mTextureView.setOnClickListener(this);
        mTextureView.setKeepScreenOn(true);
        mTextureView.setSurfaceTextureListener(this);
        mPlayIv.setOnClickListener(this);
        mFullScreenIv.setOnClickListener(this);
        registerBroadcastReceiver();
    }

    private void showLoadingView(){
        mProgressBar.setVisibility(VISIBLE);
        mFrameIv.setVisibility(GONE);
        mPlayIv.setVisibility(GONE);
    }

    private void showPlayView(){
        mProgressBar.setVisibility(GONE);
        mFrameIv.setVisibility(GONE);
        mPlayIv.setVisibility(VISIBLE);
        mPlayIv.setImageResource(R.drawable.icon_play);
    }

    private void hideAllView(){
        mProgressBar.setVisibility(GONE);
        mFrameIv.setVisibility(GONE);
        mPlayIv.setVisibility(GONE);
    }

    private void setCurrentPlayState(int state){
        playerState = state;
    }

    public boolean isPlaying(){
        if (mediaPlayer !=null && mediaPlayer.isPlaying()){
            return true;
        }
        return false;
    }

    public void setIsComplete(boolean isComplete) {
        mIsComplete = isComplete;
    }

    public void setIsRealPause(boolean isRealPause) {
        this.mIsRealPause = isRealPause;
    }

    public boolean isComplete() {
        return mIsComplete;
    }

    public boolean isRealPause() {
        return mIsRealPause;
    }

    private void load(){
        if (playerState != STATE_IDLE){
            return;
        }

        showLoadingView();
        setCurrentPlayState(STATE_IDLE);
        checkMediaPlayer();
        try {
            mediaPlayer.setDataSource(this.mUrl);
            mediaPlayer.prepareAsync(); //开始异步加载
        }catch (Exception e){
            Log.d(TAG,e.toString());
            stop();//error以后重新调用stop加载
        }

    }

    private synchronized void checkMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = createMediaPlayer(); //每次都重新创建一个新的播放器
        }
    }

    private MediaPlayer createMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (mSurface != null && mSurface.isValid()) {
            mediaPlayer.setSurface(mSurface);
        } else {
            stop();
        }
        return mediaPlayer;
    }

    public void stop() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.reset();
            this.mediaPlayer.setOnSeekCompleteListener(null);
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
        setCurrentPlayState(STATE_IDLE);
        if (mCurrentCount < LOAD_TOTAL_COUNT) { //满足重新加载的条件
            mCurrentCount += 1;
            load();
        } else {
            showPlayView();
        }
    }

    private void play(){
        if (playerState != STATE_PAUSING ){
            return;
        }

        if (!isPlaying()){
            setCurrentPlayState(STATE_PLAYING);
            setIsComplete(false);
            setIsRealPause(false);
            mediaPlayer.setOnSeekCompleteListener(null);
            mediaPlayer.start();
            hideAllView();
        }else {
            showPlayView();
        }
    }

    private void pause(){
        if (playerState != STATE_PLAYING){
            return;
        }

        if (isPlaying()){
            setCurrentPlayState(STATE_PAUSING);
            setIsRealPause(false);
            setIsComplete(false);
            mediaPlayer.pause();
        }
        showPlayView();
    }

    private void playBack(){
        setCurrentPlayState(STATE_PAUSING);
        if (mediaPlayer != null) {
            mediaPlayer.setOnSeekCompleteListener(null);
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();
        }
        showPlayView();
    }

    private void decideCanPlay() {
        if (Utils.getVisiblePercent(mParentContainer) > SDKConstant.VIDEO_SCREEN_PERCENT)
            //来回切换页面时，只有 >50,且满足自动播放条件才自动播放
            play();
        else
            pause();
    }

    private void registerBroadcastReceiver() {
        if (mScreenReceiver == null) {
            mScreenReceiver = new ScreenEventReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            getContext().registerReceiver(mScreenReceiver, filter);
        }
    }

    private void unRegisterBroadcastReceiver() {
        if (mScreenReceiver != null) {
            getContext().unregisterReceiver(mScreenReceiver);
        }
    }

    public void destroy() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.setOnSeekCompleteListener(null);
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
        unRegisterBroadcastReceiver();
        setCurrentPlayState(STATE_IDLE);
        mCurrentCount = 0;
        setIsComplete(false);
        setIsRealPause(false);
        showPlayView();
    }

    @Override
    protected void onVisibilityChanged( View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE && playerState == STATE_PAUSING) {
            if (isRealPause() || isComplete()) {
                pause();
            } else {
                decideCanPlay();
            }
        } else {
            pause();
        }
    }

    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        if (mPlayerListener != null){
            mPlayerListener.onBufferUpdate(i);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mPlayerListener != null){
            mPlayerListener.onPlayComplete();
        }
        setIsComplete(true);
        setIsRealPause(true);
        setCurrentPlayState(STATE_PAUSING);
        playBack();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        this.playerState = STATE_ERROR;
        mediaPlayer = mediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        if (mCurrentCount > LOAD_TOTAL_COUNT){
            if (mPlayerListener != null){
                mPlayerListener.onLoadingFailed();
                showPlayView();
            }
        }
        stop();
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        hideAllView();
        if (mediaPlayer != null){
            mCurrentCount = 0;
            mediaPlayer.setOnBufferingUpdateListener(this);
            if (mPlayerListener != null){
                mPlayerListener.onLoadingSuccess();
            }
            //满足自动播放条件，则直接播放
            if (Utils.canAutoPlay(getContext(),
                    AdParameters.getCurrentSetting()) &&
                    Utils.getVisiblePercent(mParentContainer) > SDKConstant.VIDEO_SCREEN_PERCENT) {
                setCurrentPlayState(STATE_PAUSING);
                play();
            } else {
                setCurrentPlayState(STATE_PLAYING);
                pause();
            }
        }

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        mSurface = new Surface(surfaceTexture);
        checkMediaPlayer();
        mediaPlayer.setSurface(mSurface);
        load();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onClick(View view) {
        if (view == mPlayIv){
            if (playerState == STATE_PAUSING) {
                if (Utils.getVisiblePercent(mParentContainer)
                        > SDKConstant.VIDEO_SCREEN_PERCENT) {
                    play();
                }
            }else {
                load();
            }
        }
    }

    public interface VideoPlayerListener{
        void onLoadingSuccess();
        void onLoadingFailed();
        void onPlayComplete();
        void onBufferUpdate(int time);
    }

    /**
     * 监听锁屏事件的广播接收器
     */
    private class ScreenEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //主动锁屏时 pause, 主动解锁屏幕时，resume
            switch (intent.getAction()) {
                case Intent.ACTION_USER_PRESENT:
                    if (playerState == STATE_PAUSING) {
                        if (mIsRealPause) {
                            //手动点的暂停，回来后还暂停
                            pause();
                        } else {
                            decideCanPlay();
                        }
                    }
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    if (playerState == STATE_PLAYING) {
                        pause();
                    }
                    break;
            }
        }
    }
}
