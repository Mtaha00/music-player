package com.example.musicplayer;

public class AudioModel {
    private String musicName;
    private String musicArtist;
    private long musicID;
    private long musicAlbum_ID;
    private long musicDuration;

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicArtist() {
        return musicArtist;
    }

    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist;
    }

    public long getMusicID() {
        return musicID;
    }

    public void setMusicID(long musicID) {
        this.musicID = musicID;
    }

    public long getMusicAlbum_ID() {
        return musicAlbum_ID;
    }

    public void setMusicAlbum_ID(long musicAlbum_ID) {
        this.musicAlbum_ID = musicAlbum_ID;
    }

    public long getMusicDuration() {
        return musicDuration;
    }

    public void setMusicDuration(long musicDuration) {
        this.musicDuration = musicDuration;
    }
}
