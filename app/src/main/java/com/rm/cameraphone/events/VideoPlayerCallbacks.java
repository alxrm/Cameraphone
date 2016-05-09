package com.rm.cameraphone.events;

/**
 * Created by alex
 */
public interface VideoPlayerCallbacks {

    void onVideoProgressChanged(int progress);
    void onVideoPlayerStarted(int at);
    void onVideoPlayerStopped(int at);
    void onVideoPlayerReady(int duration);
}
