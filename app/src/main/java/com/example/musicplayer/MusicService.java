package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mediaPlayer;
    private ArrayList<AudioModel> musics;
    private IBinder binder = new MusicBinder();
    private Integer musicPosition;
    private AudioModel music;
    private Uri musicUri;
    private SendDataToPlayer sendDataToPlayer;

    private Notification notification;
    private NotificationManager manager;
    private Intent showPlayer;
    private PendingIntent showPlayerPendingIntent;
    private PendingIntent nextAction, previousAction, playPauseAction;

    private static final String NEXT_ACTION = "nextAction";
    private static final String PREVIOUS_ACTION = "previousAction";
    private static final String PLAY_PAUSE_ACTION = "playPauseAction";
    private static final int NOTIFICATION_ID = 500;

    private Boolean isStop=true;

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        initPendingIntent();
        createNotification();
        startForeground(NOTIFICATION_ID, notification);

        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case PREVIOUS_ACTION:
                    previousMusic();
                    break;
                case PLAY_PAUSE_ACTION:
                    playPauseMusic();

                    break;
                case NEXT_ACTION:
                    nextMusic();
                    break;
            }
        }
        return START_REDELIVER_INTENT;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {


        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        mediaPlayer.release();
        mediaPlayer = null;
        stopForeground(true);
        return false;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        createNotification();
        manager.notify(NOTIFICATION_ID,notification);
        sendDataToPlayer.musicPrepared(music, mediaPlayer);
        mp.start();

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        isStop=true;
        createNotification();
        manager.notify(NOTIFICATION_ID,notification);

        sendDataToPlayer.musicCompleted();


    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public void setMusics(ArrayList<AudioModel> musics) {
        this.musics = musics;
    }

    public void setMusicPosition(int position) {
        musicPosition = position;
    }

    public void playMusic() {
        isStop=false;
        mediaPlayer.reset();
        music = musics.get(musicPosition);

        long musicID = music.getMusicID();
        musicUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicID);
        try {
            mediaPlayer.setDataSource(this, musicUri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playPauseMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            sendDataToPlayer.playOrPause(0, mediaPlayer);
            isStop=true;
        } else {
            mediaPlayer.start();
            sendDataToPlayer.playOrPause(1, mediaPlayer);
            isStop=false;
        }
        createNotification();
        manager.notify(NOTIFICATION_ID,notification);
    }

    public void nextMusic() {
        if (musicPosition == null) {
            return;
        }
        if (musicPosition + 1 < musics.size()) {
            musicPosition++;

        } else {
            musicPosition = 0;

        }
        playMusic();
    }

    public void previousMusic() {
        if (musicPosition == null) {
            return;
        }
        if (musicPosition + 1 > 0) {
            musicPosition--;

        } else {
            musicPosition = musics.size() - 1;

        }
        playMusic();
    }

    public void setSendDataToPlayer(SendDataToPlayer sendDataToPlayer) {
        this.sendDataToPlayer = sendDataToPlayer;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }


    private void createNotification() {
        Bitmap cover = null;
        String title;
        if (music == null) {
            cover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_music_note_24);
            title = "Music Player";
        } else {
            title = music.getMusicName();
            if (Utils.getCoverBitmap(this, music.getMusicID())==null){
                cover = BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_music_note_24);
            }else {
                cover = Utils.getCoverBitmap(this, music.getMusicID());
            }
        }

        notification = new NotificationCompat.Builder(this, myApplicationClass.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
                .setContentIntent(showPlayerPendingIntent)
                .setContentTitle(title)
                .setOnlyAlertOnce(true)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setLargeIcon(cover)
                .addAction(R.drawable.ic_baseline_skip_previous_24, null, previousAction)
                .addAction(setPlayPauseActionIcon(), null, playPauseAction)
                .addAction(R.drawable.ic_baseline_skip_next_24, null, nextAction)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                
                .build();

    }

    private void initPendingIntent() {
        showPlayer = new Intent(this, MainActivity.class);
        showPlayer.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        showPlayerPendingIntent = PendingIntent.getActivity(this, 100, showPlayer, 0);

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(NEXT_ACTION);
        nextAction = PendingIntent.getService(this, 101, intent, 0);

        intent.setAction(PREVIOUS_ACTION);
        previousAction = PendingIntent.getService(this, 102, intent, 0);

        intent.setAction(PLAY_PAUSE_ACTION);
        playPauseAction = PendingIntent.getService(this, 103, intent, 0);

    }
    private int setPlayPauseActionIcon(){
        if (isStop){
            return R.drawable.ic_baseline_play_arrow_24;
        }else{
            return R.drawable.ic_baseline_pause_24;
        }
    }
}
