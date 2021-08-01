package com.example.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class PlayerFragment extends Fragment implements RecyclerAdapter.OnItemClick, View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private ArrayList<AudioModel> musics = new ArrayList<>();

    public PlayerFragment() {
    }

    private View noMusicView, bottomSheet;
    private RecyclerView recyclerView;
    private BottomSheetBehavior behavior;
    private RecyclerAdapter adapter;
    private MusicService musicService;
    private Intent serviceIntent;

    private ImageView musicCover;
    private SeekBar seekBar;
    private ImageButton playPauseBtn, nextBtn, previous;
    private TextView musicName;
    private TextView currentTime;
    private TextView fullTime;
    private FloatingActionButton fab;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable seekProgress;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMusics();
    }

    @Nullable
    @Override//در اینجا هنو ویو ساخته نشده
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_fragment, container, false);
        init(rootView);

        return rootView;

    }

    private void init(View rootView) {

        noMusicView = rootView.findViewById(R.id.noMusicView);
        bottomSheet = rootView.findViewById(R.id.bottomSheet_playerFg);
        recyclerView = rootView.findViewById(R.id.recyclerView_playerFG);
        behavior = BottomSheetBehavior.from(bottomSheet);

        musicCover = rootView.findViewById(R.id.music_cover);
        seekBar = rootView.findViewById(R.id.seekBar);
        nextBtn = rootView.findViewById(R.id.music_next_btn);
        playPauseBtn = rootView.findViewById(R.id.music_play_pause_btn);
        previous = rootView.findViewById(R.id.music_pre_btn);
        musicName = rootView.findViewById(R.id.music_name_txt);
        fullTime = rootView.findViewById(R.id.full_time_btm_txt);
        currentTime = rootView.findViewById(R.id.current_time_btm_txt);
        fab = rootView.findViewById(R.id.controller_fab);


        if (musics.size() == 0) {
            noMusicView.setVisibility(View.VISIBLE);
            bottomSheet.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    //در اینجا ساخته شده
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new RecyclerAdapter(getActivity(), musics);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

        playPauseBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        previous.setOnClickListener(this);
        fab.setOnClickListener(this);
        behavior.addBottomSheetCallback(callback);

        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (serviceIntent == null) {
            serviceIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);//فلگی که اخر نوشت شده میگه اگر انکریت فراخوانی نشده بود فراخوانیش کن
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getActivity().startForegroundService(serviceIntent);
            } else {
                getActivity().startService(serviceIntent);
            }
        }
    }

    private void getMusics() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION};
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                AudioModel model = new AudioModel();
                String name = cursor.getString(0);
                String artist = cursor.getString(1);
                long id = cursor.getLong(2);
                long albumId = cursor.getLong(3);
                long duration = cursor.getLong(4);

                model.setMusicName(name);
                model.setMusicArtist(artist);
                model.setMusicID(id);
                model.setMusicAlbum_ID(albumId);
                model.setMusicDuration(duration);

                musics.add(model);
            }
            cursor.close();
        }
    }

    @Override
    public void onItemClickListener(int position) {
        musicService.setMusicPosition(position);
//        playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);
        musicService.playMusic();
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setMusics(musics);

            musicService.setSendDataToPlayer(sendDataToPlayer);


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onDestroy() {
        getActivity().unbindService(serviceConnection);
        musicService = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        buttonClicked(v);
    }

    private void buttonClicked(View v) {
        switch (v.getId()) {
            case R.id.music_play_pause_btn:
                musicService.playPauseMusic();

                break;
            case R.id.music_next_btn:
                musicService.nextMusic();
                break;
            case R.id.music_pre_btn:
                musicService.previousMusic();
                break;
            case R.id.controller_fab:
                if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    fab.setVisibility(View.GONE);
                }
                break;

        }

    }

    BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                fab.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };
    SendDataToPlayer sendDataToPlayer = new SendDataToPlayer() {
        @Override
        public void musicPrepared(AudioModel audioModel, MediaPlayer player) {
            musicName.setText(audioModel.getMusicName());
            Utils.getCover(getContext(), musicCover, audioModel);
            playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);

            fullTime.setText(Utils.getFullTime(player.getDuration()));
            runRunnable(seekBar, player);
        }

        @Override
        public void playOrPause(int isPlaying, MediaPlayer player) {
            if (isPlaying == 0) {
                playPauseBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                handler.removeCallbacks(seekProgress);
            } else {
                playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);
                runRunnable(seekBar, player);
            }
        }

        @Override
        public void musicCompleted() {
            musicService.nextMusic();
            handler.removeCallbacks(seekProgress);
            handler.postDelayed(seekProgress, 0);
        }
    };

    private void runRunnable(SeekBar seekBar, MediaPlayer player) {
        seekBar.setMax(player.getDuration());
        if (seekProgress != null) {
            handler.removeCallbacks(seekProgress);
        }
        seekProgress = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(player.getCurrentPosition() + 1);

                handler.postDelayed(this, 200);
            }
        };
        handler.postDelayed(seekProgress, 0);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            musicService.getMediaPlayer().seekTo(progress);
        }
        currentTime.setText(Utils.getFullTime(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}
