package com.rm.cameraphone.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.rm.cameraphone.events.VideoPlayerCallbacks;

/**
 * Created by alex
 */
public class VideoPlayerView extends FrameLayout implements
        SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private String mVideoPath;
    private boolean mIsPrepared;

    private VideoPlayerCallbacks mVideoPlayerCallbacks;

    private Runnable mProgressTask;

    public VideoPlayerView(Context context) {
        super(context);
        initialize();
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {

        mSurfaceView = new SurfaceView(getContext());
        mSurfaceView.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        mSurfaceView.setZOrderMediaOverlay(true);
        mSurfaceView.setVisibility(GONE);
        
        mProgressTask = registerVideoProgressTask();
    }

    private Runnable registerVideoProgressTask() {
        return new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer == null || !mMediaPlayer.isPlaying()) return;
                if (mVideoPlayerCallbacks == null) return;

                mVideoPlayerCallbacks.onVideoProgressChanged(mMediaPlayer.getCurrentPosition());
                postDelayed(mProgressTask, 300);
            }
        };
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(mSurfaceView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setupMediaPlayer();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("VideoPlayerView", "surfaceDestroyed");
        mIsPrepared = false;
        releaseMediaPlayer();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d("VideoPlayerView", "onPrepared");
        mIsPrepared = true;

        if (mVideoPlayerCallbacks != null) {
            mVideoPlayerCallbacks.onVideoPlayerReady(mp.getDuration());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        pause();
        if (mVideoPlayerCallbacks != null)
            mVideoPlayerCallbacks.onVideoProgressChanged(mMediaPlayer.getDuration());
    }

    public void play() {
        if (mMediaPlayer == null || !mIsPrepared) return;

        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            post(mProgressTask);
        }

        if (mVideoPlayerCallbacks != null) {
            mVideoPlayerCallbacks.onVideoPlayerStarted();
        }
    }

    public void pause() {
        if (mMediaPlayer == null || !mIsPrepared) return;

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            removeCallbacks(mProgressTask);
        }

        if (mVideoPlayerCallbacks != null) {
            mVideoPlayerCallbacks.onVideoPlayerStopped();
        }
    }

    public void progressTo(int progress) {
        if (mMediaPlayer == null || !mIsPrepared) return;

        mMediaPlayer.seekTo(progress);
    }

    public boolean isPlaying() {
        return !(mMediaPlayer == null || !mIsPrepared) && mMediaPlayer.isPlaying();

    }

    public void showPreview() {
        setupSurfaceHolder();
    }

    public void removePreview() {
        releaseMediaPlayer();
        mIsPrepared = false;
        mSurfaceView.setVisibility(GONE);
        mSurfaceHolder.removeCallback(this);
    }

    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
    }

    public void setVideoPlayerCallbacks(VideoPlayerCallbacks videoPlayerCallbacks) {
        mVideoPlayerCallbacks = videoPlayerCallbacks;
    }

    private void setupSurfaceHolder() {
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceView.setVisibility(VISIBLE);
    }

    private void setupMediaPlayer() {
        if (mVideoPath == null || mVideoPath.isEmpty()) return;

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setDataSource(mVideoPath);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
