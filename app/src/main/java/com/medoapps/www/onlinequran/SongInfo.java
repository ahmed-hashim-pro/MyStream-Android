package com.medoapps.www.onlinequran;

/**
 * Created by MEDO on 01/02/2018.
 */

public class SongInfo {

    public String Path;
    public String song_name;
    public String album_name;
    public String artist_name;
    public String title_name;
    public String duration;


    public SongInfo(String Path, String song_name, String album_name,
                    String artist_name,String title_name,String duration){
        this.Path=Path;
        this.song_name=song_name;
        this.album_name=album_name;
        this.artist_name=artist_name;
        this.title_name=title_name;
        this.duration=duration;
    }
}
