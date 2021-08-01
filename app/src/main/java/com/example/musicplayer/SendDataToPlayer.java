package com.example.musicplayer;

import android.media.MediaPlayer;

public interface SendDataToPlayer {
    void musicPrepared(AudioModel audioModel, MediaPlayer player);

    void playOrPause(int isPlaying,MediaPlayer player);
    void musicCompleted();


}
