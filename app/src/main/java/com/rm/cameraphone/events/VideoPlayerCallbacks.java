package com.rm.cameraphone.events;

/**
 * Created by alex
 */
public interface VideoPlayerCallbacks {

    void onVideoProgressChanged(int progress);
    void onVideoPlayerStarted();
    void onVideoPlayerStopped();
    void onVideoPlayerReady(int duration);
}
